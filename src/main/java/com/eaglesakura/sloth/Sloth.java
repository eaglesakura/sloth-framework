package com.eaglesakura.sloth;

import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.cerberus.PendingCallbackQueue;
import com.eaglesakura.sloth.annotation.Dummy;
import com.eaglesakura.sloth.cerberus.SupportBackgroundTaskBuilder;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.lang.ref.WeakReference;

/**
 *
 */
public class Sloth {

    /**
     * 実装本体
     */
    private static SlothApplicationImpl sImpl;

    /**
     * ApplicationがBindされていない場合の保留
     */
    private static WeakReference<Context> sPendingContext;

    static SlothApplicationImpl getImpl() {
        if (sPendingContext != null) {
            Context context = sPendingContext.get();
            sPendingContext = null;

            if (context == null || context.getApplicationContext() == null) {
                throw new IllegalStateException("ApplicationContext == null");
            }
            sImpl = new SlothApplicationImpl((Application) context.getApplicationContext());
        }
        return sImpl;
    }

    /**
     * Application#onCreateで呼び出す。
     * 初期化は一度だけ行われ、２度目以降の呼び出しては何もしない。
     */
    @UiThread
    public static void init(Context context) {
        AndroidThreadUtil.assertUIThread();
        // 既に初期化済
        if (sImpl != null) {
            return;
        }

        if (context == null) {
            throw new NullPointerException("Context == null");
        }

        if (!(context instanceof Application) && context.getApplicationContext() == null) {
            sPendingContext = new WeakReference<>(context);
        } else {
            // Applicationが設定されているなら直接作成する
            sPendingContext = null;
            sImpl = new SlothApplicationImpl(((Application) context.getApplicationContext()));
        }
    }

    /**
     * グローバルに動作するタスクを取得する
     *
     * FireAndForget/LocalQueueで処理される。必要に応じて処理スレッドを変更する。
     * MEMO : .start()は外部で呼び出す必要がある。
     */
    public static <T> SupportBackgroundTaskBuilder<T> newGlobalTask(BackgroundTask.Async<T> callback) {
        return (SupportBackgroundTaskBuilder<T>) new SupportBackgroundTaskBuilder<T>(getCallbackQueue())
                .async(callback)
                .callbackOn(CallbackTime.FireAndForget)
                .executeOn(ExecuteTarget.LocalQueue);
    }

    /**
     * グローバルに動作するタスクを取得する
     *
     * FireAndForget/LocalQueueで処理される。必要に応じて処理スレッドを変更する。
     * MEMO : .start()は外部で呼び出す必要がある。
     * MEMO : 非同期タスクはデフォルトでsetされない
     */
    public static <T> SupportBackgroundTaskBuilder<T> newGlobalTask(@Dummy Class<? extends T> clazz) {
        return (SupportBackgroundTaskBuilder<T>) new SupportBackgroundTaskBuilder<T>(getCallbackQueue())
                .callbackOn(CallbackTime.FireAndForget)
                .executeOn(ExecuteTarget.LocalQueue);
    }

    /**
     * グローバルで管理されるコールバックキューを取得する
     */
    public static PendingCallbackQueue getCallbackQueue() {
        return getImpl().mLifecycleDelegate.getCallbackQueue();
    }

    /**
     * Application Contextを取得する
     */
    public static Application getApplication() {
        return getImpl().mApplication;
    }

    /**
     * インストール（初回起動時）に確定するUIDを取得する
     */
    @NonNull
    public static String getUniqueId() {
        return getImpl().mInstallUniqueId;
    }

    /**
     * アプリのステート変更通知を行う
     */
    @UiThread
    public static void registerListener(ApplicationStateListener listener) {
        SlothApplicationImpl impl = getImpl();
        synchronized (impl.mStateListeners) {
            impl.mStateListeners.add(listener);
        }
    }

    /**
     * アプリのステート変更通知を廃棄する
     */
    @UiThread
    public static void unregisterListener(ApplicationStateListener listener) {
        SlothApplicationImpl impl = getImpl();
        synchronized (impl.mStateListeners) {
            impl.mStateListeners.remove(listener);
        }
    }

    /**
     * アプリが所属するProcessがForegroundであるかを確認する
     * 複数Processを構築している場合、このメソッドは別プロセスを正常にハンドリングできない。
     *
     * @return プロセスがForegroundである場合true
     */
    @UiThread
    public static boolean isApplicationForeground() {
        SlothApplicationImpl impl = getImpl();
        WeakReference<Activity> foregroundActivity = getImpl().mForegroundActivity;
        return foregroundActivity != null && foregroundActivity.get() != null;
    }

    /**
     * プロセス起動ごと（Art/Dalvik起動）ごとに割り振られるランダムな文字列を返却する
     */
    public static String getProcessId() {
        return getImpl().getProcessId();
    }

    /**
     * アプリの状態が更新された
     */
    public interface ApplicationStateListener {
        /**
         * アプリが前面に配置された
         */
        void onApplicationForeground(Activity activity);

        /**
         * アプリがバックグラウンドに戻された
         */
        void onApplicationBackground();

        /**
         * Runtime Permissionの状態が更新された
         */
        void onRuntimePermissionUpdated(String[] granted, String[] denied);
    }
}
