package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.sloth.annotation.Experimental;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;

/**
 * Lifecycle Event互換オブジェクト
 */
@Experimental
class SlothLifecycleRegistry implements LifecycleRegistryOwner {

    private LifecycleRegistry mRegistry;

    public SlothLifecycleRegistry() {
        mRegistry = new LifecycleRegistry(this);
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mRegistry;
    }
}
