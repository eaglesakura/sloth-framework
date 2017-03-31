package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.event.LifecycleEventImpl;

public class ServiceLifecycle extends Lifecycle {

    public void onCreate() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnCreate));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStart));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnResume));
    }

    public void onDestroy() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnPause));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStop));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnDestroy));
    }
}
