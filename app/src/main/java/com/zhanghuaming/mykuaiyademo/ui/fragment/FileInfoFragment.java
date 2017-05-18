package com.zhanghuaming.mykuaiyademo.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.zhanghuaming.mykuaiyademo.AppContext;
import com.zhanghuaming.mykuaiyademo.R;
import com.zhanghuaming.mykuaiyademo.core.entity.FileInfo;
import com.zhanghuaming.mykuaiyademo.core.utils.FileUtils;
import com.zhanghuaming.mykuaiyademo.core.utils.MLog;
import com.zhanghuaming.mykuaiyademo.core.utils.ToastUtils;
import com.zhanghuaming.mykuaiyademo.ui.ChooseFileActivity;
import com.zhanghuaming.mykuaiyademo.ui.adapter.FileInfoAdapter;
import com.zhanghuaming.mykuaiyademo.utils.AnimationUtils;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Apk列表Fragment
 * <p>
 * Created by zhanghuaming on 2016/11/24.
 * Contact me zhanghuamingmyself@163.com
 */
public class FileInfoFragment extends Fragment {

    @Bind(R.id.gv)
    GridView gv;
    @Bind(R.id.pb)
    ProgressBar pb;
    private static final String TAG = FileInfoFragment.class.getSimpleName();
    private int mType = FileInfo.TYPE_APK;
    private List<FileInfo> mFileInfoList;
    private FileInfoAdapter mFileInfoAdapter;

    @SuppressLint("ValidFragment")
    public FileInfoFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public FileInfoFragment(int type) {
        super();
        this.mType = type;
    }

    public static FileInfoFragment newInstance(int type) {
        FileInfoFragment fragment = new FileInfoFragment(type);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_apk, container, false);
        // Inflate the layout for this fragment
        ButterKnife.bind(this, rootView);

        if (mType == FileInfo.TYPE_APK) { //应用
            gv.setNumColumns(4);
        } else if (mType == FileInfo.TYPE_JPG) { //图片
            gv.setNumColumns(3);
        } else if (mType == FileInfo.TYPE_MP3) { //音乐
            gv.setNumColumns(1);
        } else if (mType == FileInfo.TYPE_MP4) { //视频
            gv.setNumColumns(1);
        } else if (mType == FileInfo.TYPE_OTHER) {
            gv.setNumColumns(1);
        }

        //Android6.0 requires android.permission.READ_EXTERNAL_STORAGE
        init();//初始化界面

