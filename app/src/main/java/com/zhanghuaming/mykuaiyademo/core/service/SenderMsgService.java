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
import com.zhanghuaming.mykuaiyademo.core.BaseTransfer;
import com.zhanghuaming.mykuaiyademo.core.entity.FileInfo;
import com.zhanghuaming.mykuaiyademo.core.utils.MLog;
import com.zhanghuaming.mykuaiyademo.core.utils.WifiMgr;
import com.zhanghuaming.mykuaiyademo.utils.NetUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by zhang on 2017/5/18.
 */

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class SenderMsgService extends IntentService {

    private static final String TAG = "SenderMsgService";
    private final IBinder binder = new SendServiceBinder();

    public static final int MSG_TO_FILE_SENDER_UI = 0X88;   //消息：跳转到文件发送列表UI
    public static final int MSG_TO_SHOW_SCAN_RESULT = 0X99; //消息：更新扫描可连接Wifi网络的列表
    public static final int MSG_TO_CLOSE_SOCKET = 0X11;
    public static final int MSG_TO_START_SEND_MSG = 0X22;

    public SenderMsgService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    public SenderMsgService() {
        super(TAG);
        // TODO Auto-generated constructor stub
    }

    public class SendServiceBinder extends Binder {
        public SenderMsgService getService() {
            return SenderMsgService.this;
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
    }




    //0.发送 即将发送的文件列表 到文件接收方
    public Runnable sendFileInfo(final int serverPort, final InetAddress ipAddress){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    sendFileInfoListToFileReceiverWithUdp(serverPort, ipAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    SendInitAndToUI(ipAddress, serverPort);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 发送Init消息等到回复成功，开始跳转到发送页面
     *
     * @param targetIpAddr
     * @param serverPort
     * @throws Exception
     */
    public void SendInitAndToUI(InetAddress targetIpAddr, int serverPort) throws Exception {
        //1.发送 文件接收方 初始化信息
        byte[] receiveData = new byte[1024];
        byte[] sendData = null;
        sendData = Constant.MSG_FILE_RECEIVER_INIT.getBytes(BaseTransfer.UTF_8);
        DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, targetIpAddr, serverPort);
        Constant.mSendDatagramSocket.send(sendPacket);
        MLog.i(TAG, "Send Msg To FileReceiver######>>>" + Constant.MSG_FILE_RECEIVER_INIT);

//        sendFileInfoListToFileReceiverWithUdp(serverPort, ipAddress);


        //2.接收 文件接收方 初始化 反馈
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            Constant.mSendDatagramSocket.receive(receivePacket);
            String response = new String(receivePacket.getData(), BaseTransfer.UTF_8).trim();
            MLog.i(TAG, "Get the msg from FileReceiver######>>>" + response);
            if (response != null && response.equals(Constant.MSG_FILE_RECEIVER_INIT_SUCCESS)) {
                // 进入文件发送列表界面 （并且文件接收方进入文件接收列表界面）
                Message msg = new Message();
                msg.what = MSG_TO_FILE_SENDER_UI;
                _hd.sendMessage(msg);
            }
        }
    }

    /**
     * 发送即将发送的文件列表到文件接收方
     *
     * @param serverPort
     * @param ipAddress
     * @throws IOException
     */
    private void sendFileInfoListToFileReceiverWithUdp(int serverPort, InetAddress ipAddress) throws IOException {
        //1.1将发送的List<FileInfo> 发送给 文件接收方
        //如何将发送的数据列表封装成JSON
        Map<String, FileInfo> sendFileInfoMap = AppContext.getAppContext().getFileInfoMap();
        List<Map.Entry<String, FileInfo>> fileInfoMapList = new ArrayList<Map.Entry<String, FileInfo>>(sendFileInfoMap.entrySet());
        List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
        //排序
        Collections.sort(fileInfoMapList, Constant.DEFAULT_COMPARATOR);
        for (Map.Entry<String, FileInfo> entry : fileInfoMapList) {
            if (entry.getValue() != null) {
                FileInfo fileInfo = entry.getValue();
                String fileInfoStr = FileInfo.toJsonStr(fileInfo);
                DatagramPacket sendFileInfoListPacket =
                        new DatagramPacket(fileInfoStr.getBytes(), fileInfoStr.getBytes().length, ipAddress, serverPort);
                try {
                    Constant.mSendDatagramSocket.send(sendFileInfoListPacket);
                    MLog.i(TAG, "sendFileInfoListToFileReceiverWithUdp------>>>" + fileInfoStr + "=== Success!");
                } catch (Exception e) {
                    MLog.i(TAG, "sendFileInfoListToFileReceiverWithUdp------>>>" + fileInfoStr + "=== Failure!");
                }

            }
        }
    }

    /**
     * 关闭UDP Socket 流
     */
    public void closeSocket() {
        if (Constant.mSendDatagramSocket != null) {
            Constant.mSendDatagramSocket.disconnect();
            Constant.mSendDatagramSocket.close();
            Constant.mSendDatagramSocket = null;
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
                case MSG_TO_START_SEND_MSG:

                    new Thread(sendFileInfo(Constant.DEFAULT_SERVER_COM_PORT,Constant.friendIpPortInfo.getInetAddress())).start();
                default:
                    super.handleMessage(msg);
                    break;
            }

        }
    }
}