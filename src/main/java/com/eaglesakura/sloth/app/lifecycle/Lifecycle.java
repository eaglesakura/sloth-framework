package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.BackgroundTaskBuilder;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.cerberus.LifecycleEvent;
import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.PendingCallbackQueue;
import com.eaglesakura.cerberus.event.LifecycleEventImpl;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

public abstract class Lifecycle {

    protected final BehaviorSubject<LifecycleEvent> mLifecycleSubject = BehaviorSubject.create(new LifecycleEventImpl(LifecycleState.NewObject));

    protected final PendingCallbackQueue mCallbackQueue = new PendingCallbackQueue();

    /**
     * 削除時に実行されるアクション一覧
     */
    protected List<Action1<Lifecycle>> mDestroyActions = new ArrayList<>();

    public Lifecycle() {
        mCallbackQueue.bind(mLifecycleSubject);
    }

    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mLifecycleSubject.getValue().getState();
    }

    public PendingCallbackQueue getCallbackQueue() {
        return mCallbackQueue;
    }

    public Subscription subscribe(Action1<? super LifecycleEvent> onNext) {
        return mLifecycleSubject.subscribe(onNext);
    }

    public Subscription unsafeSubscribe(Subscriber<? super LifecycleEvent> subscriber) {
        return mLifecycleSubject.unsafeSubscribe(subscriber);
    }

    /**
     * 指定したタイミングまで保留対応を行うSubscribeを行う。
     *
     * onNextのコールは必ずUIスレッドで行われる。
     * 通知を削除したい場合は戻り値Subscriptionをunsubscribeする。
     * このライフサイクルがDestroyされたとき、自動的にSubscriptionはunsubscribeされる。
     *
     * @param time    コールバックタイミング
     * @param subject 対象Subject
     * @param onNext  実際に受け取るコールバック
     * @return ラップされたSubscription
     */
    public Subscription interrupt(CallbackTime time, Subject subject, Action1 onNext) {
        if (getLifecycleState() == LifecycleState.OnDestroy) {
            return null;
        }

        Subscription sub = subject.subscribe(value -> {
            getCallbackQueue().run(time, () -> onNext.call(value));
        });
        synchronized (mDestroyActions) {
            mDestroyActions.add(it -> {
                try {
                    sub.unsubscribe();
                } catch (Throwable e) {
                }
            });
        }
        return sub;
    }

    /**
     * UIに関わる処理を非同期で実行する。
     *
     * 処理順を整列するため、非同期・直列処理されたあと、アプリがフォアグラウンドのタイミングでコールバックされる。
     */
    public <T> BackgroundTaskBuilder<T> asyncQueue(BackgroundTask.Async<T> background) {
        return async(ExecuteTarget.LocalQueue, CallbackTime.Foreground, background);
    }

    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    public <T> BackgroundTaskBuilder<T> async(ExecuteTarget execute, CallbackTime time, BackgroundTask.Async<T> background) {
        return new BackgroundTaskBuilder<T>(mCallbackQueue)
                .executeOn(execute)
                .callbackOn(time)
                .async(background);
    }

    /**
     * ライフサイクルが終了した
     */
    @CallSuper
    @UiThread
    protected void onDestroy() {
        synchronized (mDestroyActions) {
            for (Action1<Lifecycle> action : mDestroyActions) {
                action.call(this);
            }
            mDestroyActions.clear();
        }
    }
}
