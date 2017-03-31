package com.eaglesakura.sloth.ui.progress;

import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.util.FragmentUtil;
import com.eaglesakura.sloth.view.SupportProgressView;
import com.eaglesakura.sloth.app.SlothFragment;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.sloth.annotation.FragmentLayout;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * 処理中のTapGuardViewを表示する
 */
@FragmentLayout(resName = "esm_progress")
public class SupportProgressFragment extends SlothFragment {

    @Bind(resName = "Item.Progress")
    SupportProgressView mProgress;

    public SupportProgressFragment() {
        mProgressStackManager.setListener(mStackManagerListener);
    }

    private ProgressStackManager mProgressStackManager;

    @Override
    protected void onCreateLifecycle(FragmentLifecycle newLifecycle) {
        super.onCreateLifecycle(newLifecycle);
        mProgressStackManager = new ProgressStackManager(newLifecycle.getCallbackQueue());
    }

    public ProgressStackManager getProgressStackManager() {
        return mProgressStackManager;
    }

    private ProgressStackManager.Listener mStackManagerListener = new ProgressStackManager.Listener() {
        @Override
        public void onProgressStarted(@NonNull ProgressStackManager self, @NonNull ProgressToken token) {
            mProgress.setText(token.getMessage());
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onProgressTopChanged(@NonNull ProgressStackManager self, @NonNull ProgressToken topToken) {
            mProgress.setText(topToken.getMessage());
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onProgressFinished(@NonNull ProgressStackManager self) {
            mProgress.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * Fragmentを付与する
     */
    public static FragmentTransaction attach(FragmentTransaction transaction, @IdRes int containerId) {
        transaction.add(containerId, new SupportProgressFragment(), SupportProgressFragment.class.getName());
        return transaction;
    }

    /**
     * Fragmentを付与する
     */
    public static void attach(AppCompatActivity activity, @IdRes int containerId) {
        attach(activity.getSupportFragmentManager().beginTransaction(), containerId).commit();
    }

    /**
     * 非同期処理のProgressを表示する
     */
    public static ProgressToken pushProgress(Fragment sender, String message) {
        ProgressStackManager progressStackManager = FragmentUtil.listInterfaces(sender.getFragmentManager(), SupportProgressFragment.class).get(0).getProgressStackManager();
        ProgressToken token = ProgressToken.fromMessage(progressStackManager, message);
        progressStackManager.push(token);

        return token;
    }
}
