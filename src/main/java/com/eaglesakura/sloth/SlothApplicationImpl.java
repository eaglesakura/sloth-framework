package com.eaglesakura.sloth;

import com.eaglesakura.android.device.display.DisplayInfo;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.cerberus.PendingCallbackQueue;
import com.eaglesakura.json.JSON;
import com.eaglesakura.sloth.annotation.ConstantObject;
import com.eaglesakura.sloth.app.VersionContext;
import com.eaglesakura.sloth.app.lifecycle.ServiceLifecycle;
import com.eaglesakura.sloth.db.property.PropertyStore;
import com.eaglesakura.sloth.db.property.TextDatabasePropertyStore;
import com.eaglesakura.sloth.db.property.model.PropertySource;
import com.eaglesakura.sloth.gen.SystemSettings;
import com.eaglesakura.util.RandomUtil;
import com.eaglesakura.util.StringUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * ライブラリの内部実装管理
 */
@SuppressLint("NewApi")
class SlothApplicationImpl implements Application.ActivityLifecycleCallbacks {
    @NonNull
    final Application mApplication;

    @NonNull
    SystemSettings mSettings;

    /**
     * 現在のバージョン情報
     */
    @NonNull
    VersionContext mVersionContext;

    /**
     * 初回起動時に確定するID
     */
    @ConstantObject
    String mInstallUniqueId;

    @NonNull
    ServiceLifecycle mLifecycleDelegate = new ServiceLifecycle();

    @NonNull
    Set<Sloth.ApplicationStateListener> mStateListeners = new HashSet<>();

    int mForegroundActivities;

    @NonNull
    LocalMessageReceiver mLocalMessageReceiver;

    @NonNull
    final String mProcessId = RandomUtil.randShortString();

    public SlothApplicationImpl(@NonNull Application application) {
        mApplication = application;
        mApplication.registerActivityLifecycleCallbacks(this);
        loadSettings();

        mLifecycleDelegate.onCreate();
        mLocalMessageReceiver = new BroadcastReceiverImpl(application);
        mLocalMessageReceiver.connect();

        printDeviceInfo();
    }

    /**
     * デバイス情報を出力する
     */
    private void printDeviceInfo() {
        if (!ContextUtil.isDebug(mApplication)) {
            return;
        }

        SlothLog.system("========= Runtime Information =========");
        SlothLog.system("== Device %s", Build.MODEL);
        {
            DisplayInfo displayInfo = new DisplayInfo(mApplication);
            SlothLog.system("== Display %d.%d inch = %s",
                    displayInfo.getDiagonalInchRound().major, displayInfo.getDiagonalInchRound().minor,
                    displayInfo.getDeviceType().name()
            );
            SlothLog.system("==   Display [%d x %d] pix", displayInfo.getWidthPixel(), displayInfo.getHeightPixel());
            SlothLog.system("==   Display [%.1f x %.1f] dp", displayInfo.getWidthDp(), displayInfo.getHeightDp());
            SlothLog.system("==   res/values-%s", displayInfo.getDpi().name());
            SlothLog.system("==   res/values-sw%ddp", displayInfo.getSmallestWidthDp());
        }
        SlothLog.system("========= Runtime Information =========");
    }

    private void loadSettings() {
        try (InputStream is = mApplication.getResources().openRawResource(R.raw.sloth_system_properties)) {
            PropertyStore store = new TextDatabasePropertyStore(mApplication, "sloth.db")
                    .loadProperties(JSON.decode(is, PropertySource.class));

            mSettings = new SystemSettings();
            mSettings.setPropertyStore(store);
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

        SlothLog.system("Install Unique ID [%s]", mInstallUniqueId);
        SlothLog.system("Process Unique ID [%s]", mProcessId);
        SlothLog.system("VersionCode       [%d] -> [%d]", oldVersionCode, versionCode);
        SlothLog.system("VersionName       [%s] -> [%s]", oldVersionName, versionName);

        mSettings.setLastBootedAppVersionCode(versionCode);
        mSettings.setLastBootedAppVersionName(versionName);
        mSettings.commit();

        // バージョンコードかバージョン名が変わったら通知を行う
        mVersionContext = new VersionContext(oldVersionName, oldVersionCode, versionName, versionCode);
    }

    public String getProcessId() {
        return mProcessId;
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
            // フォアグラウンドに移動した
            synchronized (mStateListeners) {
                for (Sloth.ApplicationStateListener listener : mStateListeners) {
                    listener.onApplicationForeground(activity);
                }
            }
        }
        SlothLog.system("onActivityResumed num[%d] (%s)", mForegroundActivities, activity.toString());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        --mForegroundActivities;
        // バックグラウンドに移動した
        if (mForegroundActivities == 0) {
            synchronized (mStateListeners) {
                for (Sloth.ApplicationStateListener listener : mStateListeners) {
                    listener.onApplicationBackground();
                }
            }
        }
        SlothLog.system("onActivityPaused num[%d] (%s)", mForegroundActivities, activity.toString());
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


    class BroadcastReceiverImpl extends LocalMessageReceiver {
        public BroadcastReceiverImpl(Context context) {
            super(context);
        }

        @Override
        protected void onRuntimePermissionUpdated(String[] granted, String[] denied) {
            synchronized (mStateListeners) {
                for (Sloth.ApplicationStateListener listener : mStateListeners) {
                    listener.onRuntimePermissionUpdated(granted, denied);
                }
            }
        }
    }
}
