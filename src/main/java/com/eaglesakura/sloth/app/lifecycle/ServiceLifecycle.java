package com.eaglesakura.sloth.app.lifecycle;


import com.eaglesakura.sloth.app.lifecycle.event.LifecycleEvent;
import com.eaglesakura.sloth.app.lifecycle.event.State;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;

public class ServiceLifecycle extends Lifecycle {

    @CallSuper
    @UiThread
    public void onCreate() {
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnCreate));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnStart));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnResume));
    }

    @CallSuper
    @UiThread
    public void onDestroy() {
        super.onDestroy();
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnPause));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnStop));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnDestroy));
    }
}
