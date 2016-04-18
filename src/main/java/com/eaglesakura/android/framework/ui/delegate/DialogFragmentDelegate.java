package com.eaglesakura.android.framework.ui.delegate;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.util.StringUtil;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * App側で継承したFragmentでDialogFramentを生成する場合に
 */
public class DialogFragmentDelegate {
    private Dialog mDialog;

    @NonNull
    private SupportDialogFragmentCompat mCompat;

    private LifecycleDelegate mLifecycleDelegate;

    public DialogFragmentDelegate(@NonNull SupportDialogFragmentCompat compat) {
        mCompat = compat;
    }

    public void bind(LifecycleDelegate lifecycleDelegate) {
        mLifecycleDelegate = lifecycleDelegate;
        mLifecycleDelegate.getSubscription().getObservable().subscribe(it -> {
            if (it == LifecycleState.OnDestroyed) {
                onDestroy();
            }
        });
    }

    @Nullable
    public Dialog getDialog() {
        return mDialog;
    }

    public void onCreate(Bundle savedInstanceState) {
        FwLog.widget("show dialog");
        mDialog = mLifecycleDelegate.addAutoDismiss(mCompat.onCreateDialog(this, savedInstanceState));
        mDialog.setOnDismissListener(it -> {
            if (mDialog == null) {
                return;
            }
            FwLog.widget("Detach DialogFragment");
            mCompat.onDismiss(this);
        });
        mDialog.show();
    }

    private void onDestroy() {
        FwLog.widget("suspend dialog");
        if (mDialog != null && mDialog.isShowing()) {
            Dialog dialog = mDialog;
            mDialog = null;
            dialog.dismiss();
        }
        FwLog.widget("remove dialog");
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

    public interface SupportDialogFragmentCompat {
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
