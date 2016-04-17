package com.eaglesakura.android.framework.ui;

import com.eaglesakura.android.framework.ui.delegate.SupportActivityDelegate;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.rx.LifecycleState;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.rx.RxTask;
import com.eaglesakura.android.rx.RxTaskBuilder;
import com.eaglesakura.android.rx.SubscribeTarget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

/**
 *
 */
public abstract class SupportActivity extends AppCompatActivity implements SupportActivityDelegate.SupportActivityCompat {

    SupportActivityDelegate mActivityDelegate = new SupportActivityDelegate(this);

    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mActivityDelegate.getLifecycleState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityDelegate.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mActivityDelegate.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityDelegate.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityDelegate.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mActivityDelegate.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityDelegate.onDestroy();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        MargarineKnife.bind(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        MargarineKnife.bind(this);
    }

    public <T extends View> T findViewById(Class<T> clazz, int id) {
        return mActivityDelegate.findViewById(clazz, id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mActivityDelegate.onActivityResult(resultCode, requestCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Fragmentがアタッチされたタイミングで呼び出される。
     * <br>
     * このFragmentは最上位階層のみが扱われる。
     */
    @Override
    public void onAttachFragment(Fragment fragment) {
        mActivityDelegate.onAttachFragment(fragment);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mActivityDelegate.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * Runtime Permissionの更新を行わせる
     *
     * @return パーミッション取得を開始した場合はtrue
     */
    public boolean requestRuntimePermissions(String[] permissions) {
        return mActivityDelegate.requestRuntimePermissions(permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * UIに関わる処理を非同期で実行する。
     *
     * 処理順を整列するため、非同期・直列処理されたあと、アプリがフォアグラウンドのタイミングでコールバックされる。
     */
    public <T> RxTaskBuilder<T> asyncUI(RxTask.Async<T> background) {
        return mActivityDelegate.asyncUI(background);
    }

    /**
     * 規定のスレッドとタイミングで非同期処理を行う
     */
    public <T> RxTaskBuilder<T> async(SubscribeTarget subscribe, ObserveTarget observe, RxTask.Async<T> background) {
        return mActivityDelegate.async(subscribe, observe, background);
    }

    @Override
    public Activity getActivity(SupportActivityDelegate self) {
        return this;
    }
}
