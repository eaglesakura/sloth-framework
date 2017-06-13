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
