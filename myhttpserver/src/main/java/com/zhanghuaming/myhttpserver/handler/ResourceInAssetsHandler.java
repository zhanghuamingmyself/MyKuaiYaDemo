package com.zhanghuaming.myhttpserver.handler;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


import com.zhanghuaming.myhttpserver.handler.itf.IResourceUriHandler;
import com.zhanghuaming.myhttpserver.server.HttpContext;
import com.zhanghuaming.myhttpserver.util.FileManager;
import com.zhanghuaming.myhttpserver.util.StreamToolkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * 静态的资源
 * http://localhost:8088/static/client.html
 */
public class ResourceInAssetsHandler implements IResourceUriHandler {

	private static final String TAG = "ResourceInAssetsHandler";
	private String acceptPrefix = "/static/";
	private Context context;
	
	public ResourceInAssetsHandler(Context context) {
		this.context=context;
	}

	@Override
	public boolean accept(String uri) {
		return uri.startsWith(acceptPrefix);
	}

	@Override
	public void handler(String uri, HttpContext httpContext) {
		StringBuffer path = new StringBuffer(Environment.getExternalStorageDirectory().getPath() + "/serverpath/");
		int startIndex=acceptPrefix.length();
		String assetsPath=uri.substring(startIndex);
		path.append(assetsPath);

		Log.e(TAG, "assetsPath:"+assetsPath+"Loca is -----"+path.toString());
		try {
			InputStream fis = FileManager.getFile(path.toString());
		    byte[] raw= StreamToolkit.readRawFromStream(fis);
		    fis.close();
		    OutputStream nos=httpContext.getUnderlySocket().getOutputStream();
		    PrintStream printer = new PrintStream(nos);
		    printer.write(raw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
