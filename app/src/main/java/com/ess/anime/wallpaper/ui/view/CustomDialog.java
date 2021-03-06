package com.ess.anime.wallpaper.ui.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.download.apk.ApkBean;
import com.ess.anime.wallpaper.download.apk.DownloadApkService;
import com.ess.anime.wallpaper.download.image.DownloadBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.model.helper.DocDataHelper;
import com.ess.anime.wallpaper.ui.activity.HyperlinkActivity;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.SystemUtils;
import com.ess.anime.wallpaper.website.WebsiteConfig;
import com.ess.anime.wallpaper.website.WebsiteManager;
import com.qmuiteam.qmui.util.QMUIDeviceHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;

public class CustomDialog extends MaterialDialog.Builder {

    public CustomDialog(@NonNull Context context) {
        super(context);
        this.titleColorRes(R.color.color_dialog_button)
                .contentColorRes(R.color.color_dialog_text)
                .positiveColorRes(R.color.color_dialog_button)
                .negativeColorRes(R.color.color_dialog_button)
                .neutralColorRes(R.color.color_dialog_button)
                .backgroundColorRes(R.color.color_dialog_bg)
                .itemsColorRes(R.color.color_dialog_text)
                .choiceWidgetColor(context.getResources().getColorStateList(R.color.dialog_widget_seletor));
    }

    /**
     * 提示图片已存在，询问是否重新下载
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showPromptToReloadImage(Context context, OnDialogActionListener listener) {
        MaterialDialog dialog = new CustomDialog(context)
                .content(R.string.dialog_reload_msg)
                .negativeText(R.string.dialog_reload_no)
                .positiveText(R.string.dialog_reload_yes)
                .onPositive((dialog1, which) -> listener.onPositive())
                .show();
    }

    /**
     * 删除收藏图片
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showDeleteCollectionDialog(Context context, String msg, OnDialogActionListener listener) {
        MaterialDialog dialog = new CustomDialog(context)
                .content(msg)
                .negativeText(R.string.dialog_delete_cancel)
                .positiveText(R.string.dialog_delete_sure)
                .onPositive((dialog1, which) -> listener.onPositive())
                .show();
    }

    /**
     * 删除下载列表项
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showDeleteWhenDownloadingItemDialog(Context context, OnDialogActionListener listener) {
        MaterialDialog dialog = new CustomDialog(context)
                .content(R.string.dialog_delete_downloading_item_msg)
                .negativeText(R.string.dialog_delete_cancel)
                .positiveText(R.string.dialog_delete_sure)
                .onPositive((dialog1, which) -> listener.onPositive())
                .show();
    }

    /**
     * 清空全部已完成下载项
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showClearAllDownloadFinishedDialog(Context context, OnDialogActionListener listener) {
        MaterialDialog dialog = new CustomDialog(context)
                .content(R.string.dialog_clear_all_download_finished_item_msg)
                .negativeText(R.string.dialog_clear_all_cancel)
                .positiveText(R.string.dialog_clear_all_sure)
                .onPositive((dialog1, which) -> listener.onPositive())
                .show();
    }

    /**
     * 清空全部搜索历史记录
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showClearAllSearchHistoryDialog(Context context, OnDialogActionListener listener) {
        MaterialDialog dialog = new CustomDialog(context)
                .content(R.string.dialog_clear_all_search_history_msg)
                .negativeText(R.string.dialog_clear_all_cancel)
                .positiveText(R.string.dialog_clear_all_sure)
                .onPositive((dialog1, which) -> listener.onPositive())
                .show();
    }

    /**
     * 显示标签类型说明文档
     *
     * @param context 上下文
     */
    public static void showTagTypeHelpDialog(Context context) {
        View view = View.inflate(context, R.layout.layout_dialog_scroll_text, null);
        TextView tvContent = view.findViewById(R.id.tv_content);
        tvContent.setText(DocDataHelper.getTagTypeHelpDoc(context));

        MaterialDialog dialog = new CustomDialog(context)
                .title(R.string.dialog_doc_tag_type_title)
                .customView(view, false)
                .positiveText(R.string.dialog_doc_sure)
                .show();
    }

    /**
     * 显示高级搜索说明文档
     *
     * @param context 上下文
     */
    public static void showAdvancedSearchHelpDialog(Context context) {
        View view = View.inflate(context, R.layout.layout_dialog_scroll_text, null);
        TextView tvContent = view.findViewById(R.id.tv_content);
        tvContent.setText(DocDataHelper.getAdvancedSearchDoc(context));

        MaterialDialog dialog = new CustomDialog(context)
                .title(R.string.dialog_doc_advanced_search_title)
                .customView(view, false)
                .positiveText(R.string.dialog_doc_sure)
                .show();
    }

    /**
     * 请求权限
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showRequestPermissionDialog(Context context, String title, String msg, OnDialogActionListener listener) {
        MaterialDialog dialog = new CustomDialog(context)
                .title(title)
                .content(msg)
                .negativeText(R.string.dialog_permission_rationale_deny)
                .positiveText(R.string.dialog_permission_rationale_grant)
                .canceledOnTouchOutside(false)
                .onNegative((dialog1, which) -> {
                    if (listener != null) {
                        listener.onNegative();
                    }
                })
                .onPositive((dialog12, which) -> {
                    if (listener != null) {
                        listener.onPositive();
                    }
                })
                .cancelListener(dialog13 -> {
                    if (listener != null) {
                        listener.onNegative();
                    }
                }).show();
    }

    /**
     * 切换网站
     *
     * @param context  上下文
     * @param listener 事件监听器
     */
    public static void showChangeBaseUrlDialog(Context context, OnDialogActionListener listener) {
        String baseUrl = WebsiteManager.getInstance().getWebsiteConfig().getBaseUrl();
        List<String> baseList = Arrays.asList(WebsiteConfig.BASE_URLS);
        int baseIndex = baseList.indexOf(baseUrl);

        MaterialDialog dialog = new CustomDialog(context)
                .title(R.string.dialog_change_base_url_title)
                .negativeText(R.string.dialog_change_base_url_cancel)
                .items(R.array.website_list_item)
                .itemsCallbackSingleChoice(baseIndex, (dialog1, itemView, which, text) -> {
                    if (which != baseIndex) {
                        WebsiteManager.getInstance().changeWebsite(baseList.get(which));
                    }
                    listener.onPositive();
                    return true;
                })
                .alwaysCallSingleChoiceCallback()
                .show();
    }

