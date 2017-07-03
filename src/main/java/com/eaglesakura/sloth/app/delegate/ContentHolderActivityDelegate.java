package com.eaglesakura.sloth.app.delegate;

import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.sloth.R;
import com.eaglesakura.sloth.app.FragmentHolder;
import com.eaglesakura.sloth.app.lifecycle.ActivityLifecycle;
import com.eaglesakura.sloth.app.lifecycle.event.OnCreateEvent;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Sloth標準構成のActivityを構築するDelegate
 */
public class ContentHolderActivityDelegate {
    public static final String TAG_CONTENT_FRAGMENT_MAIN = "sloth.TAG_CONTENT_FRAGMENT_MAIN";

    private FragmentHolder<Fragment> mFragmentMain;

    @NonNull
    private AppCompatActivity mActivity;

    @NonNull
    private Callback mCallback;

    public interface Callback {

        /**
         * 画面表示のためのLayoutIdを取得する
         */
        @LayoutRes
        int getContentLayout(@NonNull ContentHolderActivityDelegate self);

        /**
         * 表示するコンテンツが指定されない場合のデフォルトコンテンツを開く
         */
        @NonNull
        Fragment newContentFragment(@NonNull ContentHolderActivityDelegate self);
    }

    public ContentHolderActivityDelegate(@NonNull ActivityLifecycle lifecycle, AppCompatActivity activity, @NonNull Callback callback) {
        mActivity = activity;
        mCallback = callback;

        mFragmentMain = new FragmentHolder<Fragment>(activity, R.id.Content_Holder_Root, TAG_CONTENT_FRAGMENT_MAIN) {
            @NonNull
            @Override
            protected Fragment newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
                return mCallback.newContentFragment(ContentHolderActivityDelegate.this);
            }

            @NonNull
            @Override
            public Fragment get() {
                return activity.getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT_MAIN);
            }
        };

        lifecycle.subscribe(event -> {
            switch (event.getState()) {
                case OnCreate:
                    onCreate((OnCreateEvent) event);
                    break;
                case OnResume:
                    mFragmentMain.onResume();
                    break;
            }
        });
    }

    private void onCreate(OnCreateEvent event) {
        mActivity.setContentView(mCallback.getContentLayout(this));

        Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.EsMaterial_Toolbar);
        if (toolbar != null) {
            mActivity.setSupportActionBar(toolbar);
        }

        MargarineKnife.from(mActivity).to(mActivity).bind();
        mFragmentMain.onCreate(event.getBundle());
    }


    /**
     * コンテンツコントロール用のFragmentを取得する
     */
    public Fragment getContentFragment() {
        return mFragmentMain.get();
    }
}
