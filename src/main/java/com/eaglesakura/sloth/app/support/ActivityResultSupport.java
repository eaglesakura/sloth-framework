package com.eaglesakura.sloth.app.support;

import com.eaglesakura.android.oari.ActivityResult;
import com.eaglesakura.cerberus.LifecycleEvent;
import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.event.OnActivityResultEvent;
import com.eaglesakura.sloth.app.lifecycle.ActivityLifecycle;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import rx.functions.Action1;

/**
 * {@link com.eaglesakura.android.oari.OnActivityResult} のハンドリングを自動で行う
 */
public class ActivityResultSupport {
    public static void bind(FragmentLifecycle lifecycle, Fragment receiver) {
        lifecycle.subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent event) {
                if (event.getState() == LifecycleState.OnActivityResult) {
                    onActivityResult(((OnActivityResultEvent) event));
                }
            }

            private void onActivityResult(OnActivityResultEvent event) {
                ActivityResult.invokeRecursive(receiver, event.getRequestCode(), event.getResult(), event.getData());
            }
        });
    }

    public static void bind(ActivityLifecycle lifecycle, AppCompatActivity receiver) {
        lifecycle.subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent event) {
                if (event.getState() == LifecycleState.OnActivityResult) {
                    onActivityResult(((OnActivityResultEvent) event));
                }
            }

            private void onActivityResult(OnActivityResultEvent event) {
                ActivityResult.invoke(receiver, event.getRequestCode(), event.getResult(), event.getData());
            }
        });
    }
}
