package com.eaglesakura.sloth.delegate.task;

import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.BackgroundTaskBuilder;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.cerberus.error.TaskCanceledException;
import com.eaglesakura.lambda.Action1;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.material.widget.support.SupportCancelCallbackBuilder;
import com.eaglesakura.sloth.delegate.lifecycle.LifecycleDelegate;
import com.eaglesakura.util.Util;
import com.squareup.otto.AnnotatedHandlerFinder2;
import com.squareup.otto.Bus;
import com.squareup.otto.Bus2;
import com.squareup.otto.ThreadEnforcer;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.eaglesakura.sloth.util.AppSupportUtil.assertNotCanceled;

/**
 * Nullを許容したデータバスを構築する
 *
 * データハンドリングは必ずUIスレッドで行われる。
 * Nullを許容するため、DataBusそれ自体をpostする。
 * Subscribe側は、DataBusをextendsしたクラスを引数にしてデータを受け取る
 * ex. void onModified(ExampleDataBus bus){}
 *
 * 非同期処理でmodifiedされた場合にはキャッシュに一度データが保存され、通知の直前に上書きされる。
 */
public abstract class DataBus<DataType> {
    @Nullable
    Handler mHandler = UIHandler.getInstance();

    @NonNull
    final Bus mBus = new Bus2(ThreadEnforcer.ANY, Bus.DEFAULT_IDENTIFIER, AnnotatedHandlerFinder2.newInstance()) {
    };

    /**
     * データバス
     */
    @Nullable
    DataType mData;

    public DataBus() {
    }

    public DataBus(@Nullable DataType data) {
        mData = data;
    }

    public DataBus(@Nullable Handler handler, @Nullable DataType data) {
        mHandler = handler;
        mData = data;
    }

    public DataBus(@Nullable Handler handler) {
        mHandler = handler;
    }

    /**
     * ハンドリング対象のハンドラを設定する。
     * !=nullである場合、そのハンドラのスレッドでpost処理が行われる。
     * nullである場合、即座にpost処理が行われる。
     */
    public void setHandler(@Nullable Handler handler) {
        mHandler = handler;
    }

    public Bus getBus() {
        return mBus;
    }

    /**
     * イベントバスに対してレシーバを登録する
     */
    public void register(Object object) {
        mBus.register(object);
    }

    /**
     * イベントバスからレシーバを削除する
     */
    public void unregister(Object object) {
        mBus.unregister(object);
    }

    /**
     * 握っているデータを取得する
     */
    @Nullable
    public DataType getData() {
        return mData;
    }

    /**
     * データを持っている場合true
     */
    public boolean hasData() {
        return getData() != null;
    }

    /**
     * オブジェクトの変更通知を行なう
     */
    public void modified(DataType data) {
        AndroidThreadUtil.postOrRun(mHandler, () -> {
            mData = data;
            mBus.post(this);
        });
    }

    /**
     * オブジェクトに変更があったことを通知する
     */
    public void modified() {
        modified(mData);
    }

    /**
     * 持っているデータをクリアする
     */
    public void clear() {
        modified(null);
    }

    /**
     * データを持っている場合処理を実行する
     */
    public void ifPresent(Action1<DataType> action) {
        try {
            DataType data = getData();
            if (data != null) {
                action.action(data);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends DataBus> T bind(@NonNull LifecycleDelegate delegate, @NonNull Object receiver) {
        LifecycleLinkBus.register(delegate, getBus(), receiver);
        return (T) this;
    }

    /**
     * hasData() == trueを満たすまでウェイトをかける。
     */
    public DataType await(CancelCallback cancelCallback) throws TaskCanceledException {
        while (!hasData()) {
            Util.sleep(1);
            assertNotCanceled(cancelCallback);
        }

        return getData();
    }

    /**
     * データの受取待ちを行うタスクを生成する
     */
    public static BackgroundTaskBuilder<List<DataBus>> awaitTask(LifecycleDelegate delegate, ExecuteTarget executeTarget, CallbackTime callbackTime, DataBus... bus) {
        return delegate.async(executeTarget, callbackTime, (BackgroundTask<List<DataBus>> task) -> {
            List<DataBus> list = new ArrayList<>();
            for (DataBus it : bus) {
                list.add(it);
            }
            await(SupportCancelCallbackBuilder.from(task).build(), list);
            return list;
        });
    }

    /**
     * 全てのイベントバスがデータを持つまでウェイトをかける
     */
    public static void await(CancelCallback cancelCallback, DataBus... bus) throws TaskCanceledException {
        List<DataBus> list = new ArrayList<>();
        for (DataBus it : bus) {
            list.add(it);
        }
        await(cancelCallback, list);
    }

    /**
     * 全てのイベントバスがデータを持つまでウェイトをかける
     */
    public static void await(CancelCallback cancelCallback, List<? extends DataBus> bus) throws TaskCanceledException {
        while (true) {
            int completed = 0;
            for (DataBus it : bus) {
                if (it.hasData()) {
                    ++completed;
                }
            }

            // 全てのデータの生成が完了したので抜ける
            if (bus.size() == completed) {
                return;
            }

            assertNotCanceled(cancelCallback);
            Util.sleep(1);
        }
    }
}
