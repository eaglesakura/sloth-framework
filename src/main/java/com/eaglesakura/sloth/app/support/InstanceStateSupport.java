package com.eaglesakura.sloth.app.support;

import com.eaglesakura.android.saver.LightSaver;
import com.eaglesakura.cerberus.LifecycleEvent;
import com.eaglesakura.cerberus.event.OnCreateEvent;
import com.eaglesakura.cerberus.event.OnSaveInstanceStateEvent;
import com.eaglesakura.sloth.app.lifecycle.ActivityLifecycle;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;

import android.app.Activity;
import android.support.v4.app.Fragment;

import rx.functions.Action1;

/**
 * LightSaverによるステート保持を行う
 *
 * 保持したいステートに対し、 @{@link com.eaglesakura.android.saver.BundleState} Annotationをつけることで自動的に保存を行う。
 */
public class InstanceStateSupport {
    public static void bind(FragmentLifecycle lifecycle, Fragment fragment) {
        lifecycle.subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getState()) {
                    case OnSaveInstanceState:
                        save(((OnSaveInstanceStateEvent) lifecycleEvent));
                        break;
                    case OnCreate:
                        restore(((OnCreateEvent) lifecycleEvent));
                        break;
                }
            }

            void restore(OnCreateEvent event) {
                LightSaver.create(event.getBundle())
                        .target(fragment)
                        .restore();
            }

            void save(OnSaveInstanceStateEvent event) {
                LightSaver.create(event.getBundle())
                        .target(fragment)
                        .save();
            }
        });
    }

    public static void bind(ActivityLifecycle lifecycle, Activity activity) {
        lifecycle.subscribe(new Action1<LifecycleEvent>() {
            @Override
            public void call(LifecycleEvent lifecycleEvent) {
                switch (lifecycleEvent.getState()) {
                    case OnSaveInstanceState:
                        save(((OnSaveInstanceStateEvent) lifecycleEvent));
                        break;
                    case OnCreate:
                        restore(((OnCreateEvent) lifecycleEvent));
                        break;
                }
            }

            void restore(OnCreateEvent event) {
                LightSaver.create(event.getBundle())
                        .target(activity)
                        .restore();
            }

            void save(OnSaveInstanceStateEvent event) {
                LightSaver.create(event.getBundle())
                        .target(activity)
                        .save();
            }
        });
    }
}
