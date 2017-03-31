package com.eaglesakura.sloth.data;

import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.sloth.app.lifecycle.Lifecycle;
import com.squareup.otto.Bus;

import rx.Subscription;

/**
 * ライフサイクルにリンクしたイベントバスを構築する
 *
 * Destroyされたオブジェクトにはイベントを送信しない。
 */
public class LifecycleLinkBus {

    protected final Bus mBus;

    protected final Lifecycle mLifecycleDelegate;

    private Subscription mSubscribe;

    protected LifecycleLinkBus(Lifecycle lifecycleDelegate, Bus bus, Object receiver) {
        mLifecycleDelegate = lifecycleDelegate;
        mBus = bus;
        mBus.register(receiver);
        mSubscribe = mLifecycleDelegate.getCallbackQueue().getObservable().subscribe(event -> {
            if (event.getState() == LifecycleState.OnDestroy) {
                mBus.unregister(receiver);
                mSubscribe.unsubscribe();
            }
        });
    }


    /**
     * Busへ登録する。
     * 登録されたReceiverはonDestroyイベントで自動的にunscribeされる。
     *
     * @param lifecycleDelegate ライフサイクル
     * @param bus               対象Bus
     * @param receiver          対象Receiver
     */
    public static void register(Lifecycle lifecycleDelegate, Bus bus, Object receiver) {
        new LifecycleLinkBus(lifecycleDelegate, bus, receiver);
    }
}
