package com.eaglesakura.sloth.app.support;

import com.eaglesakura.android.saver.LightSaver;
import com.eaglesakura.sloth.app.lifecycle.ActivityLifecycle;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.sloth.app.lifecycle.event.LifecycleEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnCreateEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnSaveInstanceStateEvent;

import android.app.Activity;
import android.support.v4.app.Fragment;

import io.reactivex.functions.Consumer;

/**
 * LightSaverによるステート保持を行う
 *
 * 保持したいステートに対し、 @{@link com.eaglesakura.android.saver.BundleState} Annotationをつけることで自動的に保存を行う。
 */
public class InstanceStateSupport {
    public static void bind(FragmentLifecycle lifecycle, Fragment fragment) {
        lifecycle.subscribe(new Consumer<LifecycleEvent>() {

            @Override
            public void accept(LifecycleEvent lifecycleEvent) throws Exception {
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
        lifecycle.subscribe(new Consumer<LifecycleEvent>() {
            @Override
            public void accept(LifecycleEvent lifecycleEvent) throws Exception {
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
