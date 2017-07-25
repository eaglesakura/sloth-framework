package com.eaglesakura.sloth.view.builder;

import com.eaglesakura.android.util.DrawableUtil;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.lambda.Action1;
import com.eaglesakura.sloth.view.SupportNotification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

/**
 * 通知バーの生成をサポートする
 */
public class NotificationBuilder {
    final Context mContext;

    NotificationCompat.Builder mBuilder;

    public static final String CHANNEL_DEFAULT = "sloth_default";

    NotificationBuilder(Context context, String channelId) {
        mContext = context;
        mBuilder = new NotificationCompat.Builder(mContext, channelId);
        mBuilder.setWhen(System.currentTimeMillis());
    }

    public NotificationBuilder icon(@DrawableRes int resId) {
        Bitmap image;
        {
            Drawable drawable = DrawableUtil.getDrawable(mContext, resId);
            if (drawable instanceof BitmapDrawable) {
                image = ((BitmapDrawable) drawable).getBitmap();
            } else {
                image = ImageUtil.toBitmap(drawable, Math.max(drawable.getMinimumWidth(), drawable.getMinimumHeight()));
            }
        }
        mBuilder.setLargeIcon(image);
        mBuilder.setSmallIcon(resId);
        return this;
    }

    /**
     * クリック時にBroadcastを行わせる
     */
    public NotificationBuilder clickBroadcast(Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0x00, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        return this;
    }

    /**
     * クリック時にBroadcastを行わせる
     */
    public NotificationBuilder clickBroadcast(Intent intent, int pendingIntentFlags) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0x00, intent, pendingIntentFlags);
        mBuilder.setContentIntent(pendingIntent);
        return this;
    }

    /**
     * クリック時にActivityを起動する
     */
    public NotificationBuilder clickActivity(Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0x00, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        return this;
    }

    /**
     * クリック時にActivityを起動する
     */
    public NotificationBuilder clickActivity(Intent intent, int pendingIntentFlags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0x00, intent, pendingIntentFlags);
        mBuilder.setContentIntent(pendingIntent);
        return this;
    }

    /**
     * View Contentを設定する
     */
    public NotificationBuilder content(RemoteViews view) {
        if (Build.VERSION.SDK_INT >= 24) {
            mBuilder.setCustomContentView(view);
        } else {
            mBuilder.setContent(view);
        }
        return this;
    }

    public NotificationBuilder title(@NonNull String text) {
        mBuilder.setContentTitle(text);
        return this;
    }

    public NotificationBuilder title(@StringRes int resId) {
        mBuilder.setContentTitle(mContext.getText(resId));
        return this;
    }

    public NotificationBuilder ticker(@NonNull String text) {
        mBuilder.setTicker(text);
        return this;
    }

    public NotificationBuilder ticker(@StringRes int resId) {
        mBuilder.setTicker(mContext.getString(resId));
        return this;
    }

    public NotificationBuilder autoCancel() {
        mBuilder.setAutoCancel(true);
        return this;
    }

    /**
     * NotificationBuilderを直接カスタマイズする
     */
    public NotificationBuilder customize(Action1<NotificationCompat.Builder> action) {
        try {
            action.action(mBuilder);
            return this;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("NewApi")
    Notification buildNotification() {
        Notification notification = mBuilder.build();
        return notification;
    }

    /**
     * Service用のForeground通知を行う
     */
    public SupportNotification showForeground(int notificationId) {
        if (!(mContext instanceof Service)) {
            throw new IllegalArgumentException("Context != service :: " + mContext);
        }

        mBuilder.setOngoing(true);
        Notification notification = buildNotification();

        ((Service) mContext).startForeground(notificationId, notification);
//        NotificationManager notificationManager = ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE));
//        notificationManager.notify(notificationId, notification);
        return new SupportNotification(mContext, notification, notificationId);
    }

    /**
     * Notificationへの表示を行う
     */
    public SupportNotification show(int notificationId) {
        Notification notification = buildNotification();
        NotificationManager notificationManager = ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE));
        notificationManager.notify(notificationId, notification);
        return new SupportNotification(mContext, notification, notificationId);
    }

    public static NotificationBuilder from(Context context) {
        return new NotificationBuilder(context, CHANNEL_DEFAULT);
    }

    public static NotificationBuilder from(Context context, String notificationChannel) {
        return new NotificationBuilder(context, notificationChannel);
    }
}
