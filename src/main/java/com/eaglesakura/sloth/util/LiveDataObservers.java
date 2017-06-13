package com.eaglesakura.sloth.util;

import com.eaglesakura.lambda.Matcher1;

import android.arch.lifecycle.Observer;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class LiveDataObservers {
    private LiveDataObservers() {
    }

    /**
     * 値が以前とは変更になった場合のみ通知する
     * {@link Object#equals(Object)} がオーバーライドされていない場合や、巨大なオブジェクトの比較には向かない。
     *
     * @param origin 元のObserver
     */
    public static <T> Observer<T> modified(@NonNull Observer<T> origin) {
        return new Observer<T>() {
            T mOldValue;

            @Override
            public void onChanged(@Nullable T newValue) {
                if (mOldValue == null || !mOldValue.equals(newValue)) {
                    origin.onChanged(newValue);
                }
                mOldValue = newValue;
            }
        };
    }

    /**
     * 特定条件にマッチした場合のみoriginに通知する
     *
     * @param matcher マッチ条件, trueを返却したらoriginに通知する
     * @param origin  元のObserver
     */
    public static <T> Observer<T> match(@NonNull Matcher1<T> matcher, @NonNull Observer<T> origin) {
        return value -> {
            try {
                if (matcher.match(value)) {
                    origin.onChanged(value);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * 一度だけ受信するObserverへ変換する
     *
     * @param origin 元のObserver
     * @return 一度だけ受信するObserver
     */
    @NonNull
    public static <T> Observer<T> single(@NonNull Observer<T> origin) {
        return limited(1, origin);
    }

    /**
     * 指定回数だけ受信するObserverへ変換する
     *
     * @param origin もとのObserver処理
     * @return 一度だけ受信をサポートするObserver
     */
    @NonNull
    public static <T> Observer<T> limited(@IntRange(from = 1) int maxCallbacks, @NonNull Observer<T> origin) {
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
}
