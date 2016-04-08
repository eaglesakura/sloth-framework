package com.eaglesakura.android.framework;

import com.eaglesakura.android.framework.db.BasicSettings;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.ContextUtil;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;

import rx.subjects.BehaviorSubject;

/**
 *
 */
public class FrameworkCentral {

    private static class FrameworkCentralImpl implements Application.ActivityLifecycleCallbacks {
        @NonNull
        final Application mApplication;

        @Nullable
        final FrameworkApplication mFrameworkApplication;

        @NonNull
        final BasicSettings mSettings;


        @NonNull
        final BehaviorSubject<LifecycleState> mSubject = BehaviorSubject.create(LifecycleState.NewObject);

        @NonNull
        final SubscriptionController mSubscriptionController = new SubscriptionController();

        int mForegroundActivities;

        public FrameworkCentralImpl(@NonNull Application application) {
            mApplication = application;
            if (application instanceof FrameworkApplication) {
                mFrameworkApplication = (FrameworkApplication) application;
            } else {
                mFrameworkApplication = null;
            }
            mSettings = new BasicSettings(mApplication);

            mApplication.registerActivityLifecycleCallbacks(this);
            loadSettings();

            mSubscriptionController.bind(mSubject);
            mSubject.onNext(LifecycleState.OnCreated);
            mSubject.onNext(LifecycleState.OnStarted);
        }

        void loadSettings() {
            // 設定を読み出す
            final String oldVersionName = mSettings.getLastBootedAppVersionName();
            final String versionName = ContextUtil.getVersionName(mApplication);

            final int oldVersionCode = mSettings.getLastBootedAppVersionCode();
            final int versionCode = ContextUtil.getVersionCode(mApplication);

            FwLog.system("VersionCode [%d] -> [%d]", oldVersionCode, versionCode);
            FwLog.system("VersionName [%s] -> [%s]", oldVersionName, versionName);

            mSettings.setLastBootedAppVersionCode(versionCode);
            mSettings.setLastBootedAppVersionName(versionName);
            mSettings.commit();

            // バージョンコードかバージョン名が変わったら通知を行う
            if (mFrameworkApplication != null && ((versionCode != oldVersionCode) || (!oldVersionName.equals(versionName)))) {
                mFrameworkApplication.onApplicationUpdated(oldVersionCode, versionCode, oldVersionName, versionName);
            }

        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            ++mForegroundActivities;
            if (mForegroundActivities == 1) {
                mSubject.onNext(LifecycleState.OnResumed);

                // フォアグラウンドに移動した
                if (mFrameworkApplication != null) {
                    mFrameworkApplication.onApplicationForeground(activity);
                }
            }
            FwLog.system("onActivityResumed num[%d] (%s)", mForegroundActivities, activity.toString());
        }

        @Override
        public void onActivityPaused(Activity activity) {
            FwLog.system("onActivityPaused num[%d] (%s)", mForegroundActivities, activity.toString());
            UIHandler.postDelayedUI(() -> {
                --mForegroundActivities;
                // バックグラウンドに移動した
                if (mForegroundActivities == 0 && mFrameworkApplication != null) {
                    mFrameworkApplication.onApplicationBackground();
                    mSubject.onNext(LifecycleState.OnPaused);
                }
            }, 100);
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }
    }

    private static FrameworkCentralImpl sImpl;

    /**
     * Application#onCreateで呼び出す
     */
    public static void onApplicationCreate(Application application) {
        sImpl = new FrameworkCentralImpl(application);
        if (sImpl.mFrameworkApplication != null) {
            sImpl.mFrameworkApplication.onRequestSaveCentral(sImpl);
        }
    }

    /**
     * グローバルに動作するタスクを取得する
     *
     * デフォルトはグローバルパイプラインで処理され、撃ちっぱなしとなる。
     * MEMO : .start()は外部で呼び出す必要がある。
     */
    public static <T> RxTaskBuilder<T> newGlobalTask(RxTask.Async<T> callback) {
        return new RxTaskBuilder<T>(sImpl.mSubscriptionController)
                .async(callback)
                .observeOn(ObserveTarget.FireAndForget)
                .subscribeOn(SubscribeTarget.Pipeline);
    }

    public static SubscriptionController getSubscription() {
        return sImpl.mSubscriptionController;
    }

    //    /**
//     * GCMトークンを登録する
//     */
//    public static void registerGcm() throws IOException {
//        if (!StringUtil.isEmpty(getSettings().getGcmToken())) {
//            return;
//        }
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplication());
//        String registerId = gcm.register(getApplication().getString(R.string.eglibrary_Gcm_SenderID));
//
//        if (StringUtil.isEmpty(registerId)) {
//            throw new IllegalStateException("GCM register id == null");
//        }
//        getSettings().setGcmToken(registerId);
//        getSettings().commit();
//    }

//    /**
//     * GCMトークンを無効化する
//     *
//     * @throws IOException
//     */
//    public static void unregisterGcm() throws IOException {
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplication());
//        gcm.unregister();
//        getSettings().setGcmToken("");
//        getSettings().commit();
//    }

    /**
     * Frameworkの設定クラスを取得する
     */
    public static BasicSettings getSettings() {
        return sImpl.mSettings;
    }

    public static Application getApplication() {
        return sImpl.mApplication;
    }

    /**
     * Deploygateのインストールを行う。
     * <br>
     * dependenciesが設定されていない場合、このメソッドはfalseを返す
     * <br>
     * debugCompile 'com.deploygate:sdk:3.1'
     *
     * @return 成功したらtrue
     */
    public static boolean requestDeploygateInstall() {
        try {
            Class<?> clazz = Class.forName("com.deploygate.sdk.DeployGate");
            Method installMethod = clazz.getMethod("install", Application.class);

            installMethod.invoke(clazz, getApplication());
            FwLog.system("install success Deploygate");
            return true;
        } catch (Exception e) {
            FwLog.system("not dependencies Deploygate");
            return false;
        }
    }

    /**
     * impl Application
     */
    public interface FrameworkApplication {
        /**
         * Applicationが更新された際に呼び出される
         *
         * @param oldVersionCode 前回のバージョンコード
         * @param newVersionCode アップデート後のバージョンコード
         * @param oldVersionName 前回のバージョン名
         * @param newVersionName アップデート後のバージョン名
         */
        void onApplicationUpdated(int oldVersionCode, int newVersionCode, String oldVersionName, String newVersionName);

        /**
         * Class Unloadを防ぐため、Applicationのローカル変数としてCentralを保持する
         */
        void onRequestSaveCentral(Object central);

        /**
         * アプリが前面に配置された
         */
        void onApplicationForeground(Activity activity);

        /**
         * アプリがバックグラウンドに戻された
         */
        void onApplicationBackground();
    }
}
