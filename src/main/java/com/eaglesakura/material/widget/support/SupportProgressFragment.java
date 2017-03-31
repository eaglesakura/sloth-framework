package com.eaglesakura.material.widget.support;

import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.util.FragmentUtil;
import com.eaglesakura.material.widget.MaterialProgressView;
import com.eaglesakura.sloth.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.sloth.ui.progress.ProgressStackManager;
import com.eaglesakura.sloth.ui.progress.ProgressToken;
import com.eaglesakura.sloth.ui.support.SupportFragment;
import com.eaglesakura.sloth.ui.support.annotation.FragmentLayout;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;

/**
 * 処理中のTapGuardViewを表示する
 */
@FragmentLayout(resName = "esm_progress")
public class SupportProgressFragment extends SupportFragment {

    @Bind(resName = "Item.Progress")
    MaterialProgressView mProgress;

    public SupportProgressFragment() {
        mProgressStackManager.setListener(mStackManagerListener);
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {

    }

    @Override
    public void onAfterBindMenu(SupportFragmentDelegate self, Menu menu) {

    }

    @Override
    public void onAfterInjection(SupportFragmentDelegate self) {

    }

    @NonNull
    private final ProgressStackManager mProgressStackManager = new ProgressStackManager(mLifecycleDelegate.getCallbackQueue());

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
