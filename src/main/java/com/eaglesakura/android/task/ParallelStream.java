package com.eaglesakura.android.task;

import com.eaglesakura.android.framework.delegate.lifecycle.ServiceLifecycleDelegate;
import com.eaglesakura.android.rx.BackgroundTask;
import com.eaglesakura.android.rx.CallbackTime;
import com.eaglesakura.android.rx.ExecuteTarget;
import com.eaglesakura.android.rx.ParallelTaskGroup;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.lambda.ResultAction1;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 順不同、かつ複数スレッドでタスクを処理する
 */
public class ParallelStream<Src> {
    final ServiceLifecycleDelegate mDelegate = new ServiceLifecycleDelegate();

    final List<Src> mSource;

    ResultAction1<Src, ?> mAction;

    CancelCallback mCancelCallback;

    ParallelStream(List<Src> source) {
        mDelegate.onCreate();
        mSource = source;
    }

    public ParallelStream<Src> each(ResultAction1<Src, ?> action) {
        mAction = action;
        return this;
    }

    public ParallelStream<Src> cancelSignal(CancelCallback cancelCallback) {
        mCancelCallback = cancelCallback;
        return this;
    }

    public <T> List<T> toList() throws Throwable {
        List<T> result = new ArrayList<>();

        ParallelTaskGroup<?> group = new ParallelTaskGroup<>(new ParallelTaskGroup.Callback<Object>() {
            @Nullable
            @Override
            public BackgroundTask onNextTask(@NonNull ParallelTaskGroup group) throws Throwable {
                Object obj;
                synchronized (mSource) {
                    if (mSource.isEmpty()) {
                        return null;
                    }
                    obj = mSource.remove(0);
                }
                return mDelegate.async(ExecuteTarget.LocalParallel, CallbackTime.Alive, task -> {
                    return mAction.action((Src) obj);
                }).cancelSignal(task -> {
                    return CallbackUtils.isCanceled(mCancelCallback);
                }).start();
            }

            @Override
            public void onTaskCompleted(@NonNull ParallelTaskGroup group, @NonNull BackgroundTask task) throws Throwable {
                result.add((T) task.getResult());
            }
        });

        group.setMaxParallelTasks(8);
        group.await();

        return result;
    }

    /**
     * 開放処理を行なう
     */
    public void dispose() {
        mDelegate.onDestroy();
    }

    public static <T> ParallelStream<T> of(@NonNull Collection<T> items) {
        return new ParallelStream<>(new ArrayList<>(items));
    }
}
