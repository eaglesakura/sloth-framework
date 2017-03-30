package com.eaglesakura.android.framework.delegate.task;

import com.eaglesakura.android.framework.delegate.lifecycle.LifecycleDelegate;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.BackgroundTaskBuilder;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.PendingCallbackQueue;

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
