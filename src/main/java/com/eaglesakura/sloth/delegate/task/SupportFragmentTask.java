package com.eaglesakura.sloth.delegate.task;

import com.eaglesakura.sloth.delegate.fragment.SupportFragmentDelegate;
import com.eaglesakura.sloth.delegate.lifecycle.FragmentLifecycle;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * UIFragment用の分離タスク
 */
public class SupportFragmentTask<FragmentDelegateType extends SupportFragmentDelegate, LifecycleDelegateType extends FragmentLifecycle> extends UiLifecycleTask<LifecycleDelegateType> {

    protected final FragmentDelegateType mFragment;

    public SupportFragmentTask(FragmentDelegateType fragment, LifecycleDelegateType delegate) {
        super(delegate);
        mFragment = fragment;
    }

    public Resources getResources() {
        return mFragment.getFragment().getResources();
    }


    public String getString(@StringRes int resId, Object... args) {
        return mFragment.getFragment().getString(resId, args);
    }

    @NonNull
    public Fragment getFragment() {
        return mFragment.getFragment();
    }

    @Nullable
    public Fragment getParentFragment() {
        return mFragment.getParentFragment();
    }

    public Activity getActivity() {
        return mFragment.getActivity();
    }

    public View getView() {
        return mFragment.getView();
    }

    public Context getContext() {
        return getFragment().getContext();
    }

    @Nullable
    public <T extends View> T findViewByIdFromActivity(Class<T> clazz, int id) {
        return mFragment.findViewByIdFromActivity(clazz, id);
    }

    @Nullable
    public <T extends View> T findViewById(Class<T> clazz, int id) {
        return mFragment.findViewById(clazz, id);
    }

}
