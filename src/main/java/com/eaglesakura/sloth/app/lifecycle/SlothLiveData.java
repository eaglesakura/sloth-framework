package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.lambda.Action1;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.annotation.Experimental;
import com.eaglesakura.sloth.app.lifecycle.event.LifecycleEvent;
import com.eaglesakura.sloth.app.lifecycle.event.State;
import com.eaglesakura.sloth.util.LiveDataUtil;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.CallSuper;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;

/**
 * {@link Lifecycle} に連携したLiveDataを構築する
 *
 * Observerは{@link Lifecycle#onDestroy()} タイミングで自動的に廃棄される。
 */
@Experimental
public abstract class SlothLiveData<T> extends LiveData<T> {
    /**
     * データがActiveになった際のコールバックを定義する
     */
    private Set<Action1<SlothLiveData<T>>> mOnActiveListeners = new HashSet<>();

    /**
     * データがInactiveになった際のコールバックを定義する
     */
    private Set<Action1<SlothLiveData<T>>> mOnInactiveListeners = new HashSet<>();

    /**
     * {@link LiveData#onActive()}のListenerを定義する。
     * {@link LiveData#onActive()} の直前に各Listenerが呼び出される。
     */
    public void addOnActiveListener(@NonNull Action1<SlothLiveData<T>> action) {
        mOnActiveListeners.add(action);
    }

    /**
     * {@link LiveData#onActive()}のListenerを定義する。
     * {@link LiveData#onActive()} の直後に各Listenerが呼び出される。
     */
    public void addOnInactiveListener(@NonNull Action1<SlothLiveData<T>> action) {
        mOnInactiveListeners.add(action);
    }

    public void removeOnActiveListener(@NonNull Action1<? extends SlothLiveData> action) {
        mOnActiveListeners.remove(action);
    }

    public void removeOnInactiveListener(@NonNull Action1<? extends SlothLiveData> action) {
        mOnInactiveListeners.remove(action);
    }

    @CallSuper
    @Override
    protected void onActive() {
        for (Action1<SlothLiveData<T>> action : mOnActiveListeners) {
            try {
                action.action(this);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        super.onActive();
    }

    @CallSuper
    @Override
    protected void onInactive() {
        super.onInactive();
        for (Action1<SlothLiveData<T>> action : mOnInactiveListeners) {
            try {
                action.action(this);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * {@link Lifecycle} に連携したObserverを構築する
     *
     * ObserverはonPause() タイミングで自動的にremoveObserver()される。。
     */
    public void observeCurrentForeground(@NonNull Lifecycle lifecycle, @NonNull Observer<T> observer) {
        lifecycle.subscribe(new Consumer<LifecycleEvent>() {
            boolean mPaused;

            @Override
            public void accept(LifecycleEvent event) throws Exception {
                switch (event.getState()) {
                    case OnPause:
                        removeObserver(observer);
                        mPaused = true;
                        break;
                    case OnResume:
                        if (!mPaused) {
                            observe(lifecycle.getLifecycleRegistry(), observer);
                        }
                        break;
                }
            }
        });

        // 既にForegroundであればObserverを登録する
        if (lifecycle.getLifecycleState().ordinal() >= State.OnResume.ordinal()) {
            observe(lifecycle.getLifecycleRegistry(), observer);
        }
    }

    /**
     * データの取得が完了するまで待つ。
     * デッドロックを防ぐため、このメソッドはワーカースレッドからのみ受け付ける。
     *
     * @param cancelCallback キャンセルチェック
     * @return 現在のオブジェクト
     * @throws InterruptedException キャンセルされた場合に例外として投げられる
     */
    public T await(CancelCallback cancelCallback) throws InterruptedException {
        return LiveDataUtil.await(this, cancelCallback);
    }
}
