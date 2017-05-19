package com.zhanghuaming.mykuaiyademo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zhanghuaming.mykuaiyademo.AppContext;
import com.zhanghuaming.mykuaiyademo.Constant;
import com.zhanghuaming.mykuaiyademo.R;
import com.zhanghuaming.mykuaiyademo.common.BaseActivity;
import com.zhanghuaming.mykuaiyademo.core.entity.FileInfo;
import com.zhanghuaming.mykuaiyademo.core.entity.IpPortInfo;
import com.zhanghuaming.mykuaiyademo.core.receiver.WifiAPBroadcastReceiver;
import com.zhanghuaming.mykuaiyademo.core.service.ReceiverMsgService;
import com.zhanghuaming.mykuaiyademo.core.service.SenderMsgService;
import com.zhanghuaming.mykuaiyademo.core.utils.ApMgr;
import com.zhanghuaming.mykuaiyademo.core.utils.MLog;
import com.zhanghuaming.mykuaiyademo.core.utils.TextUtils;
import com.zhanghuaming.mykuaiyademo.core.utils.ToastUtils;
import com.zhanghuaming.mykuaiyademo.core.utils.WifiMgr;
import com.zhanghuaming.mykuaiyademo.ui.view.RadarLayout;
import com.zhanghuaming.mykuaiyademo.utils.NavigatorUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.zhanghuaming.mykuaiyademo.core.service.ReceiverMsgService.MSG_TO_FILE_RECEIVER_UI;
import static com.zhanghuaming.mykuaiyademo.core.service.ReceiverMsgService.mIsInitialized;


/**
 * 接收等待文件传输UI
 */
public class ReceiverWaitingActivity extends BaseActivity {

    private static final String TAG = ReceiverWaitingActivity.class.getSimpleName();

    /**
     * Android 6.0 modify wifi status need this permission: android.permission.WRITE_SETTINGS
     */
    public static final int REQUEST_CODE_WRITE_SETTINGS = 7879;

    /**
     * Topbar相关UI
     */
    @Bind(R.id.tv_back)
    TextView tv_back;

    /**
     * 其他UI
     */
    @Bind(R.id.radarLayout)
    RadarLayout radarLayout;
    @Bind(R.id.tv_device_name)
    TextView tv_device_name;
    @Bind(R.id.tv_desc)
    TextView tv_desc;

    WifiAPBroadcastReceiver mWifiAPBroadcastReceiver;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_TO_FILE_RECEIVER_UI){
                IpPortInfo ipPortInfo = (IpPortInfo) msg.obj;
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constant.KEY_IP_PORT_INFO, ipPortInfo);
                NavigatorUtils.toFileReceiverListUI(getContext(), bundle);
                finishNormal();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver_waiting);

        ButterKnife.bind(this);

        initWithGetPermission(this);
    }

    /**
     * 初始化并且获取权限
     * @param context
     */
    public void initWithGetPermission(Activity context){
        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(context);
        } else {
            permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (permission) {
            //do your code
            init();
        }  else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_SETTINGS}, REQUEST_CODE_WRITE_SETTINGS);
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS && Settings.System.canWrite(this)){
            Log.d(TAG, "CODE_WRITE_SETTINGS_PERMISSION success");
            //do your code
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_SETTINGS && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //do your code
            init();
        } else {
            // Permission Denied
            ToastUtils.show(this, getResources().getString(R.string.tip_permission_denied_and_not_modify_ap_info));
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


    /**
     * 初始化
     */
    private void init(){
        radarLayout.setUseRing(true);
        radarLayout.setColor(getResources().getColor(R.color.white));
        radarLayout.setCount(4);
        radarLayout.start();

        //1.初始化热点
        WifiMgr.getInstance(getContext()).disableWifi();

        if(ApMgr.isApOn(getContext())){
            ApMgr.disableAp(getContext());
        }

        Intent ssi = new Intent(ReceiverWaitingActivity.this, ReceiverMsgService.class);
        bindService(ssi, ssc, BIND_AUTO_CREATE);//绑定接收消息的服务
        bound = true;

        mWifiAPBroadcastReceiver = new WifiAPBroadcastReceiver() {
            @Override
            public void onWifiApEnabled() {
                Log.i(TAG, "======>>>onWifiApEnabled !!!");

                if(!mIsInitialized){
                    if (bound) {
                        //初始化获取自己的IP
                        Message message = Message.obtain(null,
                                ReceiverMsgService.MSG_TO_GET_SELF_IP, 0, 0);
                        //开始监听端口，等待连接
                        Message message2 = Message.obtain(null,
                                ReceiverMsgService.MSG_TO_START_RECEIVER_FILE_MSG, 0, 0);
                        try {
                            messenger.send(message);
                            messenger.send(message2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    tv_desc.setText(getResources().getString(R.string.tip_now_init_is_finish));
                    tv_desc.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tv_desc.setText(getResources().getString(R.string.tip_is_waitting_connect));
                        }
                    }, 2*1000);
                }
            }
        };
        IntentFilter filter = new IntentFilter(WifiAPBroadcastReceiver.ACTION_WIFI_AP_STATE_CHANGED);
        registerReceiver(mWifiAPBroadcastReceiver, filter);

        ApMgr.isApOn(getContext());// check Ap state :boolean
        String ssid;
        ssid = Constant.MYName;
        ApMgr.configApState(getContext(), ssid); // change Ap state :boolean
        tv_device_name.setText(ssid);
        tv_desc.setText(getResources().getString(R.string.tip_now_is_initial));
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(mWifiAPBroadcastReceiver != null){
            unregisterReceiver(mWifiAPBroadcastReceiver);
            mWifiAPBroadcastReceiver = null;
        }

        if (bound) {
            Message message = Message.obtain(null,
                    ReceiverMsgService.MSG_TO_CLOSE_SOCKET, 0, 0);
            try {
                messenger.send(message);
                //unbindService(ssc);//解绑服务
                //bound = false;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        //关闭热点
        //ApMgr.disableAp(getContext());
        this.finish();
    }


    /**
     * 成功进入 文件接收列表UI 调用的finishNormal()
     */
    private void finishNormal(){
        if(mWifiAPBroadcastReceiver != null){
            unregisterReceiver(mWifiAPBroadcastReceiver);
            mWifiAPBroadcastReceiver = null;
        }

        if (bound) {
            Message message = Message.obtain(null,
                    ReceiverMsgService.MSG_TO_CLOSE_SOCKET, 0, 0);
            try {
                messenger.send(message);
                unbindService(ssc);//解绑服务
                bound = false;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        this.finish();
    }



    @OnClick({R.id.tv_back})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_back:{
                onBackPressed();
                break;
            }
        }
    }


    Messenger messenger;
    private ReceiverMsgService receiverMsgService;//发送服务
    private Boolean bound = false;//是否绑定服务
    private ServiceConnection ssc = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            MLog.e(TAG,"已经绑定接收消息的服务");
            messenger = new Messenger(service);
            Message message = Message.obtain(null,
                    ReceiverMsgService.SET_HANDLER, 0, 0);
            message.obj = mHandler;
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            messenger = null;
            bound =false;
        }

    };

}
