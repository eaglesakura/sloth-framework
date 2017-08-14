package com.eaglesakura.sloth.cerberus;

import com.eaglesakura.cerberus.BackgroundTaskBuilder;
import com.eaglesakura.cerberus.PendingCallbackQueue;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.lambda.ResultAction1;
import com.eaglesakura.sloth.annotation.Experimental;

/**
 * Util機能付きのBackgroundTaskBuilder
 */
@Experimental
public class SupportBackgroundTaskBuilder<T> extends BackgroundTaskBuilder<T> {
    public SupportBackgroundTaskBuilder(PendingCallbackQueue subscriptionController) {
        super(subscriptionController);
    }

    /**
     * キャンセル対応の非同期タスクを実行する
     */
    @Experimental
    public SupportBackgroundTaskBuilder<T> asyncCancelable(ResultAction1<CancelCallback, ? extends T> action) {
        return (SupportBackgroundTaskBuilder<T>) async(task -> action.action(task::isCanceled));
    }
}
