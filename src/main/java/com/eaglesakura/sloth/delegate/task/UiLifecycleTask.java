package com.eaglesakura.sloth.delegate.task;

import com.eaglesakura.sloth.delegate.lifecycle.UiLifecycle;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.widget.PopupWindow;

/**
 * UI系のタスクを分離する
 */
public class UiLifecycleTask<DelegateType extends UiLifecycle> extends LifecycleTask<DelegateType> {
    public UiLifecycleTask(DelegateType delegate) {
        super(delegate);
    }

    public Object getAutoDismissObject(@NonNull Object tag) {
        return mLifecycleDelegate.getAutoDismissObject(tag);
    }

    @UiThread
    public <T extends Dialog> T addAutoDismiss(@NonNull T dialog, Object tag) {
        return mLifecycleDelegate.addAutoDismiss(dialog, tag);
    }

    @UiThread
    public <T extends PopupWindow> T addAutoDismiss(@NonNull T window) {
        return mLifecycleDelegate.addAutoDismiss(window);
    }

    @UiThread
    public <T extends Dialog> T addAutoDismiss(@NonNull T dialog) {
        return mLifecycleDelegate.addAutoDismiss(dialog);
    }

    @UiThread
    public void compactAutoDismissDialogs() {
        mLifecycleDelegate.compactAutoDismissDialogs();
    }

    public boolean hasDialogs() {
        return mLifecycleDelegate.hasDialogs();
    }

    @UiThread
    public <T extends PopupWindow> T addAutoDismiss(@NonNull T window, Object tag) {
        return mLifecycleDelegate.addAutoDismiss(window, tag);
    }

    public boolean hasAutoDismissObject(@NonNull Object tag) {
        return mLifecycleDelegate.hasAutoDismissObject(tag);
    }
}