        return rootView;
    }
    private String LastDir = Environment.getExternalStorageDirectory().toString();
    private void init() {
        if (mType == FileInfo.TYPE_APK) {
            new GetFileInfoListTask(getContext(), FileInfo.TYPE_APK).executeOnExecutor(AppContext.MAIN_EXECUTOR);
        } else if (mType == FileInfo.TYPE_JPG) {
            new GetFileInfoListTask(getContext(), FileInfo.TYPE_JPG).executeOnExecutor(AppContext.MAIN_EXECUTOR);
        } else if (mType == FileInfo.TYPE_MP3) {
            new GetFileInfoListTask(getContext(), FileInfo.TYPE_MP3).executeOnExecutor(AppContext.MAIN_EXECUTOR);
        } else if (mType == FileInfo.TYPE_MP4) {
            new GetFileInfoListTask(getContext(), FileInfo.TYPE_MP4).executeOnExecutor(AppContext.MAIN_EXECUTOR);
        } else if (mType == FileInfo.TYPE_OTHER || mType == FileInfo.TYPE_DIR) {
            new GetFileInfoListTask(getContext(), FileInfo.TYPE_OTHER).executeOnExecutor(AppContext.MAIN_EXECUTOR);
        }

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileInfo fileInfo = mFileInfoList.get(position);
                if (fileInfo.getIsDir() && mType == FileInfo.TYPE_OTHER ) {
                    Context context = getContext();

                    mFileInfoList.clear();
                    mFileInfoList.addAll(FileUtils.getOneDirFiles(context, fileInfo.getFilePath()));
                    mFileInfoList = FileUtils.getDetailFileInfos(context, mFileInfoList, FileInfo.TYPE_OTHER);

                    if(!fileInfo.getFilePath().equals(Environment.getExternalStorageDirectory().toString())) {
                        String s = new String(fileInfo.getFilePath());
                        String[] ss = s.split("/");
                        LastDir = s.replace(ss[ss.length - 1], "");
                        File l = new File(LastDir);
                        FileInfo lf = new FileInfo(l.getPath(), l.length(), true);
                        lf.setBitmap(BitmapFactory.decodeResource(context.getResources(), R.mipmap.back));
                        lf.setIsDir(true);
                        mFileInfoList.add(lf);
                        updateFileInfoAdapter();
                    }
                } else {
                    if (AppContext.getAppContext().isExist(fileInfo)) {
                        AppContext.getAppContext().delFileInfo(fileInfo);
                        updateSelectedView();
                    } else {
                        //1.添加任务
                        AppContext.getAppContext().addFileInfo(fileInfo);
                        //2.添加任务 动画
                        View startView = null;
                        View targetView = null;

                        startView = view.findViewById(R.id.iv_shortcut);
                        if (getActivity() != null && (getActivity() instanceof ChooseFileActivity)) {
                            ChooseFileActivity chooseFileActivity = (ChooseFileActivity) getActivity();
                            targetView = chooseFileActivity.getSelectedView();
                        }
                        AnimationUtils.setAddTaskAnimation(getActivity(), startView, targetView, null);
                    }
                }

                mFileInfoAdapter.notifyDataSetChanged();
            }
        }
    );
}

    @Override
    public void onResume() {
        updateFileInfoAdapter();
        super.onResume();
    }

    /**
     * 更新FileInfoAdapter
     */
    public void updateFileInfoAdapter() {
        if (mFileInfoAdapter != null) {
            mFileInfoAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 更新ChoooseActivity选中View
     */
    private void updateSelectedView() {
        if (getActivity() != null && (getActivity() instanceof ChooseFileActivity)) {
            ChooseFileActivity chooseFileActivity = (ChooseFileActivity) getActivity();
            chooseFileActivity.getSelectedView();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * 显示进度
     */
    public void showProgressBar() {
        if (pb != null) {
            pb.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏进度
     */
    public void hideProgressBar() {
        if (pb != null && pb.isShown()) {
            pb.setVisibility(View.GONE);
        }
    }


/**
 * 获取ApkInfo列表任务
 */
class GetFileInfoListTask extends AsyncTask<String, Integer, List<FileInfo>> {
    Context sContext = null;
    int sType = FileInfo.TYPE_APK;


    public GetFileInfoListTask(Context sContext, int type) {
        this.sContext = sContext;
        this.sType = type;
    }

    @Override
    protected void onPreExecute() {
        showProgressBar();
        super.onPreExecute();
    }

    @Override
    protected List doInBackground(String... params) {
        //FileUtils.getSpecificTypeFiles 只获取FileInfo的属性 filePath与size
        if (sType == FileInfo.TYPE_APK) {
            mFileInfoList = FileUtils.getSpecificTypeFiles(sContext, new String[]{FileInfo.EXTEND_APK});
            mFileInfoList = FileUtils.getDetailFileInfos(sContext, mFileInfoList, FileInfo.TYPE_APK);
        } else if (sType == FileInfo.TYPE_JPG) {
            mFileInfoList = FileUtils.getSpecificTypeFiles(sContext, new String[]{FileInfo.EXTEND_JPG, FileInfo.EXTEND_JPEG});
            mFileInfoList = FileUtils.getDetailFileInfos(sContext, mFileInfoList, FileInfo.TYPE_JPG);
        } else if (sType == FileInfo.TYPE_MP3) {
            mFileInfoList = FileUtils.getSpecificTypeFiles(sContext, new String[]{FileInfo.EXTEND_MP3});
            mFileInfoList = FileUtils.getDetailFileInfos(sContext, mFileInfoList, FileInfo.TYPE_MP3);
        } else if (sType == FileInfo.TYPE_MP4) {
            mFileInfoList = FileUtils.getSpecificTypeFiles(sContext, new String[]{FileInfo.EXTEND_MP4});
            mFileInfoList = FileUtils.getDetailFileInfos(sContext, mFileInfoList, FileInfo.TYPE_MP4);
        } else if (sType == FileUtils.TYPE_OTHER) {
            mFileInfoList = FileUtils.getOneDirFiles(sContext, Environment.getExternalStorageDirectory().toString());
            mFileInfoList = FileUtils.getDetailFileInfos(sContext, mFileInfoList, FileInfo.TYPE_OTHER);
        }


        return mFileInfoList;
    }


    @Override
    protected void onPostExecute(List<FileInfo> list) {
        hideProgressBar();
        if (mFileInfoList != null && mFileInfoList.size() > 0) {
            if (mType == FileInfo.TYPE_APK) { //应用
                mFileInfoAdapter = new FileInfoAdapter(sContext, mFileInfoList, FileInfo.TYPE_APK);
                gv.setAdapter(mFileInfoAdapter);
            } else if (mType == FileInfo.TYPE_JPG) { //图片
                mFileInfoAdapter = new FileInfoAdapter(sContext, mFileInfoList, FileInfo.TYPE_JPG);
                gv.setAdapter(mFileInfoAdapter);
            } else if (mType == FileInfo.TYPE_MP3) { //音乐
                mFileInfoAdapter = new FileInfoAdapter(sContext, mFileInfoList, FileInfo.TYPE_MP3);
                gv.setAdapter(mFileInfoAdapter);
            } else if (mType == FileInfo.TYPE_MP4) { //视频
                mFileInfoAdapter = new FileInfoAdapter(sContext, mFileInfoList, FileInfo.TYPE_MP4);
                gv.setAdapter(mFileInfoAdapter);
            } else if (mType == FileInfo.TYPE_OTHER) {
                mFileInfoAdapter = new FileInfoAdapter(sContext, mFileInfoList, FileInfo.TYPE_OTHER);
                gv.setAdapter(mFileInfoAdapter);
            }
        } else {
            ToastUtils.show(sContext, sContext.getResources().getString(R.string.tip_has_no_apk_info));
        }
    }
}

}
