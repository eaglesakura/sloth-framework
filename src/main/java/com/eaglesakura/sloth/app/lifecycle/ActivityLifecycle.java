package com.eaglesakura.sloth.app.lifecycle;

import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.event.LifecycleEventImpl;
import com.eaglesakura.cerberus.event.OnActivityResultEvent;
import com.eaglesakura.cerberus.event.OnCreateEvent;
import com.eaglesakura.cerberus.event.OnRestoreEvent;
import com.eaglesakura.cerberus.event.OnSaveInstanceStateEvent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.UiThread;

public class ActivityLifecycle extends UiLifecycle {
    @UiThread
    public void onCreate(Bundle savedInstanceState) {
        mLifecycleSubject.onNext(new OnCreateEvent(savedInstanceState));
    }

    @UiThread
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mLifecycleSubject.onNext(new OnRestoreEvent(savedInstanceState));
    }

    @UiThread
    public void onSaveInstanceState(Bundle outState) {
        mLifecycleSubject.onNext(new OnSaveInstanceStateEvent(outState));
    }

    @UiThread
    public void onStart() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStart));
    }

    @UiThread
    @Override
    public void onResume() {
        super.onResume();
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnResume));
    }

    @UiThread
    @Override
    public void onPause() {
        super.onPause();
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnPause));
    }

    @UiThread
    public void onStop() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnStop));
    }

    @UiThread
    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnDestroy));
    }

    @UiThread
    public void onActivityResult(int requestCode, int result, Intent data) {
        mLifecycleSubject.onNext(new OnActivityResultEvent(requestCode, result, data));
    }
}
