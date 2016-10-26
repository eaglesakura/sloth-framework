package com.eaglesakura.android.framework.delegate.task;

import com.eaglesakura.android.framework.delegate.lifecycle.LifecycleDelegate;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.BackgroundTaskBuilder;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.PendingCallbackQueue;

/**
 * ライフサイクルとリンクしたタスクを扱う
 *
 * ライフサイクルのリンクした処理を移譲する。
 */
public class LifecycleTask<DelegateType extends LifecycleDelegate> {
    protected final DelegateType mLifecycleDelegate;

    public LifecycleTask(DelegateType delegate) {
        mLifecycleDelegate = delegate;
    }

    public DelegateType getLifecycleDelegate() {
        return mLifecycleDelegate;
    }

    public LifecycleState getLifecycleState() {
        return mLifecycleDelegate.getLifecycleState();
    }

    public <T> BackgroundTaskBuilder<T> asyncUI(BackgroundTask.Async<T> background) {
        return mLifecycleDelegate.asyncUI(background);
    }

    public PendingCallbackQueue getCallbackQueue() {
        return mLifecycleDelegate.getCallbackQueue();
    }

    public <T> BackgroundTaskBuilder<T> async(ExecuteTarget execute, CallbackTime time, BackgroundTask.Async<T> background) {
        return mLifecycleDelegate.async(execute, time, background);
    }
}
