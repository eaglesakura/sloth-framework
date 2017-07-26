package com.eaglesakura.sloth.app;

import com.eaglesakura.android.util.FragmentUtil;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.cerberus.BackgroundTask;
import com.eaglesakura.cerberus.BackgroundTaskBuilder;
import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.cerberus.PendingCallbackQueue;
import com.eaglesakura.sloth.annotation.BindInterface;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.util.CollectionUtil;
import com.eaglesakura.util.ReflectionUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.InternalSupportFragmentUtil;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * startActivityForResultを行う場合、ParentFragmentが存在していたらそちらのstartActivityForResultを呼び出す。
 * <br>
 * これはchildFragmentの場合にonActivityResultが呼ばれない不具合を可能な限り回避するため。
 * <br>
 * ただし、複数のonActivityResultがハンドリングされる恐れが有るため、RequestCodeの重複には十分に注意すること
 */
public abstract class SlothFragment extends Fragment {

    private FragmentLifecycle mLifecycle;

    /**
     * ライフサイクルオブジェクトを取得する
     */
    @NonNull
    public FragmentLifecycle getLifecycle() {
        if (mLifecycle == null) {
            synchronized (this) {
                if (mLifecycle == null) {
                    mLifecycle = new FragmentLifecycle();
                    onCreateLifecycle(mLifecycle);
                }
            }
        }
        return mLifecycle;
    }

    /**
     * ライフサイクルが初期化された
     */
    protected void onCreateLifecycle(FragmentLifecycle newLifecycle) {

    }


    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return getLifecycle().getLifecycleState();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getLifecycle().onCreateView(inflater, container, savedInstanceState);
        return null;
    }

    /**
     * ActionBarを取得する
     */
    @NonNull
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
        getLifecycle().onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // インターフェースを検索し直す
        // バインド対象のインターフェースを検索する
        for (Field field : ReflectionUtil.listAnnotationFields(getClass(), BindInterface.class)) {
            bindInterface(field);
        }

        getLifecycle().onAttach(context);
    }

    /**
     * インターフェースのバインディングを行なう
     */
    private void bindInterface(Field field) {
        BindInterface annotation = field.getAnnotation(BindInterface.class);
        try {
            Object value;
            if (ReflectionUtil.isListInterface(field)) {
                // リストオブジェクト
                // 通常Object
                value = FragmentUtil.listInterfaces(((AppCompatActivity) getActivity()), field.getType());
            } else {
                // 通常Object
                List objects = FragmentUtil.listInterfaces(((AppCompatActivity) getActivity()), field.getType());
                value = objects.get(0);
            }
            field.setAccessible(true);
            field.set(this, value);
        } catch (Exception e) {
            // null許容しないなら、例外を投げて終了する
            if (!annotation.nullable()) {
                throw new Error("bind failed :: " + field.getType() + " " + field.getName(), e);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        InternalSupportFragmentUtil.onCreate(this, savedInstanceState);
        super.onCreate(savedInstanceState);
        getLifecycle().onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getLifecycle().onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLifecycle().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        getLifecycle().onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getLifecycle().onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStop() {
        super.onStop();
        getLifecycle().onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLifecycle().onDestroyView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        InternalSupportFragmentUtil.onDetach(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLifecycle().onDestroy();
    }

    /**
     * コールバックキューを取得する
     */
    public PendingCallbackQueue getCallbackQueue() {
        return getLifecycle().getCallbackQueue();
    }

    /**
     * UIに関わる処理を非同期で実行する。
     *
     * 処理順を整列するため、非同期・直列処理されたあと、アプリがフォアグラウンドのタイミングでコールバックされる。
     */
    public <T> BackgroundTaskBuilder<T> asyncQueue(BackgroundTask.Async<T> background) {
        return getLifecycle().asyncQueue(background);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, null);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        Fragment fragment = this;
        while (fragment.getParentFragment() != null) {
            fragment = fragment.getParentFragment();
        }
        InternalSupportFragmentUtil.startActivityForResult(fragment, requestCode, intent, options);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getLifecycle().onActivityResult(requestCode, resultCode, data);
    }

    public void requestPermissions(@NonNull Collection<PermissionUtil.PermissionType> permissions, int requestCode) {
        Set<String> rawPermissions = PermissionUtil.listPermissions(permissions);
        requestPermissions(CollectionUtil.asArray(rawPermissions, new String[rawPermissions.size()]), requestCode);
    }
}