package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.android.garnet.Initializer;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.lambda.Action1;
import com.eaglesakura.sloth.annotation.Experimental;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.CallSuper;

import java.util.HashSet;
import java.util.Set;

/**
 * Garnetによって生成されることを前提としたViewModel
 */
@Experimental
public abstract class GarnetViewModel extends ViewModel {
    private boolean mInitialized;

    private ViewModelLifecycle mLifecycle;

    /**
     * 管理下にあるLiveData一覧
     */
    private Set<SlothLiveData> mLiveDatas = new HashSet<>();

    /**
     * Activeなデータ数
     */
    private int mActiveDataNum = 0;

    /**
     * Garnetによって初期化されるメソッド
     * このメソッドはGarnetから自動的に呼び出されるため、自発的にコールする必要はない。
     */
    @Initializer
    public final synchronized void initializeFromGarnet() {
        if (mInitialized) {
            return;
        }

        ViewModelLifecycle lifecycle = new ViewModelLifecycle();
        UIHandler.postUIorRun(lifecycle::onCreate);
        mLifecycle = lifecycle;

        onInitialize();
        mInitialized = true;
    }

    /**
     * ViewModel管理下にあるLiveDataを登録する
     */
    protected void registerLiveData(SlothLiveData liveData) {
        if (mInitialized) {
            throw new IllegalStateException("registerLiveData is onInitialized() only");
        }

        liveData.addOnActiveListener(mOnActiveAction);
        liveData.addOnInactiveListener(mOnInactiveAction);
        mLiveDatas.add(liveData);
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

    /**
     * ViewModelが活性化した
     */
    @CallSuper
    protected void onActive() {
        mLifecycle.onActive();
    }

    /**
     * ViewModelが非活性化した
     */
    @CallSuper
    protected void onInactive() {
        mLifecycle.onInactive();
    }

    /**
     * データがアクティブになった
     */
    private Action1<LiveData> mOnActiveAction = data -> {
        if (mActiveDataNum == 0) {
            onActive();
        }
        ++mActiveDataNum;
    };

    /**
     * データが非活性化した
     */
    private Action1<LiveData> mOnInactiveAction = data -> {
        --mActiveDataNum;
        if (mActiveDataNum == 0) {
            onInactive();
        }
    };
}
