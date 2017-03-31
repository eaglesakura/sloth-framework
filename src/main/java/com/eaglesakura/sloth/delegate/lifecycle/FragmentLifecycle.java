package com.eaglesakura.sloth.delegate.lifecycle;

import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.event.LifecycleEventImpl;
import com.eaglesakura.cerberus.event.OnActivityResultEvent;
import com.eaglesakura.cerberus.event.OnAttachEvent;
import com.eaglesakura.cerberus.event.OnCreateEvent;
import com.eaglesakura.cerberus.event.OnCreateOptionsMenuEvent;
import com.eaglesakura.cerberus.event.OnCreateViewEvent;
import com.eaglesakura.cerberus.event.OnSaveInstanceStateEvent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewGroup;

public class FragmentLifecycle extends UiLifecycle {


    @UiThread
    public void onAttach(Context context) {
        mLifecycleSubject.onNext(new OnAttachEvent(context));
    }

    @UiThread
    public void onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLifecycleSubject.onNext(new OnCreateViewEvent(inflater, container, savedInstanceState));
    }


    @UiThread
    public void onCreate(Bundle savedInstanceState) {
        mLifecycleSubject.onNext(new OnCreateEvent(savedInstanceState));
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mLifecycleSubject.onNext(new OnCreateOptionsMenuEvent(menu, inflater));
    }

    @UiThread
    public void onSaveInstanceState(Bundle outState) {
        mLifecycleSubject.onNext(new OnSaveInstanceStateEvent(outState));
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
    public void onDestroyView() {
        mLifecycleSubject.onNext(new LifecycleEventImpl(LifecycleState.OnDestroyView));
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
