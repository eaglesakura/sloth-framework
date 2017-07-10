package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.sloth.annotation.Experimental;

/**
 * データの更新を外部から行えるようにしたLiveData
 *
 * 外部からは通常の {@link android.arch.lifecycle.LiveData} として扱うことを想定する。
 */
@Experimental
public abstract class SlothPublicLiveData<T> extends SlothLiveData<T> {
    @Override
    public void postValue(T value) {
        super.postValue(value);
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);
    }
}
