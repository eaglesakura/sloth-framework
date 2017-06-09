package com.eaglesakura.sloth.app.lifecycle;


import com.eaglesakura.sloth.app.lifecycle.event.LifecycleEvent;
import com.eaglesakura.sloth.app.lifecycle.event.State;

public class ServiceLifecycle extends Lifecycle {

    public void onCreate() {
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnCreate));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnStart));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnResume));
    }

    public void onDestroy() {
        super.onDestroy();
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnPause));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnStop));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnDestroy));
    }
}
