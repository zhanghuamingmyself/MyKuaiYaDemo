package com.zhanghuaming.mykuaiyademo.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhanghuaming.mykuaiyademo.Constant;

import com.zhanghuaming.mykuaiyademo.R;
import com.zhanghuaming.mykuaiyademo.common.BaseActivity;
import com.zhanghuaming.mykuaiyademo.core.utils.FileUtils;
import com.zhanghuaming.mykuaiyademo.core.utils.MLog;
import com.zhanghuaming.mykuaiyademo.core.utils.TextUtils;
import com.zhanghuaming.mykuaiyademo.core.utils.ToastUtils;
import com.zhanghuaming.mykuaiyademo.ui.view.MyScrollView;
import com.zhanghuaming.mykuaiyademo.utils.NavigatorUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = HomeActivity.class.getSimpleName();


    /**
     * 左右两大块 UI
     */
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view)
    NavigationView mNavigationView;

    TextView tv_name;

    /**
     * top bar 相关UI
     */

    @Bind(R.id.tv_title)
    TextView tv_title;

    @Bind(R.id.btn_set)
    Button btn_set;
    @Bind(R.id.btn_http)
    Button btn_http;

    /**
     * 其他UI
     */
    @Bind(R.id.msv_content)
    MyScrollView mScrollView;
    @Bind(R.id.ll_main)
    LinearLayout ll_main;
    @Bind(R.id.btn_send_big)
    Button btn_send_big;
    @Bind(R.id.btn_receive_big)
    Button btn_receive_big;

    @Bind(R.id.rl_device)
    RelativeLayout rl_device;
    @Bind(R.id.tv_device_desc)
    TextView tv_device_desc;
    @Bind(R.id.rl_file)
    RelativeLayout rl_file;
    @Bind(R.id.tv_file_desc)
    TextView tv_file_desc;
    @Bind(R.id.rl_storage)
    RelativeLayout rl_storage;
    @Bind(R.id.tv_storage_desc)
    TextView tv_storage_desc;



    //大的我要发送和我要接受按钮的LinearLayout的高度
    int mContentHeight = 0;


    //
    boolean mIsExist = false;
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ButterKnife.bind(this);


        //Android6.0 requires android.permission.READ_EXTERNAL_STORAGE
        //TODO
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_FILE);
        }else{
            //初始化
            init();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refreshNavigation();
    }

    @Override
    protected void onResume() {
        updateBottomData();
        super.onResume();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_WRITE_FILE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //初始化
                init();
            } else {
                // Permission Denied
                ToastUtils.show(this, getResources().getString(R.string.tip_permission_denied_and_not_send_file));
                finish();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 初始化
     */
    private void init() {

        initNavigation();
        updateBottomData();

    }

    /**
     * 初始化侧边栏
     */
    public void initNavigation()
    {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);

        refreshNavigation();
    }

    private void refreshNavigation() {
        //设置设备名称
        String device = Constant.MYName;
        try{//设置左边抽屉的设备名称
            tv_name = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.tv_name);
            tv_name.setText(device);
        }catch(Exception e){
            //maybe occur some exception
        }
    }

    /**
     * 更新底部 设备数，文件数，节省流量数的数据
     */
    private void updateBottomData(){
        //TODO 设备数的更新
        //TODO 文件数的更新
        tv_file_desc.setText(String.valueOf(FileUtils.getReceiveFileCount()));
        //TODO 节省流量数的更新
        tv_storage_desc.setText(String.valueOf(FileUtils.getReceiveFileListTotalLength()));

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
//                super.onBackPressed();
                if(mIsExist){
                    this.finish();
                }else{
                    ToastUtils.show(getContext(), getContext().getResources().getString(R.string.tip_call_back_agin_and_exist)
                                        .replace("{appName}", getContext().getResources().getString(R.string.app_name)));
                    mIsExist = true;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIsExist = false;
                        }
                    }, 2 * 1000);

                }

            }
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if(id == R.id.nav_about){
            MLog.i(TAG, "R.id.nav_about------>>> click");
            showAboutMeDialog();
        }else if(id == R.id.nav_web_transfer){
            MLog.i(TAG, "R.id.nav_web_transfer------>>> click");
            NavigatorUtils.toChooseFileUI(getContext(), true);
        }else{
            ToastUtils.show(getContext(), getResources().getString(R.string.tip_next_version_update));
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @OnClick({R.id.btn_send_big, R.id.btn_receive_big,
            R.id.btn_set, R.id.rl_file, R.id.rl_storage,R.id.rl_device ,R.id.btn_http })
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.btn_send:
            case R.id.btn_send_big: {
                NavigatorUtils.toChooseFileUI(getContext());
                break;
            }

            case R.id.btn_receive_big: {
                NavigatorUtils.toReceiverWaitingUI(getContext());
                break;
            }

            case R.id.rl_device:

                break;
            case R.id.rl_file:

                break;
            case R.id.rl_storage: {
                NavigatorUtils.toSystemFileChooser(getContext());
                break;
            }
            case R.id.btn_set:
                ToastUtils.show(this,"just a test");
                NavigatorUtils.toSettingUI(getContext());
                break;
            case R.id.btn_http:
                ToastUtils.show(this,"just a test");
                NavigatorUtils.toHTTPServerUI(getContext());
                break;
        }
    }



    /**
     * 显示对话框
     */
    private void showAboutMeDialog(){
        View contentView = View.inflate(getContext(), R.layout.view_about_me, null);
        contentView.findViewById(R.id.tv_github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toProject();
            }
        });
        new AlertDialog.Builder(getContext())
                .setTitle(getResources().getString(R.string.title_about_me))
                .setView(contentView)
                .setPositiveButton(getResources().getString(R.string.str_weiguan), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toProject();
                    }
                })
                .create()
                .show();
    }

    /**
     * 跳转到项目
     */
    private void toProject() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(Constant.GITHUB_PROJECT_SITE);
        intent.setData(uri);
        getContext().startActivity(intent);
    }
}
