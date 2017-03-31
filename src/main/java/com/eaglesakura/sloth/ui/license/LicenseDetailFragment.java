package com.eaglesakura.sloth.ui.license;

import com.eaglesakura.android.margarine.Bind;
import com.eaglesakura.android.saver.BundleState;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.app.SlothFragment;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.sloth.app.support.ViewBindingSupport;

import android.content.Context;
import android.view.Menu;
import android.view.View;
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

    @Override
    protected void onCreateLifecycle(FragmentLifecycle newLifecycle) {
        super.onCreateLifecycle(newLifecycle);
        ViewBindingSupport.bind(newLifecycle, this, new ViewBindingSupport.Callback() {
            @Override
            public void onAfterViews(View rootView) {
                mDetailView.setText(mDetail);
            }

            @Override
            public void onAfterBindMenu(Menu menu) {

            }
        });
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
