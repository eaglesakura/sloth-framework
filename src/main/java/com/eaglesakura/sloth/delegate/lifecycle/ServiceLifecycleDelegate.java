package com.eaglesakura.sloth.delegate.lifecycle;

import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.event.LifecycleEventImpl;

public class ServiceLifecycleDelegate extends LifecycleDelegate {

    public void onCreate() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnCreated));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStarted));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnResumed));
    }

    public void onDestroy() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnPaused));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStopped));
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnDestroyed));
    }
}
