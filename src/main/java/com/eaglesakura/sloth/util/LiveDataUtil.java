package com.eaglesakura.sloth.util;

import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.util.Util;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * LiveData関連のUtil
 */
public class LiveDataUtil {
    private LiveDataUtil() {

    }

    /**
     * 一度だけ受信するObserverへ変換する
     *
     * @param origin 元のObserver
     * @return 一度だけ受信するObserver
     */
    @NonNull
    public static <T> Observer<T> singleObserver(@NonNull Observer<T> origin) {
        return limitedObserver(1, origin);
    }

    /**
     * 指定回数だけ受信するObserverへ変換する
     *
     * @param origin もとのObserver処理
     * @return 一度だけ受信をサポートするObserver
     */
    @NonNull
    public static <T> Observer<T> limitedObserver(@IntRange(from = 1) int maxCallbacks, @NonNull Observer<T> origin) {
        return new Observer<T>() {
            int mChangeCount = 0;

            @Override
            public void onChanged(@Nullable T t) {
                // コールバック回数が既定以内であれば処理する
                if (mChangeCount < maxCallbacks) {
                    synchronized (this) {
                        if (mChangeCount < maxCallbacks) {
                            origin.onChanged(t);
                        }
                        ++mChangeCount;
                    }
                }
            }
        };
    }

    /**
     * データの取得待ちを行う
     *
     * @param data           対象のLiveData
     * @param cancelCallback キャンセルチェック
     * @throws InterruptedException キャンセルされた場合例外として投げられる
     */
    @NonNull
    public static <T> T await(@NonNull LiveData<T> data, @NonNull CancelCallback cancelCallback) throws InterruptedException {
        while (!CallbackUtils.isCanceled(cancelCallback)) {
            T value = data.getValue();
            if (value != null) {
                return value;
            } else {
                Util.sleep(1);
            }
        }

        throw new InterruptedException("LiveData<T> await canceled");
    }
}
