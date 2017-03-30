package com.eaglesakura.android.framework;

import com.eaglesakura.android.device.display.DisplayInfo;
import com.eaglesakura.android.framework.gen.props.SystemSettings;
import com.eaglesakura.android.framework.ui.message.LocalMessageReceiver;
import com.eaglesakura.android.property.PropertyStore;
import com.eaglesakura.android.property.TextDatabasePropertyStore;
import com.eaglesakura.android.property.model.PropertySource;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.BackgroundTaskBuilder;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.cerberus.LifecycleEvent;
import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.PendingCallbackQueue;
import com.eaglesakura.cerberus.event.LifecycleEventImpl;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.json.JSON;
import com.eaglesakura.util.RandomUtil;
import com.eaglesakura.util.StringUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

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
        SystemSettings mSettings;

        /**
         * 初回起動時に確定するID
         */
        @NonNull
        String mInstallUniqueId;

        @NonNull
        final BehaviorSubject<LifecycleEvent> mSubject = BehaviorSubject.create(new LifecycleEventImpl(LifecycleState.NewObject));

        @NonNull
        final PendingCallbackQueue mCallbackQueue = new PendingCallbackQueue();

        @NonNull
        final Set<ApplicationStateListener> mStateListeners = new HashSet<>();

        int mForegroundActivities;

        @NonNull
        final LocalMessageReceiver mLocalMessageReceiver;

        public FrameworkCentralImpl(@NonNull Application application) {
            mApplication = application;
            if (application instanceof FrameworkApplication) {
                mFrameworkApplication = (FrameworkApplication) application;
            } else {
                mFrameworkApplication = null;
            }
            mApplication.registerActivityLifecycleCallbacks(this);
            loadSettings();

            mCallbackQueue.bind(mSubject);
            mSubject.onNext(new LifecycleEventImpl(LifecycleState.OnCreated));
            mSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStarted));

            mLocalMessageReceiver = new LocalMessageReceiver(mApplication) {
                @Override
                protected void onRuntimePermissionUpdated(String[] granted, String[] denied) {
                    synchronized (mStateListeners) {
                        for (ApplicationStateListener listener : mStateListeners) {
                            listener.onRuntimePermissionUpdated(granted, denied);
                        }
                    }
                }

                @Override
                protected void onGooglePlayLoginCompleted() {
                }
            };

            if (ContextUtil.isDebug(mApplication)) {
                FwLog.system("========= Runtime Information =========");
                FwLog.system("== Device %s", Build.MODEL);
                {
                    DisplayInfo displayInfo = new DisplayInfo(mApplication);
                    FwLog.system("== Display %d.%d inch = %s",
                            displayInfo.getDiagonalInchRound().major, displayInfo.getDiagonalInchRound().minor,
                            displayInfo.getDeviceType().name()
                    );
                    FwLog.system("==   Display [%d x %d] pix", displayInfo.getWidthPixel(), displayInfo.getHeightPixel());
                    FwLog.system("==   Display [%.1f x %.1f] dp", displayInfo.getWidthDp(), displayInfo.getHeightDp());
                    FwLog.system("==   res/values-%s", displayInfo.getDpi().name());
                    FwLog.system("==   res/values-sw%ddp", displayInfo.getSmallestWidthDp());
                }
                FwLog.system("========= Runtime Information =========");
            }
        }

        @SuppressLint("NewApi")
        void loadSettings() {

            try (InputStream is = mApplication.getResources().openRawResource(R.raw.esm_system_properties)) {
                PropertyStore store = new TextDatabasePropertyStore(mApplication, "framework.db")
                        .loadProperties(JSON.decode(is, PropertySource.class));

                mSettings = new SystemSettings(store);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

            // 設定を読み出す
            final String oldVersionName = mSettings.getLastBootedAppVersionName();
            final String versionName = ContextUtil.getVersionName(mApplication);
            mInstallUniqueId = mSettings.getInstallUniqueId();
            if (StringUtil.isEmpty(mInstallUniqueId)) {
                mInstallUniqueId = RandomUtil.randString();
                mSettings.setInstallUniqueId(mInstallUniqueId);
            }

            final int oldVersionCode = mSettings.getLastBootedAppVersionCode();
            final int versionCode = ContextUtil.getVersionCode(mApplication);

            FwLog.system("Install Unique ID [%s]", mInstallUniqueId);
            FwLog.system("VersionCode       [%d] -> [%d]", oldVersionCode, versionCode);
            FwLog.system("VersionName       [%s] -> [%s]", oldVersionName, versionName);

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
                mSubject.onNext(new LifecycleEventImpl(LifecycleState.OnResumed));

                // フォアグラウンドに移動した
                synchronized (mStateListeners) {
                    for (ApplicationStateListener listener : mStateListeners) {
                        listener.onApplicationForeground(activity);
                    }
                }
            }
            FwLog.system("onActivityResumed num[%d] (%s)", mForegroundActivities, activity.toString());
        }

        @Override
        public void onActivityPaused(Activity activity) {
            --mForegroundActivities;
            // バックグラウンドに移動した
            if (mForegroundActivities == 0) {
                mSubject.onNext(new LifecycleEventImpl(LifecycleState.OnPaused));

                synchronized (mStateListeners) {
                    for (ApplicationStateListener listener : mStateListeners) {
                        listener.onApplicationBackground();
                    }
                }
            }
            FwLog.system("onActivityPaused num[%d] (%s)", mForegroundActivities, activity.toString());
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
    public static <T> BackgroundTaskBuilder<T> newGlobalTask(BackgroundTask.Async<T> callback) {
        return new BackgroundTaskBuilder<T>(sImpl.mCallbackQueue)
                .async(callback)
                .callbackOn(CallbackTime.FireAndForget)
                .executeOn(ExecuteTarget.LocalQueue);
    }

    @Deprecated
    public static PendingCallbackQueue getSubscription() {
        return sImpl.mCallbackQueue;
    }

    public static PendingCallbackQueue getCallbackQueue() {
        return sImpl.mCallbackQueue;
    }


    /**
     * Frameworkの設定クラスを取得する]
     *
     * MEMO: 将来的に削除予定
     */
    @Deprecated
    public static SystemSettings getSettings() {
        return sImpl.mSettings;
    }

    public static Application getApplication() {
        return sImpl.mApplication;
    }

    /**
     * インストール（初回起動時）に確定するUIDを取得する
     */
    @NonNull
    public static String getUniqueId() {
        return sImpl.mInstallUniqueId;
    }

    /**
     * アプリのステート変更通知を行う
     */
    @UiThread
    public static void registerListener(ApplicationStateListener listener) {
        synchronized (sImpl.mStateListeners) {
            sImpl.mStateListeners.add(listener);
        }
    }

    /**
     * アプリのステート変更通知を廃棄する
     */
    @UiThread
    public static void unregisterListener(ApplicationStateListener listener) {
        synchronized (sImpl.mStateListeners) {
            sImpl.mStateListeners.remove(listener);
        }
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
        return requestDeploygateInstall(true);
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
    public static boolean requestDeploygateInstall(boolean forceApply) {
        try {
            Class<?> DeployGateCallback = Class.forName("com.deploygate.sdk.DeployGateCallback");
            Class<?> DeployGate = Class.forName("com.deploygate.sdk.DeployGate");

            Method installMethod = DeployGate.getMethod("install", Application.class, DeployGateCallback, boolean.class);

            installMethod.invoke(DeployGate, getApplication(), null, forceApply);
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
        void onRequestSaveCentral(@NonNull Object central);
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
