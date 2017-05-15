package com.eaglesakura.sloth.ui.license;

import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.app.SlothFragment;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.sloth.app.support.ViewBindingSupport;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * ライセンス詳細テキストを表示するFragment
 */
@FragmentLayout(resName = "esm_license_detail")
public class LicenseDetailFragment extends SlothFragment {

    @Bind(resName = "eglibrary.Item.Value")
    TextView mDetailView;

    @BundleState
    String mDetail;

    private String mTitle;

    private View mRootView;

    @Override
    protected void onCreateLifecycle(FragmentLifecycle newLifecycle) {
        super.onCreateLifecycle(newLifecycle);
        ViewBindingSupport.bind(newLifecycle, this, new ViewBindingSupport.Callback() {
            @Override
            public void onAfterViews(View rootView) {
                mDetailView.setText(mDetail);
                mRootView = rootView;
            }

            @Override
            public void onAfterBindMenu(Menu menu) {

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActionBar().setTitle(mTitle);
    }

    public void setLicense(String title, String detailText) {
        mTitle = title;
        mDetail = detailText;
    }
}
