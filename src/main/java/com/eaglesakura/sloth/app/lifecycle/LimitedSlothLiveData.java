package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.sloth.annotation.Experimental;
import com.eaglesakura.sloth.util.LiveDataUtil;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;

import java.util.HashMap;
import java.util.Map;

/**
 * 各Observerごとに受信回数の制限を設けたLiveDataを定義する。
 *
 * 受信回数は各Observerごとに制限されるため、全体としては複数回コールされる。
 * デフォルトでは一度だけ受信を許可している。
 */
@Experimental
public class LimitedSlothLiveData<T> extends SlothLiveData<T> {
    /**
     * 元のObserver, 変換後Observerのマッピング
     */
    private Map<Observer, Observer> mWrapObservers = new HashMap<>();

    /**
     * 受信最大回数
     */
    private int mLimitedCount = 1;

    public int getLimitedCount() {
        return mLimitedCount;
    }

    public void setLimitedCount(int limitedCount) {
        mLimitedCount = limitedCount;
    }

    private Observer<T> wrap(Observer<T> origin) {
        Observer<T> limitedObserver = LiveDataUtil.limitedObserver(mLimitedCount, origin);
        mWrapObservers.put(origin, limitedObserver);
        return limitedObserver;
    }

    private Observer<T> limited(Observer<T> origin) {
        return mWrapObservers.get(origin);
    }

    @Override
    public void observeAlive(Lifecycle lifecycle, Observer<T> observer) {
        super.observeAlive(lifecycle, wrap(observer));
    }

    @Override
    public void observeForeground(Lifecycle lifecycle, Observer<T> observer) {
        super.observeForeground(lifecycle, wrap(observer));
    }

    @Override
    public void observe(LifecycleOwner owner, Observer<T> observer) {
        super.observe(owner, wrap(observer));
    }

    @Override
    public void observeForever(Observer<T> observer) {
        super.observeForever(wrap(observer));
    }

    @Override
    public void removeObserver(Observer<T> observer) {
        super.removeObserver(limited(observer));
    }

    @Override
    public void removeObservers(LifecycleOwner owner) {
        super.removeObservers(owner);
    }
}
