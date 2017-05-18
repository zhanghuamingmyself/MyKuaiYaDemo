package com.zhanghuaming.myhttpserver.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhang on 2017/5/10.
 */

public class FileManager {
    String filePath ;
    public static InputStream getFile(String filePath)
    {
        if(filePath == null)
        {
            return  null;
        }
        try {
            FileInputStream file = new FileInputStream(filePath);
            byte[] bytes = new byte[10240];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream inputStream = null;
            int n= -1;
            while((n = file.read(bytes)) != -1)
            {
                baos.write(bytes,0,n);
            }
            byte[] aArray =baos.toByteArray();
            inputStream = new ByteArrayInputStream(aArray);
            baos.close();
            file.close();
            return inputStream;
        }catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
