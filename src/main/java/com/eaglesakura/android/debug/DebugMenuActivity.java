package com.eaglesakura.android.debug;

import com.eaglesakura.sloth.R;
import com.eaglesakura.sloth.delegate.activity.ContentHolderActivityDelegate;
import com.eaglesakura.sloth.ui.support.ContentHolderActivity;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * デフォルトのデバッグメニューを表示するActivity
 */
public class DebugMenuActivity extends ContentHolderActivity {

    @Override
    public int getDefaultLayoutId(@NonNull ContentHolderActivityDelegate self) {
        return R.layout.esm_activity_debugmenu;
    }

    @NonNull
    @Override
    public Fragment newDefaultContentFragment(@NonNull ContentHolderActivityDelegate self) {
        return new DebugContentFragment();
    }
}
