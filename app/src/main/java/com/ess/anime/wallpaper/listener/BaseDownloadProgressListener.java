package com.ess.anime.wallpaper.listener;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.utils.FileUtils;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.body.ProgressInfo;

public abstract class BaseDownloadProgressListener<T> implements ProgressListener {

    private static final String NOTIFY_CHANNEL_ID = "notification";
    private static final String NOTIFY_CHANNEL_NAME = "notification";

    Context mContext;
    String mFileAvailable;

    NotificationManager mNotifyManager;
    Notification.Builder mNotifyBuilder;
    int mNotifyId;
    PendingIntent mReloadIntent;
    PendingIntent mOperateIntent;

    BaseDownloadProgressListener(Context context, T data, Intent intent) {
        setData(data);
        mContext = context;
        createNotifyChannel();
        prepareNotification();
        createReloadPendingIntent(intent);
        createOperatePendingIntent();
    }

    abstract void setData(T data);

    private void createNotifyChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFY_CHANNEL_ID,
                    NOTIFY_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            NotificationManager notifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notifyManager.createNotificationChannel(channel);
        }
    }

    public void prepareNotification() {
        if (mNotifyBuilder == null) {
            mFileAvailable = FileUtils.computeFileSize(getTotalFileSize());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotifyBuilder = new Notification.Builder(mContext, NOTIFY_CHANNEL_ID);
            } else {
                mNotifyBuilder = new Notification.Builder(mContext);
            }
            mNotifyId = (int) System.currentTimeMillis();
            mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

            String title = getNotifyTitle();
            String ticker = mContext.getString(R.string.download_started, title);
            mNotifyBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher));
            mNotifyBuilder.setTicker(ticker);
            mNotifyBuilder.setContentTitle(title);
            mNotifyBuilder.setPriority(Notification.PRIORITY_MAX);
        }
        mNotifyBuilder.setSmallIcon(R.drawable.ic_notification_download);
        mNotifyBuilder.setContentText("0B / " + mFileAvailable);
        mNotifyBuilder.setProgress(100, 0, false);
        mNotifyBuilder.setOngoing(true);
        mNotifyBuilder.setContentIntent(null);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
    }

    abstract long getTotalFileSize();

    abstract String getNotifyTitle();

    @Override
    public void onProgress(ProgressInfo progressInfo) {
        int progress = progressInfo.getPercent();
        mNotifyBuilder.setProgress(100, progress, false);
        String content = FileUtils.computeFileSize(progressInfo.getCurrentbytes())
                + " / " + mFileAvailable;
        mNotifyBuilder.setContentText(content);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());

        if (progressInfo.isFinish() && autoPerformFinish()) {
            // 下载完成
            performFinish();
        }
    }

    abstract boolean autoPerformFinish();

    @Override
    public void onError(long id, Exception e) {
        mNotifyBuilder.setSmallIcon(R.drawable.ic_notification_download_failed);
        mNotifyBuilder.setContentText(mContext.getString(R.string.download_failed));
        mNotifyBuilder.setOngoing(false);
        mNotifyBuilder.setContentIntent(mReloadIntent);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
    }

    private void createReloadPendingIntent(Intent intent) {
        Intent reloadIntent = new Intent(mContext, getClassToReload());
        reloadIntent.putExtras(intent);
        mReloadIntent = PendingIntent.getService(mContext, mNotifyId,
                reloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    abstract Class<?> getClassToReload();

    abstract void createOperatePendingIntent();

    public void performFinish() {
        String finish = mContext.getString(R.string.download_finished, mFileAvailable);
        mNotifyBuilder.setProgress(100, 100, false);
        mNotifyBuilder.setSmallIcon(R.drawable.ic_notification_download_succeed);
        mNotifyBuilder.setContentText(finish);
        mNotifyBuilder.setOngoing(false);
        mNotifyBuilder.setContentIntent(mOperateIntent);
        mNotifyManager.notify(mNotifyId, mNotifyBuilder.build());
    }
}