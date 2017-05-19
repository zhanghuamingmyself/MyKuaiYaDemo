package com.zhanghuaming.mykuaiyademo.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.zhanghuaming.mykuaiyademo.AppContext;
import com.zhanghuaming.mykuaiyademo.Constant;
import com.zhanghuaming.mykuaiyademo.R;
import com.zhanghuaming.mykuaiyademo.common.BaseActivity;
import com.zhanghuaming.mykuaiyademo.core.entity.IpPortInfo;
import com.zhanghuaming.mykuaiyademo.core.service.SenderMsgService;
import com.zhanghuaming.mykuaiyademo.core.utils.ApMgr;
import com.zhanghuaming.mykuaiyademo.core.utils.MLog;
import com.zhanghuaming.mykuaiyademo.core.utils.ToastUtils;
import com.zhanghuaming.mykuaiyademo.core.utils.WifiMgr;
import com.zhanghuaming.mykuaiyademo.ui.adapter.WifiScanResultAdapter;
import com.zhanghuaming.mykuaiyademo.ui.view.RadarScanView;
import com.zhanghuaming.mykuaiyademo.utils.ListUtils;
import com.zhanghuaming.mykuaiyademo.utils.NavigatorUtils;
import com.zhanghuaming.mykuaiyademo.utils.NetUtils;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.zhanghuaming.mykuaiyademo.core.service.SenderMsgService.MSG_TO_FILE_SENDER_UI;
import static com.zhanghuaming.mykuaiyademo.core.service.SenderMsgService.MSG_TO_SHOW_SCAN_RESULT;


/**
 * Created by zhanghuaming on 2016/11/28.
 * Contact me zhanghuamingmyself@163.com
 */
public class ChooseReceiverActivity extends BaseActivity {

    private static final String TAG = ChooseReceiverActivity.class.getSimpleName();
    /**
     * Topbar相关UI
     */
    @Bind(R.id.tv_back)
    TextView tv_back;

    /**
     * 其他UI
     */
    @Bind(R.id.radarView)
    RadarScanView radarScanView;
//    @Bind(R.id.tab_layout)
//    TabLayout tab_layout;
//    @Bind(R.id.view_pager)
//    ViewPager view_pager;

    /**
     * 扫描结果
     */
    @Bind(R.id.lv_result)
    ListView lv_result;

    List<ScanResult> mScanResultList;
    WifiScanResultAdapter mWifiScanResultAdapter;


