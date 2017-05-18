package com.zhanghuaming.mykuaiyademo.utils;

import android.net.wifi.ScanResult;

import com.zhanghuaming.mykuaiyademo.Constant;
import com.zhanghuaming.mykuaiyademo.core.utils.MLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanghuaming on 2016/11/28.
 * Contact me zhanghuamingmyself@163.com
 */
public class ListUtils {

    private static final String TAG = "ListUtils";
    public static final String NO_PASSWORD = "[ESS]";
    public static final String NO_PASSWORD_WPS = "[WPS][ESS]";

    /**
     * 过滤有密码的Wifi扫描结果集合
     * @param scanResultList
     * @return
     */
    public static List<ScanResult> filterWithNoPassword(List<ScanResult> scanResultList){
        if(scanResultList == null || scanResultList.size() == 0){
            return scanResultList;
        }

        List<ScanResult> resultList = new ArrayList<>();
        for(ScanResult scanResult : scanResultList){
            if(scanResult.capabilities != null && scanResult.capabilities.equals(NO_PASSWORD) || scanResult.capabilities != null && scanResult.capabilities.equals(NO_PASSWORD_WPS)){
                MLog.e(TAG,"scan Wifi SSID id -----"+scanResult.SSID);
                if(scanResult.SSID.startsWith(Constant.MYName)) {

                    resultList.add(scanResult);
                }
            }
        }

        return resultList;
    }
}
