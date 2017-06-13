package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.lambda.Action1;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.annotation.Experimental;
import com.eaglesakura.sloth.app.lifecycle.event.State;
import com.eaglesakura.sloth.util.LiveDataUtil;
import com.eaglesakura.util.Util;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.CallSuper;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**
 * {@link Lifecycle} に連携したLiveDataを構築する
 *
 * Observerは{@link Lifecycle#onDestroy()} タイミングで自動的に廃棄される。
 */
@Experimental
public abstract class SlothLiveData<T> extends LiveData<T> {

    /**
     * LiveDataごとの独自のライフサイクルを指定する。
     */
    private ServiceLifecycle mLifecycle;

    /**
     * 自動的にライフサイクルが生成された場合はtrue,
     * これは onInactive() で自動的に破棄する。
     */
    private boolean mLifecycleCreated;

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

    /**
     * ライフサイクルオブジェクトを設定する
     *
     * 外部から設定された場合、そのライフサイクルが優先されて使用される。
     */
    @Experimental
    public void setLifecycle(ServiceLifecycle lifecycle) {
        if (mLifecycle != null) {
            throw new IllegalStateException("Lifecycle injected!");
        }
        mLifecycle = lifecycle;
        mLifecycleCreated = false;
    }


    /**
     * ライフサイクルを取得する
     *
     * LiveDataがInactiveの場合、このライフサイクルは基本的にnullを返却する。
     */
    @Nullable
    public Lifecycle getLifecycle() {
        return mLifecycle;
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

        if (mLifecycle == null) {
            mLifecycle = new ServiceLifecycle();
            mLifecycle.onCreate();
            mLifecycleCreated = true;
        } else {
            mLifecycleCreated = false;
        }
        super.onActive();
    }

    @CallSuper
    @Override
    protected void onInactive() {
        super.onInactive();

        if (mLifecycleCreated) {
            mLifecycle.onDestroy();
            mLifecycle = null;
        }

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
     * Observerは{@link Lifecycle#onDestroy()} タイミングで自動的にremoveObserver()される。。
     */
    public void observeAlive(@NonNull Lifecycle lifecycle, @NonNull Observer<T> observer) {
        lifecycle.subscribe(event -> {
            switch (event.getState()) {
                case OnDestroy:
                    removeObserver(observer);
                    break;
            }
        });
        observeForever(observer);
    }

    /**
     * {@link Lifecycle} に連携したObserverを構築する
     *
     * Observerは{@link Lifecycle#onDestroy()} タイミングで自動的にremoveObserver()される。。
     */
    public void observeForeground(@NonNull Lifecycle lifecycle, @NonNull Observer<T> observer) {
        lifecycle.subscribe(event -> {
            switch (event.getState()) {
                case OnPause:
                case OnDestroy:
                    removeObserver(observer);
                    break;
                case OnResume:
                    observe(lifecycle.getLifecycleRegistry(), observer);
                    break;
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
