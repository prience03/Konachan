package com.ess.anime.wallpaper.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.io.File;
import java.util.List;

import androidx.core.content.FileProvider;

/**
 * 通过ActivityManager获取当前系统中的Activity与Service的运行状态
 * ActivityManager需要权限<uses-permission android:name="android.permission.GET_TASKS" />
 *
 * @author Zero
 */
public class SystemUtils {

    /**
     * 获取当前正在运行的Activity名称，Android 4.X(API 20)及以下完美支持，
     * Android 5.0(API 21)及以上只支持获取本app内的activity名称
     *
     * @param context 上下文
     * @return 当前正在运行的Activity名称
     */
    public static String getRunningActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//		if (Build.VERSION.SDK_INT < 22) {
        return am.getRunningTasks(1).get(0).topActivity.getClassName();
//		}else{
//		}
    }

    /**
     * 获取当前正在运行的应用包名，Android 4.X(API 20)及以下完美支持，
     * Android 5.0(API 21)及以上只支持获取本app内的activity和home界面
     *
     * @param context 上下文
     * @return 当前正在运行的应用包名
     */
    public static String getRunningAppPackageName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningTasks(1).get(0).topActivity.getPackageName();
    }

    /**
     * 判断当前运行在前台的是否为本应用
     *
     * @param context 上下文
     * @return 当前运行在前台的是否为本应用
     */
    public static boolean isAppRunningForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = activityManager.getRunningAppProcesses();
        // 枚举进程
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (info.processName.equals(context.getApplicationInfo().processName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断某Service是否正在运行
     *
     * @param context 上下文
     * @param clazz   目标Service类
     * @return 是否正在运行
     */
    public static boolean isServiceRunning(Context context, Class<? extends Service> clazz) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> services = am.getRunningServices(Integer.MAX_VALUE);
        for (RunningServiceInfo info : services) {
            String className = info.service.getClassName();
            if (clazz.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取手机可用内存大小
     *
     * @param context 上下文
     * @return 手机可用内存大小
     */
    public static long getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo outInfo = new MemoryInfo();
        am.getMemoryInfo(outInfo);
        return outInfo.availMem;
    }

    /**
     * 获取手机总内存大小
     *
     * @param context 上下文
     * @return 手机总内存大小
     */
    public static long getTotalMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo outInfo = new MemoryInfo();
        am.getMemoryInfo(outInfo);
        return outInfo.totalMem;
    }

    /**
     * 获取app版本号
     *
     * @param context 上下文
     * @return app版本号
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取app版本名
     *
     * @param context 上下文
     * @return app版本名
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 安装apk
     *
     * @param context 上下文
     * @param apkFile apk文件
     * @param start   是否直接启动安装
     * @return intent
     */
    public static Intent installApk(Context context, File apkFile, boolean start) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, getFileProviderAuthority(context), apkFile);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            }
            if (start) {
                context.startActivity(intent);
            }
            return intent;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getFileProviderAuthority(Context context) {
        return context.getPackageName() + ".fileprovider";
    }

    /**
     * 获取设备Android Id
     *
     * @param context 上下文
     * @return Android Id
     */
    public static String getAndroidId(Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 将字符串复制到剪贴板
     *
     * @param context 上下文
     * @param str     字符串
     */
    public static void setClipString(Context context, String str) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            ClipData clipData = ClipData.newPlainText("clip", str);
            manager.setPrimaryClip(clipData);
        }
    }

    /**
     * 获取当前剪贴板内容
     *
     * @param context 上下文
     * @return 剪贴板内容
     */
    public static String getFirstClipString(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null && manager.hasPrimaryClip()) {
            ClipData clipData = manager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence text = clipData.getItemAt(0).getText();
                return text == null ? "" : text.toString();
            }
        }
        return "";
    }

    public static boolean isActivityActive(Activity activity) {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }
}
