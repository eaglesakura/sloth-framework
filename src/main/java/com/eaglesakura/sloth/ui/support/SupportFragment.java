package com.eaglesakura.sloth.ui.support;

import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.saver.LightSaver;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.BackgroundTaskBuilder;
import com.eaglesakura.cerberus.CallbackTime;
import com.eaglesakura.cerberus.ExecuteTarget;
import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.PendingCallbackQueue;
import com.eaglesakura.sloth.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.sloth.delegate.lifecycle.FragmentLifecycle;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.InternalSupportFragmentUtil;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import java.util.List;

/**
 * startActivityForResultを行う場合、ParentFragmentが存在していたらそちらのstartActivityForResultを呼び出す。
 * <br>
 * これはchildFragmentの場合にonActivityResultが呼ばれない不具合を可能な限り回避するため。
 * <br>
 * ただし、複数のonActivityResultがハンドリングされる恐れが有るため、RequestCodeの重複には十分に注意すること
 */
public abstract class SupportFragment extends Fragment implements SupportFragmentDelegate.SupportFragmentCompat {

    protected final FragmentLifecycle mLifecycleDelegate = new FragmentLifecycle();

    protected final SupportFragmentDelegate mFragmentDelegate = new SupportFragmentDelegate(this, mLifecycleDelegate);

    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mLifecycleDelegate.getLifecycleState();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLifecycleDelegate.onCreateView(inflater, container, savedInstanceState);
        return mFragmentDelegate.getView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mFragmentDelegate.onCreateOptionsMenu(menu, inflater);
    }

    public <T extends Activity> T getActivity(@NonNull Class<T> clazz) {
        return mFragmentDelegate.getActivity(clazz);
    }

    public <T extends View> T findViewById(@NonNull Class<T> clazz, @IdRes int id) {
        return mFragmentDelegate.findViewById(clazz, id);
    }

    public <T extends View> T findViewByIdFromActivity(@NonNull Class<T> clazz, @IdRes int id) {
        return mFragmentDelegate.findViewByIdFromActivity(clazz, id);
    }

    /**
     * ActionBarを取得する
     */
    @NonNull
    @Override
    public ActionBar getActionBar() {
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            return ((AppCompatActivity) activity).getSupportActionBar();
        } else {
            return null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mLifecycleDelegate.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mLifecycleDelegate.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        InternalSupportFragmentUtil.onCreate(this, savedInstanceState);
        super.onCreate(savedInstanceState);
        mLifecycleDelegate.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mLifecycleDelegate.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLifecycleDelegate.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLifecycleDelegate.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mLifecycleDelegate.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifecycleDelegate.onDestroy();
    }

    public boolean hasAutoDismissObject(@NonNull Object tag) {
        return mLifecycleDelegate.hasAutoDismissObject(tag);
    }

    public Object getAutoDismissObject(@NonNull Object tag) {
        return mLifecycleDelegate.getAutoDismissObject(tag);
    }

    public <T extends Dialog> T addAutoDismiss(@NonNull T dialog) {
        return mLifecycleDelegate.addAutoDismiss(dialog);
    }

    @UiThread
    public <T extends PopupWindow> T addAutoDismiss(@NonNull T window) {
        return mLifecycleDelegate.addAutoDismiss(window);
    }

    @UiThread
    public <T extends Dialog> T addAutoDismiss(@NonNull T dialog, Object tag) {
        return mLifecycleDelegate.addAutoDismiss(dialog, tag);
    }

    @UiThread
    public <T extends PopupWindow> T addAutoDismiss(@NonNull T window, Object tag) {
        return mLifecycleDelegate.addAutoDismiss(window, tag);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        mFragmentDelegate.startActivityForResult(intent, requestCode);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFragmentDelegate.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Runtime Permissionの更新を行わせる
     *
     * @return パーミッション取得を開始した場合はtrue
     */
    public boolean requestRuntimePermission(String[] permissions) {
        return mFragmentDelegate.requestRuntimePermission(permissions);
    }

    /**
     * Runtime Permissionのブロードキャストを行わせる
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mFragmentDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean requestRuntimePermission(PermissionUtil.PermissionType... type) {
        return mFragmentDelegate.requestRuntimePermission(type);
    }

    /**
     * Permissionの取得を開始する。
     *
     * OSによるハンドリングを開始した場合はtrue
     */
    public boolean requestRuntimePermission(List<PermissionUtil.PermissionType> types) {
        return mFragmentDelegate.requestRuntimePermission(types);
    }

    /**
     * コールバックキューを取得する
     */
    public PendingCallbackQueue getCallbackQueue() {
        return mLifecycleDelegate.getCallbackQueue();
    }

    /**
     * UIに関わる処理を非同期で実行する。
     *
     * 処理順を整列するため、非同期・直列処理されたあと、アプリがフォアグラウンドのタイミングでコールバックされる。
     */
    public <T> BackgroundTaskBuilder<T> asyncUI(BackgroundTask.Async<T> background) {
        return mLifecycleDelegate.asyncQueue(background)
                .cancelSignal((Fragment) this);
    }

    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    public <T> BackgroundTaskBuilder<T> async(ExecuteTarget execute, CallbackTime time, BackgroundTask.Async<T> background) {
        return mLifecycleDelegate.async(execute, time, background);
    }

    @NonNull
    @Override
    public Fragment getFragment(SupportFragmentDelegate self) {
        return this;
    }

    @Nullable
    @Override
    public Garnet.Builder newInjectionBuilder(SupportFragmentDelegate self, Context context) {
        return Garnet.create(this).depend(Context.class, context);
    }

    @Nullable
    @Override
    public LightSaver.Builder newBundleBuilder(SupportFragmentDelegate self, Bundle bundle, Context context) {
        return LightSaver.create(bundle);
    }

    public void startActivityForResultWithCarryData(Intent intent, int requestCode, Bundle carryState) {
        mFragmentDelegate.startActivityForResultWithCarryData(intent, requestCode, carryState);
    }


}