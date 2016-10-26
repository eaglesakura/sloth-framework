package com.eaglesakura.android.framework.delegate.task;

import com.eaglesakura.android.thread.ui.UIHandler;
import com.squareup.otto.Bus;

/**
 * Nullを許容したデータバスを構築する
 *
 * データハンドリングは必ずUIスレッドで行われる。
 */
public class DataBus<DataType> {
    Bus mBus = new Bus() {
        @Override
        public void post(Object event) {
            UIHandler.postUIorRun(() -> {
                super.post(event);
            });
        }
    };

    DataType mData;

    public Bus getBus() {
        return mBus;
    }

    /**
     * 握っているデータを取得する
     */
    public DataType getData() {
        return mData;
    }

    /**
     * データを持っている場合true
     */
    public boolean hasData() {
        return mData != null;
    }

    /**
     * オブジェクトの変更通知を行なう
     */
    public void modified(DataType data) {
        mBus.post(data);
    }

    /**
     * オブジェクトに変更があったことを通知する
     */
    public void modified() {
        mBus.post(mData);
    }
}
