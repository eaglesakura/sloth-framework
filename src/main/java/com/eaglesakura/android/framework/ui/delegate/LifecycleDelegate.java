package com.eaglesakura.android.framework.ui.delegate;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.DialogUtil;

import android.app.Dialog;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import rx.subjects.BehaviorSubject;

public class LifecycleDelegate {

    private BehaviorSubject<LifecycleState> mLifecycleSubject = BehaviorSubject.create(LifecycleState.NewObject);

    private SubscriptionController mSubscription = new SubscriptionController();

    /**
     * 監視対象とするDialog
     */
    private List<WeakReference<Dialog>> mAutoDismissDialogs = new LinkedList<>();

    public LifecycleDelegate() {
        mSubscription.bind(mLifecycleSubject);
    }

    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mLifecycleSubject.getValue();
    }

    public SubscriptionController getSubscription() {
        return mSubscription;
    }

    @UiThread
    public <T extends Dialog> T addAutoDismiss(@NonNull T dialog) {
        compactAutoDismissDialogs();
        if (dialog != null) {
            mAutoDismissDialogs.add(new WeakReference<>(dialog));
        }
        return dialog;
    }

    /**
     * Dialogを全て開放する
     */
    @UiThread
    public void compactAutoDismissDialogs() {
        Iterator<WeakReference<Dialog>> iterator = mAutoDismissDialogs.iterator();
        while (iterator.hasNext()) {
            WeakReference<Dialog> next = iterator.next();
            Dialog dialog = next.get();
            if (dialog == null) {
                iterator.remove();
            }
        }
    }

    @CallSuper
    @UiThread
    public void onCreate(Bundle state) {
        mLifecycleSubject.onNext(LifecycleState.OnCreated);
    }

    @CallSuper
    @UiThread
    public void onStart() {
        mLifecycleSubject.onNext(LifecycleState.OnStarted);
    }

    @CallSuper
    @UiThread
    public void onResume() {
        mLifecycleSubject.onNext(LifecycleState.OnResumed);
        compactAutoDismissDialogs();
    }

    @CallSuper
    @UiThread
    public void onPause() {
        mLifecycleSubject.onNext(LifecycleState.OnPaused);
        compactAutoDismissDialogs();
    }

    @CallSuper
    @UiThread
    public void onStop() {
        mLifecycleSubject.onNext(LifecycleState.OnStopped);
    }

    @CallSuper
    @UiThread
    public void onDestroy() {
        {
            Iterator<WeakReference<Dialog>> iterator = mAutoDismissDialogs.iterator();
            while (iterator.hasNext()) {
                Dialog dialog = iterator.next().get();
                if (dialog != null) {
                    FwLog.widget("AutoDismiss :: %s", dialog.getClass());
                    DialogUtil.dismiss(dialog);
                }
            }
            mAutoDismissDialogs.clear();
        }

        mLifecycleSubject.onNext(LifecycleState.OnDestroyed);
    }

    /**
     * UIスレッドで実行されたならばそのまでrunnableを実行し、そうでないならUIスレッドキューに登録する
     */
    public void runOnUiThread(@NonNull Runnable runnable) {
        UIHandler.postUIorRun(runnable);
    }

    /**
     * UIに関わる処理を非同期で実行する。
     *
     * 処理順を整列するため、非同期・直列処理されたあと、アプリがフォアグラウンドのタイミングでコールバックされる。
     */
    public <T> RxTaskBuilder<T> asyncUI(RxTask.Async<T> background) {
        return async(SubscribeTarget.Pipeline, ObserveTarget.Foreground, background);
    }

    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    public <T> RxTaskBuilder<T> async(SubscribeTarget subscribe, ObserveTarget observe, RxTask.Async<T> background) {
        return new RxTaskBuilder<T>(mSubscription)
                .subscribeOn(subscribe)
                .observeOn(observe)
                .async(background);
    }
}
