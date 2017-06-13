package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.android.garnet.Initializer;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.sloth.annotation.Experimental;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.CallSuper;

/**
 * Garnetによって生成されることを前提としたViewModel
 */
@Experimental
public abstract class GarnetViewModel extends ViewModel {
    private boolean mInitialized;

    private Lifecycle mLifecycle;

    /**
     * Garnetによって初期化されるメソッド
     * このメソッドはGarnetから自動的に呼び出されるため、自発的にコールする必要はない。
     */
    @Initializer
    public final synchronized void initializeFromGarnet() {
        if (mInitialized) {
            return;
        }

        ServiceLifecycle lifecycle = new ServiceLifecycle();
        UIHandler.postUIorRun(lifecycle::onCreate);
        mLifecycle = lifecycle;
        onInitialize();
        mInitialized = true;
    }

    /**
     * ViewModelのライフサイクルを取得する
     */
    public Lifecycle getLifecycle() {
        return mLifecycle;
    }

    /**
     * 初期化処理を行う
     */
    protected void onInitialize() {

    }

    @CallSuper
    @Override
    protected void onCleared() {
        super.onCleared();
        UIHandler.postUIorRun(mLifecycle::onDestroy);
    }
}
