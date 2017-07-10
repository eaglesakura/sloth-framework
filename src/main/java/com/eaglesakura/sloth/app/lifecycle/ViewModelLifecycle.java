package com.eaglesakura.sloth.app.lifecycle;


import com.eaglesakura.sloth.annotation.Experimental;
import com.eaglesakura.sloth.app.lifecycle.event.LifecycleEvent;
import com.eaglesakura.sloth.app.lifecycle.event.State;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;

/**
 * ViewModelごとに管理されるライフサイクル
 */
@Experimental
public class ViewModelLifecycle extends Lifecycle {

    @CallSuper
    @UiThread
    public void onCreate() {
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnCreate));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnStart));
    }

    @CallSuper
    @UiThread
    public void onActive() {
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnStart));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnResume));
    }

    @CallSuper
    @UiThread
    public void onInactive() {
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnPause));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnStop));
    }

    @CallSuper
    @UiThread
    public void onDestroy() {
        if (getLifecycleState() == State.OnDestroy) {
            return;
        }

        super.onDestroy();
        if (getLifecycleState() != State.OnStop) {
            onInactive();
        }
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnDestroy));
    }
}
