package com.eaglesakura.sloth.app;

import com.eaglesakura.sloth.app.lifecycle.ServiceLifecycle;

import android.app.Service;
import android.support.annotation.NonNull;

/**
 * ライフサイクルサポートを行ったService
 */
public abstract class SlothService extends Service {
    private ServiceLifecycle mLifecycle;

    public ServiceLifecycle getLifecycle() {
        if (mLifecycle == null) {
            synchronized (this) {
                if (mLifecycle == null) {
                    mLifecycle = new ServiceLifecycle();
                    onCreateLifecycle(mLifecycle);
                }
            }
        }
        return mLifecycle;
    }

    /**
     * ライフサイクルが新規生成された
     */
    protected void onCreateLifecycle(@NonNull ServiceLifecycle lifecycle) {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getLifecycle().onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLifecycle().onDestroy();
    }
}
