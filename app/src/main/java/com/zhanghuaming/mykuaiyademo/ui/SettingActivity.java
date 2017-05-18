package com.zhanghuaming.mykuaiyademo.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.zhanghuaming.mykuaiyademo.Constant;
import com.zhanghuaming.mykuaiyademo.R;
import com.zhanghuaming.mykuaiyademo.utils.NavigatorUtils;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by zhanghuaming on 2016/11/28.
 * Contact me zhanghuamingmyself@163.com
 */
public class SettingActivity extends AppCompatActivity {

    @Bind(R.id.et_name)
    EditText et_name;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.save)
    FloatingActionButton btn_save;
    String[] strArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);

        init();


        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constant.MYName = "ZHM"+et_name.getText();
                Snackbar.make(view, "已保存", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


    }

    private void init() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if(Constant.MYName!=null)
        et_name.setText(Constant.MYName.replace("ZHM",""));
    }

    public Context getContext(){
        return this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_client) {
            NavigatorUtils.toChooseFileUI(getContext());
            return true;
        }else if (id == R.id.action_server) {
            NavigatorUtils.toReceiverWaitingUI(getContext());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
