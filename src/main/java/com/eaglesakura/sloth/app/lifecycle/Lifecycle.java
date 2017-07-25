package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.BackgroundTaskBuilder;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.cerberus.PendingCallbackQueue;
import com.eaglesakura.sloth.annotation.Experimental;
import com.eaglesakura.sloth.app.lifecycle.event.LifecycleEvent;
import com.eaglesakura.sloth.app.lifecycle.event.State;
import com.eaglesakura.thread.Holder;

import android.arch.lifecycle.LifecycleRegistryOwner;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;

/**
 * 詳細なライフサイクル管理を行う
 *
 * 内部では {@link LifecycleRegistryOwner} を保持し、このオブジェクトのライフサイクル変動に合わせて {@link
 * android.arch.lifecycle.LifecycleRegistry#handleLifecycleEvent(android.arch.lifecycle.Lifecycle.Event)}
 * が自動的に呼ばれる。
 */
public abstract class Lifecycle {

    protected final BehaviorSubject<LifecycleEvent> mLifecycleSubject = BehaviorSubject.createDefault(LifecycleEvent.wrap(State.NewObject));

    protected final PendingCallbackQueue mCallbackQueue = new PendingCallbackQueue();

    /**
     * 削除時に実行されるアクション一覧
     */
    @Experimental
    protected List<Consumer<Lifecycle>> mDestroyActions = new ArrayList<>();

    /**
     * ライフサイクル管理オブジェクト
     */
    @NonNull
    private LifecycleRegistryOwner mLifecycleRegistryOwner;

    public Lifecycle() {
        this(new SlothLifecycleRegistry());
    }

    public Lifecycle(@NonNull LifecycleRegistryOwner lifecycleRegistryOwner) {
        mLifecycleRegistryOwner = lifecycleRegistryOwner;
        mCallbackQueue.bind(lifecycleRegistryOwner);
        subscribe(it -> {
            android.arch.lifecycle.Lifecycle.Event event;
            switch (it.getState()) {
                case OnCreate:
                    event = android.arch.lifecycle.Lifecycle.Event.ON_CREATE;
                    break;
                case OnStart:
                    event = android.arch.lifecycle.Lifecycle.Event.ON_START;
                    break;
                case OnResume:
                    event = android.arch.lifecycle.Lifecycle.Event.ON_RESUME;
                    break;
                case OnStop:
                    event = android.arch.lifecycle.Lifecycle.Event.ON_STOP;
                    break;
                case OnDestroy:
                    event = android.arch.lifecycle.Lifecycle.Event.ON_DESTROY;
                    break;
                default:
                    return;
            }
            getLifecycleRegistry().getLifecycle().handleLifecycleEvent(event);
        });
    }

    /**
     * Android Architecture Components互換オブジェクトを生成する
     */
    public LifecycleRegistryOwner getLifecycleRegistry() {
        return mLifecycleRegistryOwner;
    }

    /**
     * ライフサイクル状態を取得する
     */
    public State getLifecycleState() {
        return mLifecycleSubject.getValue().getState();
    }

    public PendingCallbackQueue getCallbackQueue() {
        return mCallbackQueue;
    }

    /**
     * ライフサイクルイベントをハンドリングする。
     *
     * OnDestroyタイミングで自動的に {@link Disposable#dispose()} が行われる。
     */
    public Disposable subscribe(Consumer<? super LifecycleEvent> onNext) {
        Disposable sub = mLifecycleSubject.subscribe(onNext);
        synchronized (mDestroyActions) {
            mDestroyActions.add(it -> {
                UIHandler.postUI(() -> {
                    if (!sub.isDisposed()) {
                        sub.dispose();
                    }
                });
            });
        }
        return sub;
    }

    /**
     * 指定したタイミングまで保留対応を行うSubscribeを行う。
     *
     * onNextのコールは必ずUIスレッドで行われる。
     * 通知を削除したい場合は戻り値 {@link Disposable} を {@link Disposable#dispose()} する。
     * このライフサイクルがDestroyされたとき、自動的にdispose()される。
     *
     * @param time    コールバックタイミング
     * @param subject 対象Subject
     * @param onNext  実際に受け取るコールバック
     * @return ラップされたSubscription
     */
    @Experimental
    public Disposable interrupt(CallbackTime time, Observable subject, Consumer onNext) {
        if (getLifecycleState() == State.OnDestroy) {
            return null;
        }

        Disposable sub = subject.subscribe(value -> {
            getCallbackQueue().run(time, () -> {
                try {
                    onNext.accept(value);
                } catch (Exception e) {
                }
            });
        });
        synchronized (mDestroyActions) {
            mDestroyActions.add(it -> {
                UIHandler.postUI(() -> {
                    if (!sub.isDisposed()) {
                        sub.dispose();
                    }
                });
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
            for (Consumer<Lifecycle> action : mDestroyActions) {
                try {
                    action.accept(this);
                } catch (Exception e) {
                }
            }
            mDestroyActions.clear();
        }
    }
}
