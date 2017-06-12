package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.sloth.app.lifecycle.event.LifecycleEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnActivityResultEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnCreateEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnRestoreEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnSaveInstanceStateEvent;
import com.eaglesakura.sloth.app.lifecycle.event.State;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;

public class ActivityLifecycle extends UiLifecycle {
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
        mLifecycleSubject.onNext(new OnSaveInstanceStateEvent(outState));
    }

    @CallSuper
    @UiThread
    public void onStart() {
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnStart));
    }

    @CallSuper
    @UiThread
    @Override
    public void onResume() {
        super.onResume();
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnResume));
    }

    @CallSuper
    @UiThread
    @Override
    public void onPause() {
        super.onPause();
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnPause));
    }

    @CallSuper
    @UiThread
    public void onStop() {
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnStop));
    }

    @CallSuper
    @UiThread
    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnDestroy));
    }

    @CallSuper
    @UiThread
    public void onActivityResult(int requestCode, int result, Intent data) {
        mLifecycleSubject.onNext(new OnActivityResultEvent(requestCode, result, data));
    }
}
