package com.zhanghuaming.mykuaiyademo.core.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.zhanghuaming.mykuaiyademo.AppContext;
import com.zhanghuaming.mykuaiyademo.Constant;
import com.zhanghuaming.mykuaiyademo.core.entity.FileInfo;
import com.zhanghuaming.mykuaiyademo.core.entity.IpPortInfo;
import com.zhanghuaming.mykuaiyademo.core.utils.MLog;
import com.zhanghuaming.mykuaiyademo.core.utils.WifiMgr;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * Created by zhang on 2017/5/18.
 */

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class ReceiverMsgService extends IntentService {

    private static final String TAG = "ReceiverMsgService";
    private final IBinder binder = new ReceiverServiceBinder();

    /**
     * 与 文件发送方 通信的 线程
     */
    Runnable mUdpServerRuannable;

    public static final int MSG_TO_START_RECEIVER_FILE_MSG = 0X88;
    public static final int MSG_TO_CLOSE_SOCKET = 0X11;
    public static final int MSG_TO_FILE_RECEIVER_UI = 0X22;
    public static final int MSG_TO_GET_SELF_IP = 0X33;
    public volatile static boolean mIsInitialized = false;


    public ReceiverMsgService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    public ReceiverMsgService() {
        super(TAG);
        // TODO Auto-generated constructor stub
    }

    public class ReceiverServiceBinder extends Binder {
        public ReceiverMsgService getService() {
            return ReceiverMsgService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        Messenger messenger = new Messenger(new IncomingHandler());
        return messenger.getBinder();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while (true) ;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        MLog.i(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MLog.i(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MLog.i(TAG, "onDestroy()");
        _hd.obtainMessage(MSG_TO_CLOSE_SOCKET).sendToTarget();
    }

    /**
     * 获取自己的IP 的服务线程
     */
    private void getIP()
    {
        new Thread(createGetSelfIPRunnable()).start();
    }
    private Runnable createGetSelfIPRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    getSelfIPServer(Constant.DEFAULT_SERVER_COM_PORT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
    DatagramSocket mDatagramSocket;
    private void getSelfIPServer(int serverPort)throws Exception{
        //网络连接上，无法获取IP的问题
        int count = 0;
        String localAddress = WifiMgr.getInstance(ReceiverMsgService.this).getHotspotLocalIpAddress();
        while (localAddress.equals(Constant.DEFAULT_UNKOWN_IP) && count < Constant.DEFAULT_TRY_TIME) {
            Thread.sleep(1000);
            localAddress = WifiMgr.getInstance(ReceiverMsgService.this).getHotspotLocalIpAddress();
            Log.i(TAG, "receiver get local Ip ----->>>" + localAddress);
            count++;
        }
        Constant.localAddress = localAddress;//保存自己的地址
        mDatagramSocket = new DatagramSocket(serverPort);

    }


    public void startSendMsgToServer() {
        if (!mIsInitialized) {
            mUdpServerRuannable = createSendMsgToFileSenderRunnable();
            AppContext.MAIN_EXECUTOR.execute(mUdpServerRuannable);
            mIsInitialized = true;
        }
    }

    /**
     * 创建发送UDP消息到 文件发送方 的服务线程
     */
    private Runnable createSendMsgToFileSenderRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    startFileReceiverServer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }


    /**
     * 开启 文件接收方 通信服务 (必须在子线程执行)
     *
     * @param serverPort
     * @throws Exception
     */


    private void startFileReceiverServer() throws Exception {

        byte[] receiveData = new byte[1024];
        byte[] sendData = null;
        while (true) {
            //1.接收 文件发送方的消息
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            mDatagramSocket.receive(receivePacket);
            String msg = new String(receivePacket.getData()).trim();
            InetAddress inetAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            Constant.friendIpPortInfo = new IpPortInfo(inetAddress, port);//保存连接方的IP信息
            if (msg != null && msg.startsWith(Constant.MSG_FILE_RECEIVER_INIT)) {
                MLog.i(TAG, "Get the msg from FileReceiver######>>>" + Constant.MSG_FILE_RECEIVER_INIT);
                // 进入文件接收列表界面 (文件接收列表界面需要 通知 文件发送方发送 文件开始传输UDP通知)
                _hd.obtainMessage(MSG_TO_FILE_RECEIVER_UI, Constant.friendIpPortInfo).sendToTarget();
            } else { //接收发送方的 文件列表
                if (msg != null) {
                    MLog.i("TAG", "Get the FileInfo from FileReceiver######>>>" + msg);
                    parseFileInfo(msg);
                }
            }
        }
    }

    /**
     * 解析FileInfo
     *
     * @param msg
     */
    private void parseFileInfo(String msg) {
        FileInfo fileInfo = FileInfo.toObject(msg);
        if (fileInfo != null && fileInfo.getFilePath() != null) {
            AppContext.getAppContext().addReceiverFileInfo(fileInfo);
        }
    }

    /**
     * 关闭UDP Socket 流
     */
    private void closeSocket() {
        if (mDatagramSocket != null) {
            mDatagramSocket.disconnect();
            mDatagramSocket.close();
            mDatagramSocket = null;
        }
    }

    public static final int SET_HANDLER = 4;
    Handler _hd = null;

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case SET_HANDLER:
                    _hd = (Handler) msg.obj;
                    break;
                case MSG_TO_CLOSE_SOCKET:
                    closeSocket();
                    break;
                case MSG_TO_START_RECEIVER_FILE_MSG:
                    startSendMsgToServer();
                    break;
                case MSG_TO_GET_SELF_IP:
                    getIP();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }

        }
    }
}