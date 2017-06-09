package com.eaglesakura.sloth.data;

import com.eaglesakura.sloth.app.lifecycle.Lifecycle;
import com.eaglesakura.sloth.app.lifecycle.event.State;
import com.squareup.otto.Bus;

import io.reactivex.disposables.Disposable;

/**
 * MEMO: Android Architecture Components / LiveData / ViewModelへの移行を推奨
 *
 * ライフサイクルにリンクしたイベントバスを構築する
 *
 * Destroyされたオブジェクトにはイベントを送信しない。
 */
public class LifecycleLinkBus {

    protected final Bus mBus;

    protected final Lifecycle mLifecycleDelegate;

    private Disposable mSubscribe;

    protected LifecycleLinkBus(Lifecycle lifecycleDelegate, Bus bus, Object receiver) {
        mLifecycleDelegate = lifecycleDelegate;
        mBus = bus;
        mBus.register(receiver);
        mSubscribe = mLifecycleDelegate.subscribe(event -> {
            if (event.getState() == State.OnDestroy) {
                mBus.unregister(receiver);
                mSubscribe.dispose();
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
