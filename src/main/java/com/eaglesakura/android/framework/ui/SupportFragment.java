package com.eaglesakura.android.framework.ui;

import com.eaglesakura.android.framework.ui.delegate.SupportFragmentDelegate;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;
import com.eaglesakura.android.rx.SubscriptionController;
import com.eaglesakura.android.util.PermissionUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * startActivityForResultを行う場合、ParentFragmentが存在していたらそちらのstartActivityForResultを呼び出す。
 * <br>
 * これはchildFragmentの場合にonActivityResultが呼ばれない不具合を可能な限り回避するため。
 * <br>
 * ただし、複数のonActivityResultがハンドリングされる恐れが有るため、RequestCodeの重複には十分に注意すること
 */
public abstract class SupportFragment extends Fragment implements SupportFragmentDelegate.SupportFragmentCompat {

    protected final SupportFragmentDelegate mFragmentDelegate = new SupportFragmentDelegate(this);

    public SupportFragment() {
    }

    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mFragmentDelegate.getLifecycleState();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mFragmentDelegate.onCreateView(inflater, container, savedInstanceState);
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
     * 親クラスを特定のインターフェースに変換する
     *
     * 変換できない場合、このメソッドはnullを返却する
     */
    @Nullable
    public <T> T getParent(@NonNull Class<T> clazz) {
        return mFragmentDelegate.getParent(clazz);
    }

    /**
     * 親クラスを特定のインターフェースに変換する
     *
     * 変換できない場合、このメソッドはnullを返却する
     */
    @NonNull
    public <T> T getParentOrThrow(@NonNull Class<T> clazz) {
        return mFragmentDelegate.getParentOrThrow(clazz);
    }

    /**
     * ActionBarを取得する
     */
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
        mFragmentDelegate.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFragmentDelegate.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentDelegate.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFragmentDelegate.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFragmentDelegate.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mFragmentDelegate.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFragmentDelegate.onDestroy();
    }

    public <T extends Dialog> T addAutoDismiss(@NonNull T dialog) {
        return mFragmentDelegate.addAutoDismiss(dialog);
    }

    /**
     * バックスタックが一致したらtrue
     */
    public boolean isCurrentBackstack() {
        return mFragmentDelegate.isCurrentBackstack();
    }

    /**
     * 自身をFragmentから外す
     *
     * @param withBackStack backstack階層も含めて排除する場合はtrue
     */
    public void detatchSelf(boolean withBackStack) {
        mFragmentDelegate.detatchSelf(withBackStack);
    }

    /**
     * 戻るボタンのハンドリングを行う
     *
     * @return ハンドリングを行えたらtrue
     */
    public boolean handleBackButton() {
        return mFragmentDelegate.handleBackButton();
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

    public boolean requestRuntimePermission(PermissionUtil.PermissionType type) {
        return mFragmentDelegate.requestRuntimePermission(type);
    }


    /**
     * UIスレッドで実行する
     */
    public void runOnUiThread(@NonNull Runnable runnable) {
        mFragmentDelegate.runOnUiThread(runnable);
    }

    /**
     * タスクコントローラを取得する
     */
    public SubscriptionController getSubscription() {
        return mFragmentDelegate.getSubscription();
    }

    /**
     * UIに関わる処理を非同期で実行する。
     *
     * 処理順を整列するため、非同期・直列処理されたあと、アプリがフォアグラウンドのタイミングでコールバックされる。
     */
    public <T> RxTaskBuilder<T> asyncUI(RxTask.Async<T> background) {
        return mFragmentDelegate.asyncUI(background);
    }

    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    public <T> RxTaskBuilder<T> async(SubscribeTarget subscribe, ObserveTarget observe, RxTask.Async<T> background) {
        return mFragmentDelegate.async(subscribe, observe, background);
    }

    @NonNull
    @Override
    public Fragment getFragment(SupportFragmentDelegate self) {
        return this;
    }

    @Override
    public void setBackStackIndex(int index) {
        mFragmentDelegate.setBackStackIndex(index);
    }

    @Nullable
    @Override
    public Garnet.Builder newInjectionBuilder(SupportFragmentDelegate self, Context context) {
        return Garnet.create(this).depend(Context.class, context);
    }
}