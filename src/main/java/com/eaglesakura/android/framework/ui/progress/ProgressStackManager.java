package com.eaglesakura.android.framework.ui.progress;

import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.PendingCallbackQueue;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProgressStackManager {
    List<ProgressToken> mTokens = new ArrayList<>();

    @NonNull
    final PendingCallbackQueue mCallbackQueue;

    @NonNull
    Listener mListener;

    public ProgressStackManager(@NonNull PendingCallbackQueue callbackQueue) {
        mCallbackQueue = callbackQueue;
    }

    public void setListener(@NonNull Listener listener) {
        mListener = listener;
    }

    private void sort() {
        synchronized (mTokens) {
            Collections.sort(mTokens, (a, b) -> Float.compare(a.getPriority(), b.getPriority()));
        }
    }

    /**
     * 優先すべきトークンを取得する
     */
    @Nullable
    public ProgressToken getTopToken() {
        synchronized (mTokens) {
            if (mTokens.isEmpty()) {
                return null;
            } else {
                return mTokens.get(0);
            }
        }
    }

    public void push(@NonNull ProgressToken token) {
        synchronized (mTokens) {
            ProgressToken topToken = getTopToken();
            mTokens.add(token);
            sort();
            if (topToken == null) {
                // 処理が開始された
                mCallbackQueue.run(CallbackTime.Foreground, () -> {
                    mListener.onProgressStarted(this, token);
                });
            } else {
                // 切り替わった？
                ProgressToken newTopToken = getTopToken();
                if (topToken != newTopToken) {
                    // 新しいトークンを発信する
                    mCallbackQueue.run(CallbackTime.Foreground, () -> {
                        mListener.onProgressTopChanged(this, newTopToken);
                    });
                }
            }
        }
    }

    void pop(@NonNull ProgressToken token) {
        synchronized (mTokens) {
            ProgressToken topToken = getTopToken();

            mTokens.remove(token);
            sort();

            if (mTokens.isEmpty()) {
                // 処理が終了した
                mCallbackQueue.run(CallbackTime.Foreground, () -> {
                    mListener.onProgressFinished(this);
                });
            } else {
                // トークンが切り替わった
                ProgressToken newTopToken = getTopToken();
                if (newTopToken != topToken) {
                    mCallbackQueue.run(CallbackTime.Foreground, () -> {
                        mListener.onProgressTopChanged(this, newTopToken);
                    });
                }
            }
        }
    }

    /**
     * トークンが
     */
    public boolean hasTokens() {
        synchronized (mTokens) {
            return !mTokens.isEmpty();
        }
    }

    public interface Listener {

        /**
         * 処理が開始された
         */
        @UiThread
        void onProgressStarted(@NonNull ProgressStackManager self, @NonNull ProgressToken token);

        /**
         * 優先度のトップが切り替わった
         */
        @UiThread
        void onProgressTopChanged(@NonNull ProgressStackManager self, @NonNull ProgressToken topToken);

        /**
         * 全ての処理が完了した
         */
        @UiThread
        void onProgressFinished(@NonNull ProgressStackManager self);
    }

}
