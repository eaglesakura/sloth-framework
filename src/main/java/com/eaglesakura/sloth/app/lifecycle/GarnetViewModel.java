package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.android.garnet.Initializer;
import com.eaglesakura.sloth.annotation.Experimental;

import android.arch.lifecycle.ViewModel;

/**
 * Garnetによって生成されることを前提としたViewModel
 */
@Experimental
public abstract class GarnetViewModel extends ViewModel {
    private boolean mInitialized;

    /**
     * Garnetによって初期化されるメソッド
     * このメソッドはGarnetから自動的に呼び出されるため、自発的にコールする必要はない。
     */
    @Initializer
    public final synchronized void initializeFromGarnet() {
        if (mInitialized) {
            return;
        }

        onInitialize();
        mInitialized = true;
    }

    /**
     * 初期化処理を行う
     */
    protected void onInitialize() {

    }
}