    /**
     * 选择尺寸下载图片
     *
     * @param context  上下文
     * @param itemList 三种尺寸详细数据
     * @param listener 事件监听器
     */
    public static void showChooseToDownloadDialog(Context context, List<DownloadBean> itemList, OnDialogActionListener listener) {
        MaterialDialog dialog = new CustomDialog(context)
                .title(R.string.save)
                .negativeText(R.string.dialog_download_cancel)
                .positiveText(R.string.dialog_download_sure)
                .items(itemList)
                .itemsCallbackMultiChoice(null, (dialog1, which, text) -> {
                    List<DownloadBean> chosenList = new ArrayList<>();
                    for (int index : which) {
                        chosenList.add(itemList.get(index));
                    }
                    listener.onDownloadChosen(chosenList);
                    return false;
                }).show();
    }

    /**
     * 版本更新提示
     *
     * @param context 上下文
     * @param apkBean 新版本信息
     */
    public static void showUpdateDialog(Context context, ApkBean apkBean) {
        String updateContent = DocDataHelper.isChinese() ? apkBean.updatedContentZh : apkBean.updatedContentEn;
        MaterialDialog dialog = new CustomDialog(context)
                .title(context.getString(R.string.dialog_update_title))
                .titleGravity(GravityEnum.CENTER)
                .content(context.getString(R.string.dialog_update_msg, apkBean.versionName,
                        FileUtils.computeFileSize(apkBean.apkSize), updateContent))
                .canceledOnTouchOutside(false)
                .negativeText(R.string.dialog_update_ignore)
                .positiveText(R.string.dialog_update_update)
                .onPositive((dialog1, which) -> {
                    File apkFile = new File(apkBean.localFilePath);
                    if (apkFile.exists()) {
                        SystemUtils.installApk(context, apkFile, true);
                    } else {
                        Intent intent = new Intent(context, DownloadApkService.class);
                        intent.putExtra(Constants.APK_BEAN, apkBean);
                        context.startService(intent);
                    }
                }).show();
    }

    /**
     * Feedback说明
     *
     * @param context 上下文
     */
    public static void showFeedbackDialog(Context context) {
        String email = "1018717197@qq.com";
        MaterialDialog dialog = new CustomDialog(context)
                .title(R.string.dialog_feedback_title)
                .content(context.getString(R.string.dialog_feedback_msg, email))
                .negativeText(R.string.dialog_feedback_cancel)
                .neutralText(R.string.dialog_feedback_neutral)
                .onNeutral((dialog12, which) -> {
                    String url = "https://github.com/EternalSoySauce/Konachan/issues";
                    HyperlinkActivity.launch(context, url);
                })
                .positiveText(R.string.dialog_feedback_sure)
                .onPositive((dialog1, which) -> {
                    Uri uri = Uri.parse("mailto:" + email);
                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "["
                            + SystemUtils.getVersionName(context)
                            + "] Feedback - K Anime Wallpaper");
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.feedback_title)));
                }).show();
    }

    /**
     * 网站功能说明
     *
     * @param context 上下文
     * @param title   title
     * @param msgRes  msg
     */
    public static void showWebsiteHelpDialog(Context context, CharSequence title, int msgRes) {
        MaterialDialog dialog = new CustomDialog(context)
                .title(title)
                .content(msgRes)
                .positiveText(R.string.dialog_doc_sure)
                .show();
    }

    private final static String NOT_SHOW_WALLPAPER_PROMPT_AGAIN = "NOT_SHOW_WALLPAPER_PROMPT_AGAIN";

    /**
     * 部分设备提示无法将自定义壁纸设置为锁屏
     *
     * @param context 上下文
     */
    public static void checkToShowCannotCustomLockscreenWallpaperDialog(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if ((!QMUIDeviceHelper.isMIUI() && !QMUIDeviceHelper.isHuawei())
                || preferences.getBoolean(NOT_SHOW_WALLPAPER_PROMPT_AGAIN, false)) {
            return;
        }

        MaterialDialog dialog = new CustomDialog(context)
                .content(R.string.dialog_cannot_wallpaper_lockscreen_msg)
                .positiveText(R.string.dialog_cannot_wallpaper_lockscreen_sure)
                .neutralText(R.string.dialog_cannot_wallpaper_lockscreen_neutral)
                .onNeutral((dialog1, which) -> {
                    preferences.edit().putBoolean(NOT_SHOW_WALLPAPER_PROMPT_AGAIN, true).apply();
                }).show();
    }

    public interface OnDialogActionListener {
        void onPositive();

        void onNegative();

        void onDownloadChosen(List<DownloadBean> chosenList);
    }

    public static class SimpleDialogActionListener implements OnDialogActionListener {

        @Override
        public void onPositive() {
        }

        @Override
        public void onNegative() {
        }

        @Override
        public void onDownloadChosen(List<DownloadBean> chosenList) {
        }
    }
}
