package com.eaglesakura.sloth.app.delegate;

import com.eaglesakura.cerberus.event.OnCreateEvent;
import com.eaglesakura.sloth.SlothLog;
import com.eaglesakura.sloth.annotation.Experimental;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.sloth.app.lifecycle.UiLifecycle;
import com.eaglesakura.util.StringUtil;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * DialogFragment風の管理を行う場合に使用する
 */
@Experimental
public class DialogFragmentDelegate {
    private Dialog mDialog;

    @NonNull
    private final Callback mCompat;

    @NonNull
    private final UiLifecycle mLifecycleDelegate;

    public DialogFragmentDelegate(@NonNull Callback compat, @NonNull FragmentLifecycle lifecycle) {
        mCompat = compat;
        mLifecycleDelegate = lifecycle;
        lifecycle.getCallbackQueue().getObservable().subscribe(it -> {
            switch (it.getState()) {
                case OnCreate:
                    onCreate((OnCreateEvent) it);
                    break;
                case OnDestroy:
                    onDestroy();
                    break;
            }
        });
    }

    @Nullable
    public Dialog getDialog() {
        return mDialog;
    }

    void onCreate(OnCreateEvent event) {
        SlothLog.widget("show dialog");
        mDialog = mLifecycleDelegate.addAutoDismiss(mCompat.onCreateDialog(this, event.getBundle()));
        mDialog.setOnDismissListener(it -> {
            if (mDialog == null) {
                return;
            }
            SlothLog.widget("Detach DialogFragment");
            mCompat.onDismiss(this);
        });
        mDialog.show();
    }

    private void onDestroy() {
        SlothLog.widget("suspend dialog");
        if (mDialog != null && mDialog.isShowing()) {
            Dialog dialog = mDialog;
            mDialog = null;
            dialog.dismiss();
        }
        SlothLog.widget("remove dialog");
    }

    /**
     * ダイアログ表示を行う
     */
    public void onShow(FragmentManager fragmentManager, String tag) {
        if (mDialog != null) {
            throw new IllegalStateException();
        }

        if (StringUtil.isEmpty(tag)) {
            tag = mDialog.toString();
        }

        fragmentManager
                .beginTransaction()
                .add(mCompat.getFragment(this), tag)
                .commit();
    }

    public interface Callback {
        /**
         * ダイアログの生成を行わせる
         *
         * この呼出が行われたタイミングでは、全ての@Stateオブジェクトはレストア済である。
         */
        @NonNull
        Dialog onCreateDialog(DialogFragmentDelegate self, Bundle savedInstanceState);

        /**
         * ダイアログを閉じたため、Fragmentをcloseする
         */
        void onDismiss(DialogFragmentDelegate self);

        /**
         * Fragment取得
         */
        Fragment getFragment(DialogFragmentDelegate self);
    }
}
