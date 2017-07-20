package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.lambda.Action1;
import com.eaglesakura.lambda.Action2;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.annotation.Experimental;
import com.eaglesakura.sloth.app.lifecycle.event.LifecycleEvent;
import com.eaglesakura.sloth.app.lifecycle.event.State;
import com.eaglesakura.sloth.util.LiveDataUtil;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.CallSuper;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * データがActiveになった際のコールバック
     */
    private Set<Action1<SlothLiveData<T>>> mOnActiveListeners = new HashSet<>();

    /**
     * データがInactiveになった際のコールバック
     */
    private Set<Action1<SlothLiveData<T>>> mOnInactiveListeners = new HashSet<>();

    /**
     * setValueされた際のコールバック
     */
    private Set<Action2<SlothLiveData<T>, T>> mValueUpdateListeners = new HashSet<>();

    /**
     * Activeかどうかのステータス
     */
    private AtomicBoolean mActive = new AtomicBoolean(false);

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

    public void addOnDataSetListener(@NonNull Action2<SlothLiveData<T>, T> action) {
        mValueUpdateListeners.add(action);
    }

    public void removeOnActiveListener(@NonNull Action1<? extends SlothLiveData> action) {
        mOnActiveListeners.remove(action);
    }

    public void removeOnInactiveListener(@NonNull Action1<? extends SlothLiveData> action) {
        mOnInactiveListeners.remove(action);
    }

    public void removeOnDataSetListener(Action2<SlothLiveData<T>, T> action) {
        mValueUpdateListeners.remove(action);
    }

    @Override
    protected void setValue(T value) {
        super.setValue(value);
        for (Action2<SlothLiveData<T>, T> action : mValueUpdateListeners) {
            try {
                action.action(this, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @CallSuper
    @Override
    protected void onActive() {
        mActive.set(true);
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
        mActive.set(false);
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
     * このLiveDataがActiveな状態であればtrue
     */
    public boolean isActive() {
        return mActive.get();
    }

    public void observe(@NonNull Lifecycle lifecycle, @NonNull Observer<T> observer) {
        observe(lifecycle.getLifecycleRegistry(), observer);
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
