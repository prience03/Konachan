package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerWebviewMoreAdapter;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.ui.view.LollipopFixedWebView;
import com.ess.anime.wallpaper.ui.view.LongClickWebView;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.jiang.android.indicatordialog.IndicatorBuilder;
import com.jiang.android.indicatordialog.IndicatorDialog;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebChromeClient;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.Arrays;
import java.util.List;

import butterknife.OnClick;

public abstract class BaseWebActivity extends BaseActivity {

    AgentWeb mAgentWeb;
    private IndicatorDialog mPopup;

    abstract int titleRes();

    abstract String webUrl();

    @OnClick(R.id.iv_help)
    abstract void showHelpDialog();

    @Override
    int layoutRes() {
        return R.layout.activity_web;
    }

    @Override
    void init(Bundle savedInstanceState) {
        initToolBarLayout();
        initWebView();
        initListPopupWindow();
        PermissionHelper.checkStoragePermissions(this, new PermissionHelper.SimpleRequestListener() {
            @Override
            public void onDenied() {
                finish();
            }
        });
    }

    private void initToolBarLayout() {
        Toolbar toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitle(titleRes());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    void initWebView() {
        LongClickWebView webView = new LongClickWebView(this);
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(findViewById(R.id.layout_web_view),
                        new FrameLayout.LayoutParams(-1, -1))
                .useDefaultIndicator(ResourcesCompat.getColor(getResources(), R.color.color_text_selected, null))
                // 5.x机器的webview会出现资源Resources$NotFoundException错误，需对此进行适配
                .setWebView(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ? new LollipopFixedWebView(this) : null)
                .setMainFrameErrorView(View.inflate(this, R.layout.layout_webview_error, null))
                .interceptUnkownUrl()
                .setWebChromeClient(new WebChromeClient() {
                    @Override
                    public Bitmap getDefaultVideoPoster() {
                        Bitmap bitmap = super.getDefaultVideoPoster();
                        if (bitmap == null) {
                            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                        }
                        return bitmap;
                    }
                })
                .setWebView(webView)
                .createAgentWeb()
                .ready()
                .go(webUrl());

        webView.init();
    }

    private void initListPopupWindow() {
        List<String> searchModeList = Arrays.asList(getResources().getStringArray(R.array.spinner_list_item_webview_more));
        RecyclerWebviewMoreAdapter adapter = new RecyclerWebviewMoreAdapter(searchModeList);
        adapter.setOnItemClickListener((adapter1, view, position) -> {
            WebView webView = mAgentWeb.getWebCreator().getWebView();
            String title = webView.getTitle();
            String url = webView.getUrl();
            switch (position) {
                case 0: // 复制链接
                    ComponentUtils.setClipString(this, url);
                    Toast.makeText(this, R.string.copied_link, Toast.LENGTH_SHORT).show();
                    break;

                case 1: // 分享链接
                    String share = url;
                    if (!TextUtils.isEmpty(title)) {
                        share = title + "\n" + url;
                    }
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, share);
                    shareIntent.setType("text/plain");
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
                    break;

                case 2: // 用浏览器打开
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(Intent.createChooser(intent, getString(R.string.browse_title)));
                    break;

                case 3: // 打开我的收藏
                    startActivity(new Intent(this, CollectionActivity.class));
                    break;
            }
            mPopup.dismiss();
        });

        mPopup = new IndicatorBuilder(this)
                .width(computePopupItemMaxWidth(adapter))
                .height(-1)
                .bgColor(getResources().getColor(R.color.colorPrimary))
                .dimEnabled(false)
                .gravity(IndicatorBuilder.GRAVITY_RIGHT)
                .ArrowDirection(IndicatorBuilder.TOP)
                .ArrowRectage(0.86f)
                .radius(8)
                .layoutManager(new LinearLayoutManager(this))
                .adapter(adapter)
                .create();
        mPopup.setCanceledOnTouchOutside(true);
        mPopup.getDialog().setOnShowListener(dialog -> UIUtils.setBackgroundAlpha(this, 0.4f));
        mPopup.getDialog().setOnDismissListener(dialog -> UIUtils.setBackgroundAlpha(this, 1f));
    }

    // 使弹窗自适应文字宽度
    private int computePopupItemMaxWidth(RecyclerWebviewMoreAdapter adapter) {
        float maxWidth = 0;
        View layout = View.inflate(this, R.layout.recyclerview_item_popup_webview_more, null);
        TextView tv = layout.findViewById(R.id.tv_function);
        TextPaint paint = tv.getPaint();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            String item = adapter.getItem(i);
            maxWidth = Math.max(maxWidth, paint.measureText(item));
        }
        maxWidth += tv.getPaddingStart() + tv.getPaddingEnd();
        return (int) maxWidth;
    }

    @OnClick(R.id.iv_more)
    void showMore(View view) {
        mPopup.show(view);
    }

    @Override
    protected void onPause() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onResume();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mAgentWeb == null || !mAgentWeb.back()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionHelper.REQ_CODE_PERMISSION) {
            // 进入系统设置界面请求权限后的回调
            if (!PermissionHelper.hasPermissions(this, Permission.Group.STORAGE)) {
                finish();
            }
        }
    }

}
