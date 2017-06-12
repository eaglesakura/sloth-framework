package com.eaglesakura.sloth.app.support;

import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.sloth.app.lifecycle.event.LifecycleEvent;
import com.eaglesakura.sloth.app.lifecycle.event.OnAttachEvent;
import com.eaglesakura.sloth.app.lifecycle.event.State;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import io.reactivex.functions.Consumer;

/**
 * {@link com.eaglesakura.android.garnet.Garnet} を自動的に適用する。
 *
 * onAttachタイミングでInjectが実行され、onAfterInjection()が同期的に呼び出される。
 */
public class GarnetSupport {

    public static void bind(FragmentLifecycle lifecycle, Callback callback) {
        lifecycle.subscribe(new Consumer<LifecycleEvent>() {

            /**
             * 既に依存構築済であればtrue
             */
            boolean mInjectedInstance = false;

            @Override
            public void accept(LifecycleEvent event) throws Exception {
                if (event.getState() == State.OnAttach) {
                    onAttach(((OnAttachEvent) event));
                }
            }

            private void onAttach(OnAttachEvent event) {
                if (!mInjectedInstance) {
                    Garnet.Builder builder = callback.newInjectionBuilder(event.getContext());
                    builder.depend(Context.class, event.getContext()).inject();
                    callback.onAfterInjection();
                    mInjectedInstance = true;
                }
            }
        });
    }

    public interface Callback {
        /**
         * 依存注入が完了した
         */
        void onAfterInjection();

        /**
         * 依存注入用のBuilderを開始する
         */
        @NonNull
        Garnet.Builder newInjectionBuilder(Context context);
    }
}
