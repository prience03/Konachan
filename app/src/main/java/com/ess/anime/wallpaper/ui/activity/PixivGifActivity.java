package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerPixivGifDlAdapter;
import com.ess.anime.wallpaper.listener.OnTouchScaleListener;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifBean;
import com.ess.anime.wallpaper.pixiv.gif.PixivGifDlManager;
import com.ess.anime.wallpaper.ui.view.CustomDialog;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.Collections;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import nl.bravobit.ffmpeg.FFmpeg;

public class PixivGifActivity extends BaseActivity {

    public final static String TAG = PixivGifActivity.class.getName();

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.et_id)
    EditText mEtId;
    @BindView(R.id.rv_pixiv_gif)
    RecyclerView mRvPixivGif;

    @Override
    int layoutRes() {
        return R.layout.activity_pixiv_gif;
    }

    @Override
    void init(Bundle savedInstanceState) {
        initToolBarLayout();
        if (!FFmpeg.getInstance(this).isSupported()) {
            Toast.makeText(MyApp.getInstance(), R.string.not_support_ffmpeg, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        PermissionHelper.checkStoragePermissions(this, new PermissionHelper.RequestListener() {
            @Override
            public void onGranted() {
                initWhenPermissionGranted();
            }

            @Override
            public void onDenied() {
                finish();
            }
        });
    }

    private void initToolBarLayout() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> finish());
    }

    @OnClick(R.id.iv_clear_all)
    void clearAllFinished() {
        CustomDialog.showClearAllDownloadFinishedDialog(this, new CustomDialog.SimpleDialogActionListener() {
            @Override
            public void onPositive() {
                super.onPositive();
                PixivGifDlManager.getInstance().clearAllFinished();
            }
        });
    }

    @OnClick(R.id.iv_goto_collection)
    void gotoCollection() {
        startActivity(new Intent(this, CollectionActivity.class));
    }

    private void initWhenPermissionGranted() {
        initViews();
        initRecyclerPixivGif();
    }

    private void initViews() {
        findViewById(R.id.btn_download).setOnTouchListener(OnTouchScaleListener.DEFAULT);

        mEtId.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                startDownload();
                return true;
            }
            return false;
        });
    }

    private void initRecyclerPixivGif() {
        List<PixivGifBean> downloadList = PixivGifDlManager.getInstance().getDownloadList();
        Collections.reverse(downloadList);

        int span = Math.max(UIUtils.px2dp(this, UIUtils.getScreenWidth(this)) / 360, 1);
        mRvPixivGif.setLayoutManager(new GridLayoutManager(this, span));
        new RecyclerPixivGifDlAdapter(downloadList).bindToRecyclerView(mRvPixivGif);

        int spaceHor = UIUtils.dp2px(this, 5);
        int spaceVer = UIUtils.dp2px(this, 10);
        mRvPixivGif.addItemDecoration(new GridDividerItemDecoration(
                1, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));
    }

    @OnClick(R.id.btn_download)
    void startDownload() {
        String pixivId = mEtId.getText().toString();
        if (!TextUtils.isEmpty(pixivId)) {
            PixivGifDlManager.getInstance().execute(pixivId);
            mEtId.setText(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            mRvPixivGif.setAdapter(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRvPixivGif.setAdapter(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionHelper.REQ_CODE_PERMISSION) {
            // 进入系统设置界面请求权限后的回调
            if (PermissionHelper.hasPermissions(this, Permission.Group.STORAGE)) {
                initWhenPermissionGranted();
            } else {
                finish();
            }
        }
    }

}