    /**
     * 与 文件发送方 通信的 线程
     */


    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
             if (msg.what == MSG_TO_SHOW_SCAN_RESULT) {
                getOrUpdateWifiScanResult();
            }

        }
    };
    int count = 0;
    Runnable Scan = new Runnable() {
        @Override
        public void run() {
            while (count < Constant.DEFAULT_TRY_TIME  && !(ApMgr.isApOn(ChooseReceiverActivity.this))) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_TO_SHOW_SCAN_RESULT), 1000);
                count++;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            count = 0;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_receiver);

        ButterKnife.bind(this);

        init();
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_OPEN_GPS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 允许
                updateUI();
            } else {
                // Permission Denied
                ToastUtils.show(this, getResources().getString(R.string.tip_permission_denied_and_not_get_wifi_info_list));
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }




    /**
     * 初始化
     */
    private void init() {
        radarScanView.startScan();

//        if(WifiMgr.getInstance(getContext()).isWifiEnable()){//wifi打开的情况
//        }else{//wifi关闭的情况
//            WifiMgr.getInstance(getContext()).openWifi();
//        }

        if (!WifiMgr.getInstance(getContext()).isWifiEnable()) {//wifi未打开的情况
            WifiMgr.getInstance(getContext()).openWifi();
        }

        //Android 6.0 扫描wifi 需要开启定位
        if (Build.VERSION.SDK_INT >= 23) { //Android 6.0 扫描wifi 需要开启定位
            if (ContextCompat.checkSelfPermission(this, Manifest.permission_group.LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // 获取wifi连接需要定位权限,没有获取权限
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                }, REQUEST_CODE_OPEN_GPS);
                return;
            }
        } else {//Android 6.0 以下的直接开启扫描
            updateUI();
        }
    }

    /**
     * 更新UI
     */
    private void updateUI() {
            getOrUpdateWifiScanResult();
            new Thread(Scan).start();
    }

    /**
     * 获取或者更新wifi扫描列表
     */


    private void getOrUpdateWifiScanResult() {

        WifiMgr.getInstance(getContext()).startScan();
        mScanResultList = WifiMgr.getInstance(getContext()).getScanResultList();
        mScanResultList = ListUtils.filterWithNoPassword(mScanResultList);//过滤没用的网络

        if (mScanResultList != null) {
            mWifiScanResultAdapter = new WifiScanResultAdapter(getContext(), mScanResultList);
            lv_result.setAdapter(mWifiScanResultAdapter);
        }
        lv_result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO 进入文件传输部分
                ScanResult scanResult = mScanResultList.get(position);
                Log.i(TAG, "###select the wifi info ======>>>" + scanResult.toString());

                //1.连接网络
                String ssid = Constant.DEFAULT_SSID;
                ssid = scanResult.SSID;
                WifiMgr.getInstance(getContext()).openWifi();
                WifiMgr.getInstance(getContext()).addNetwork(WifiMgr.createWifiCfg(ssid, null, WifiMgr.WIFICIPHER_NOPASS));


                //开始连接
                startConnectServer();
            }
        });

    }
    public void startConnectServer() {
        String addr;
        addr = WifiMgr.getInstance(ChooseReceiverActivity.this).getIpAddressFromHotspot();
        AppContext.MAIN_EXECUTOR.execute(createConnectRunnable(addr));
    }

    /**
     * 创建发送UDP消息到 文件接收方 的服务线程
     *
     * @param serverIP
     */
    public Runnable createConnectRunnable(final String serverIP) {
        Log.i(TAG, "receiver serverIp ----->>>" + serverIP);
        return new Runnable() {
            @Override
            public void run() {
                try {
                    startConnectServer(serverIP, Constant.DEFAULT_SERVER_COM_PORT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 开启 文件发送方 通信服务 (必须在子线程执行)
     *
     * @param targetIpAddr
     * @param serverPort
     * @throws Exception
     */

    private void startConnectServer(String targetIpAddr, int serverPort) throws Exception {
//        Thread.sleep(3*1000);
        // 确保Wifi连接上之后获取得到IP地址
        int count = 0;
        while (targetIpAddr.equals(Constant.DEFAULT_UNKOWN_IP) && count < Constant.DEFAULT_TRY_TIME) {
            Thread.sleep(1000);
            targetIpAddr = WifiMgr.getInstance(ChooseReceiverActivity.this).getIpAddressFromHotspot();
            MLog.i(TAG, "receiver serverIp ----->>>" + targetIpAddr);
            count++;
        }

        // 即使获取到连接的热点wifi的IP地址也是无法连接网络 所以采取此策略
        count = 0;
        while (!NetUtils.pingIpAddress(targetIpAddr) && count < Constant.DEFAULT_TRY_TIME) {
            Thread.sleep(500);
            MLog.i(TAG, "try to ping ----->>>" + targetIpAddr + " - " + count);
            count++;
        }


        if (Constant.mSendDatagramSocket == null) {
            Constant.mSendDatagramSocket = new DatagramSocket(serverPort);
        }

        InetAddress ipAddress = InetAddress.getByName(targetIpAddr);
        Constant.friendIpPortInfo = new IpPortInfo(ipAddress,Constant.DEFAULT_SERVER_COM_PORT);//保存对方（开热点）的地址
        MLog.i(TAG, "save friend ipAddress is " + ipAddress);


        ToastUtils.show(ChooseReceiverActivity.this,"已经连接到热点"+ipAddress);

        //0.打开发送的文件列表的activity
       // Intent sendMsgIntent = new Intent(ChooseReceiverActivity.this,SendFileMessageActivity.class);
       // startActivity(sendMsgIntent);
        finish();




    }

    @OnClick({R.id.tv_back, R.id.radarView})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back: {
                onBackPressed();
                break;
            }
            case R.id.radarView: {
                MLog.i(TAG, "radarView ------>>> click!");
                new Thread(Scan).start();
                break;
            }
        }
    }



}
