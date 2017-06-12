package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.sloth.annotation.Experimental;
import com.eaglesakura.sloth.app.lifecycle.event.State;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;

import io.reactivex.annotations.NonNull;

/**
 * {@link Lifecycle} に連携したLiveDataを構築する
 *
 * Observerは{@link Lifecycle#onDestroy()} タイミングで自動的に廃棄される。
 */
@Experimental
public abstract class SlothLiveData<T> extends LiveData<T> {

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
                    observeForever(observer);
                    break;
            }
        });

        // 既にForegroundであればObserverを登録する
        if (lifecycle.getLifecycleState().ordinal() >= State.OnResume.ordinal()) {
            observeForever(observer);
        }
    }
}
