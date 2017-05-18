package com.zhanghuaming.mykuaiyademo.core.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;

import com.zhanghuaming.mykuaiyademo.core.utils.MLog;

/**
 * Created by zhang on 2017/5/18.
 */

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class SenderService extends IntentService {

    private static final String TAG ="SenderService";
    private final IBinder binder = new LocalBinder();

    public SenderService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }
    public SenderService() {
        super(TAG);
        // TODO Auto-generated constructor stub
    }

    public class LocalBinder extends Binder {
        public SenderService getService()
        {
            return SenderService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return binder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

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


}