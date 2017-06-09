package com.eaglesakura.sloth.app.lifecycle;


import com.eaglesakura.sloth.app.lifecycle.event.LifecycleEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnActivityResultEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnAttachEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnCreateEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnCreateOptionsMenuEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnCreateViewEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnSaveInstanceStateEvent;
import com.eaglesakura.sloth.app.lifecycle.event.State;

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
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnStart));
    }

    @UiThread
    @Override
    public void onResume() {
        super.onResume();
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnResume));
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
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnPause));
    }

    @UiThread
    public void onStop() {
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnStop));
    }

    @UiThread
    public void onDestroyView() {
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnDestroyView));
    }

    @UiThread
    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleSubject.onNext(LifecycleEvent.wrap(State.OnDestroy));
    }

    @UiThread
    public void onActivityResult(int requestCode, int result, Intent data) {
        mLifecycleSubject.onNext(new OnActivityResultEvent(requestCode, result, data));
    }
}
