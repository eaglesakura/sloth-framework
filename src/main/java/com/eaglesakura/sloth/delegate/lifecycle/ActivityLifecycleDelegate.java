package com.eaglesakura.sloth.delegate.lifecycle;

import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.event.LifecycleEventImpl;
import com.eaglesakura.cerberus.event.OnCreateEvent;
import com.eaglesakura.cerberus.event.OnRestoreEvent;
import com.eaglesakura.cerberus.event.OnSaveEvent;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;

public class ActivityLifecycleDelegate extends UiLifecycleDelegate {
    @CallSuper
    @UiThread
    public void onCreate(Bundle savedInstanceState) {
        mLifecycleSubject.onNext(new OnCreateEvent(savedInstanceState));
    }

    @CallSuper
    @UiThread
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mLifecycleSubject.onNext(new OnRestoreEvent(savedInstanceState));
    }

    @CallSuper
    @UiThread
    public void onSaveInstanceState(Bundle outState) {
        mLifecycleSubject.onNext(new OnSaveEvent(outState));
    }

    @CallSuper
    @UiThread
    public void onStart() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStarted));
    }

    @CallSuper
    @UiThread
    @Override
    public void onResume() {
        super.onResume();
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnResumed));
    }

    @CallSuper
    @UiThread
    @Override
    public void onPause() {
        super.onPause();
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnPaused));
    }

    @CallSuper
    @UiThread
    public void onStop() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStopped));
    }

    @CallSuper
    @UiThread
    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnDestroyed));
    }
}
