package com.eaglesakura.sloth.provider;

import com.eaglesakura.android.garnet.Depend;

import android.app.Activity;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Activity/FragmentとViewModelの依存解決を行うProviderを定義する
 */
public abstract class GarnetViewModelProvider extends ContextProvider {
    private Activity mActivity;

    private Fragment mFragment;

    @Depend
    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    @Depend
    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    protected AppCompatActivity getActivity() {
        if (mActivity == null && mFragment != null) {
            mActivity = mFragment.getActivity();
        }
        return ((AppCompatActivity) mActivity);
    }

    protected Fragment getFragment() {
        return mFragment;
    }

    protected <T extends ViewModel> T getViewModelFromFragment(Class<T> clazz) {
        return ViewModelProviders.of(getFragment()).get(clazz);
    }

    protected <T extends ViewModel> T getViewModelFromActivity(Class<T> clazz) {
        return ViewModelProviders.of(getActivity()).get(clazz);
    }


}
