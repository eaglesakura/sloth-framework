package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.cerberus.LifecycleEvent;
import com.eaglesakura.cerberus.LifecycleState;

public class ServiceLifecycle extends Lifecycle {

    public void onCreate() {
        mLifecycleSubject.onNext(LifecycleEvent.wrap(LifecycleState.OnCreate));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(LifecycleState.OnStart));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(LifecycleState.OnResume));
    }

    public void onDestroy() {
        super.onDestroy();
        mLifecycleSubject.onNext(LifecycleEvent.wrap(LifecycleState.OnPause));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(LifecycleState.OnStop));
        mLifecycleSubject.onNext(LifecycleEvent.wrap(LifecycleState.OnDestroy));
    }
}
