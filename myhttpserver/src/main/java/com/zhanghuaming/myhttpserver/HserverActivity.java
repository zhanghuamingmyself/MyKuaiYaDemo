package com.zhanghuaming.myhttpserver;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.zhanghuaming.myhttpserver.config.WebConfiguration;
import com.zhanghuaming.myhttpserver.handler.PostDataHandler;
import com.zhanghuaming.myhttpserver.handler.ResourceInAssetsHandler;
import com.zhanghuaming.myhttpserver.handler.UploadImageHandler;
import com.zhanghuaming.myhttpserver.server.SimpleHttpServer;
import com.zhanghuaming.myhttpserver.util.IPUtils;

import org.json.JSONObject;

public class HserverActivity extends Activity {

    private static final String TAG = "HserverActivity";
    private Button btn_operator;
    private TextView username;
    private TextView password;
    private TextView tv_ipv4;
    private ImageView mImageView;

    private SimpleHttpServer shs;

    private boolean isServerRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hserver);
        username = (TextView) findViewById(R.id.username);
        password = (TextView) findViewById(R.id.password);
        tv_ipv4 = (TextView)findViewById(R.id.ipv4);
        showIpv4();
        btn_operator = (Button) findViewById(R.id.btn_operator);

        WebConfiguration webConfiguration = new WebConfiguration();
        webConfiguration.setPort(8088);
        webConfiguration.setMaxParallels(50);
        shs = new SimpleHttpServer(webConfiguration);
        shs.registerResourceHandler(new ResourceInAssetsHandler(this));
        shs.registerResourceHandler(new UploadImageHandler() {

            @Override
            protected void onImageLoaded(String path) {
                showImage(path);
            }
        });
        shs.registerResourceHandler(new PostDataHandler() {

            @Override
            public String showRequestDatas(JSONObject json) {
                return showDatas(json);
            }
        });

        btn_operator.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isServerRun) {
                    isServerRun = false;
                    btn_operator.setText("启动服务");
                    shs.stopAsync();
                } else {
                    isServerRun = true;
                    btn_operator.setText("关闭服务");
                    shs.startAsync();
                }
            }
        });
    }

    private void showIpv4() {
        IPUtils ip = new IPUtils();
        tv_ipv4.setText(ip.getIpAddress(HserverActivity.this));
    }

    protected void showImage(final String path) {
        Log.d("spy", "showImage:"+path);
        // UI线程
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mImageView = (ImageView) findViewById(R.id.iv_main);
                Bitmap bm = BitmapFactory.decodeFile(path);
                mImageView.setImageBitmap(bm);
                Toast.makeText(HserverActivity.this, "upload success!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected String showDatas(final JSONObject jsonStr) {
        final String[] result = { "faile" };
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,"json get name ---"+jsonStr.optString("username", "username"));
                username.setText(jsonStr.optString("username", "username"));
                password.setText(jsonStr.optString("password", "password"));
                result[0] = "success";
            }
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result[0];
    }

    @Override
    protected void onDestroy() {
        shs.stopAsync();
        super.onDestroy();
    }

}
