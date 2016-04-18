package com.eaglesakura.android.framework.ui.support;

import com.eaglesakura.android.framework.ui.delegate.ContentHolderActivityDelegate;
import com.eaglesakura.android.framework.ui.support.SupportActivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * 親となるFragmentを指定して起動するActivityの雛形
 * <p/>
 * メインコンテンツは @+id/Content.Holder.Root を持たなければならない。
 * Toolbarは @+id/EsMaterial.Toolbar を自動的に検索し、存在するならToolbarとして自動設定する。
 */
public abstract class ContentHolderActivity extends SupportActivity implements ContentHolderActivityDelegate.ContentHolderActivityCompat {
    ContentHolderActivityDelegate mContentHolderDelegate = new ContentHolderActivityDelegate(this);

    public ContentHolderActivity() {
        mContentHolderDelegate.bind(mLifecycleDelegate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentHolderDelegate.onCreate(savedInstanceState);
    }

    public Fragment getContentFragment() {
        return mContentHolderDelegate.getContentFragment();
    }

    @NonNull
    @Override
    public Activity getActivity(@NonNull ContentHolderActivityDelegate self) {
        return this;
    }
}
