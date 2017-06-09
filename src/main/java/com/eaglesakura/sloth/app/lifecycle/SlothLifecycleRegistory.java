package com.eaglesakura.sloth.app.lifecycle;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;

/**
 * Lifecycle Event互換オブジェクト
 */
class SlothLifecycleRegistory implements LifecycleRegistryOwner {

    LifecycleRegistry mRegistry;

    public SlothLifecycleRegistory() {
        mRegistry = new LifecycleRegistry(this);
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mRegistry;
    }
}
