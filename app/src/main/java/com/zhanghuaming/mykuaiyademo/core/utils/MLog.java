package com.zhanghuaming.mykuaiyademo.core.utils;

import android.util.Log;

/**
 * Created by zhanghuaming on 2016/11/10.
 * Contact me zhanghuamingmyself@163.com
 */
public final class MLog {

    public static boolean DEBUG = true;

    public static void v(String tag, String msg){
        if(!DEBUG){
            return;
        }
        Log.v(tag, msg);
    }

    public static void d(String tag, String msg){
        if(!DEBUG){
            return;
        }
        Log.d(tag, msg);
    }
    public static void i(String tag, String msg){
        if(!DEBUG){
            return;
        }
        Log.i(tag, msg);
    }

    public static void w(String tag, String msg){
        if(!DEBUG){
            return;
        }
        Log.w(tag, msg);
    }

    public static void e(String tag, String msg){
        if(!DEBUG){
            return;
        }
        Log.e(tag, msg);
    }


}
