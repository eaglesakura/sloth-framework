package com.eaglesakura.sloth.app.support;

import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.cerberus.LifecycleEvent;
import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.event.OnAttachEvent;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;

import android.content.Context;
import android.support.annotation.NonNull;

import rx.functions.Action1;

/**
 * {@link com.eaglesakura.android.garnet.Garnet} を自動的に適用する。
 *
 * onAttachタイミングでInjectが実行され、onAfterInjection()が同期的に呼び出される。
 */
public class GarnetSupport {

    public static void bind(FragmentLifecycle lifecycle, Callback callback) {
        lifecycle.subscribe(new Action1<LifecycleEvent>() {

            /**
             * 既に依存構築済であればtrue
             */
            boolean mInjectedInstance = false;

            @Override
            public void call(LifecycleEvent event) {
                if (event.getState() == LifecycleState.OnAttach) {
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
