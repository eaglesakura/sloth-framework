package com.eaglesakura.android.framework.delegate.service;

import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.rx.LifecycleEvent;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.rx.event.LifecycleEventImpl;
import com.eaglesakura.android.thread.ui.UIHandler;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicInteger;

import rx.subjects.BehaviorSubject;

/**
 * 便利系メソッドを固めたUtilクラス
 */
public abstract class SupportServiceDelegate {

    public interface SupportServiceCompat {
        @Nullable
        Service getService(@NonNull SupportServiceDelegate self);
    }

    /**
     * 呼び出しリクエストを指定する
     */
    private static final String EXTRA_WAKEUP_REQUEST_CODE = "EXTRA_WAKEUP_REQUEST_CODE";

    /**
     * 呼び出しリクエストの引数を指定する
     */
    private static final String EXTRA_WAKEUP_REQUEST_ARGMENTS = "EXTRA_WAKEUP_REQUEST_ARGMENTS";

    /**
     * アラームを作成した時刻
     */
    private static final String EXTRA_WAKEUP_REQUEST_ALARM_TIME = "EXTRA_WAKEUP_REQUEST_ALARM_TIME";

    private String ACTION_SELF_WAKEUP_BROADCAST;

    /**
     * CPU稼働保証を行うカウント
     */
    AtomicInteger mWakeUpRef = new AtomicInteger(0);

    @NonNull
    private AlarmManager mAlarmManager;

    @NonNull
    private PowerManager mPowerManager;

    @NonNull
    private PowerManager.WakeLock wakeLock;

    private BehaviorSubject<LifecycleEvent> mLifecycleSubject = BehaviorSubject.create(new LifecycleEventImpl(LifecycleState.NewObject));

    @NonNull
    private SubscriptionController mSubscriptionController = new SubscriptionController();

    @NonNull
    final SupportServiceCompat mCompat;

    public SupportServiceDelegate(SupportServiceCompat compat) {
        mCompat = compat;
        mSubscriptionController.bind(mLifecycleSubject);
    }

    public void bind(ServiceLifecycleDelegate lifecycleDelegate) {
        lifecycleDelegate.getSubscription().getObservable().subscribe(it -> {
            switch (it.getState()) {
                case OnCreated:
                    onCreate();
                    break;
                case OnDestroyed:
                    onDestroy();
                    break;
            }
        });
    }

    protected Service getService() {
        return mCompat.getService(this);
    }

    @UiThread
    @CallSuper
    protected void onCreate() {
        Service service = mCompat.getService(this);

        mAlarmManager = (AlarmManager) service.getSystemService(Context.ALARM_SERVICE);
        mPowerManager = (PowerManager) service.getSystemService(Context.POWER_SERVICE);

        this.ACTION_SELF_WAKEUP_BROADCAST = service.getPackageName() + "/" + getClass().getName() + ".ACTION_SELF_WAKEUP_BROADCAST" + "/" + hashCode();
        service.registerReceiver(wakeupBroadcastReceiver, new IntentFilter(ACTION_SELF_WAKEUP_BROADCAST));

        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnCreated));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStarted));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnResumed));
    }

    @UiThread
    @CallSuper
    protected void onDestroy() {
        synchronized (this) {
            // 強制的にCPU稼働を停止させる
            if (wakeLock != null) {
                wakeLock.release();
                wakeLock = null;
                mWakeUpRef.set(0);
            }
        }
        getService().unregisterReceiver(wakeupBroadcastReceiver);
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnPaused));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStopped));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnDestroyed));
    }

    @NonNull
    public SubscriptionController getSubscriptionController() {
        return mSubscriptionController;
    }

    /**
     * 新しい非同期タスクを実行する
     *
     * @param subscribeTarget 実行スレッド
     * @param action          実行タスク
     */
    public <T> RxTaskBuilder<T> newTask(SubscribeTarget subscribeTarget, RxTask.Async<T> action) {
        return new RxTaskBuilder<T>(getSubscriptionController())
                .subscribeOn(subscribeTarget)
                .observeOn(ObserveTarget.Alive)
                .async(action);
    }

    public boolean isDestroyed() {
        return mLifecycleSubject.getValue().getState() == LifecycleState.OnDestroyed;
    }

    /**
     * CPU稼働参照を減らす
     */
    public void popCpuWakeup() {
        if (mWakeUpRef.decrementAndGet() == 0) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    /**
     * CPU稼働参照を増やす
     */
    public void pushCpuWakeup() {
        if (mWakeUpRef.incrementAndGet() == 1) {
            wakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getSimpleName());
            wakeLock.acquire();
        }
    }

    /**
     * 指定したミリ秒後、再度CPUを叩き起こす。
     * <br>
     * 繰り返しには対応しない。
     *
     * @param requestCode     呼び出しリクエスト
     * @param requestArgments コールバックに呼び出される引数
     * @param delayTimeMs     遅延時間
     */
    protected void requestNextAlarmDelayed(int requestCode, Bundle requestArgments, long delayTimeMs) {
        requestNextAlarmDelayed(requestCode, requestArgments, delayTimeMs, true);
    }

    /**
     * 指定したミリ秒後、再度CPUを叩き起こす。
     * <br>
     * 繰り返しには対応しない。
     *
     * @param requestCode     呼び出しリクエスト
     * @param requestArgments コールバックに呼び出される引数
     * @param delayTimeMs     遅延時間
     * @param extract         時間保証を有効にする場合はtrue
     */
    @SuppressLint("NewApi")
    protected void requestNextAlarmDelayed(int requestCode, Bundle requestArgments, long delayTimeMs, boolean extract) {
        Intent intent = new Intent(ACTION_SELF_WAKEUP_BROADCAST);
        intent.putExtra(EXTRA_WAKEUP_REQUEST_CODE, requestCode);
        if (requestArgments != null) {
            intent.putExtra(EXTRA_WAKEUP_REQUEST_ARGMENTS, requestArgments);
        }
        final long current = SystemClock.elapsedRealtime();
        intent.putExtra(EXTRA_WAKEUP_REQUEST_ALARM_TIME, current);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getService(), requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT
        );

        if (extract && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAlarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, current + delayTimeMs, pendingIntent);
        } else {
            mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, current + delayTimeMs, pendingIntent);
        }
    }

    /**
     * セットしてあるアラームを解除する
     */
    protected void cancelAlarm(int requestCode) {
        Intent intent = new Intent(ACTION_SELF_WAKEUP_BROADCAST);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getService(), requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT
        );
        mAlarmManager.cancel(pendingIntent);
    }

    /**
     * Systemのアラームに対応する
     */
    private final BroadcastReceiver wakeupBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int requestCode = intent.getIntExtra(EXTRA_WAKEUP_REQUEST_CODE, 0);
            Bundle argments = intent.getBundleExtra(EXTRA_WAKEUP_REQUEST_ARGMENTS);
            long alarmTime = intent.getLongExtra(EXTRA_WAKEUP_REQUEST_ALARM_TIME, 0);


            final long current = SystemClock.elapsedRealtime();
            onAlarmReceived(requestCode, argments, current - alarmTime);
        }
    };

    /**
     * AlarmManagerによってCPUが叩き起こされたタイミングで呼び出される
     * <br>
     * このメソッドは必ずonReceiveの中で呼び出されることを保証する。
     *
     * @param requestCode     呼び出しリクエスト
     * @param requestArgments コールバックに呼び出される引数
     * @param delayedTimeMs   設定から実際に遅延した時間
     */
    protected void onAlarmReceived(int requestCode, Bundle requestArgments, long delayedTimeMs) {
    }
}
