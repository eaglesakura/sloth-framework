package com.eaglesakura.sloth.app.support;

import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.cerberus.LifecycleEvent;
import com.eaglesakura.cerberus.event.OnCreateOptionsMenuEvent;
import com.eaglesakura.cerberus.event.OnCreateViewEvent;
import com.eaglesakura.sloth.Sloth;
import com.eaglesakura.sloth.app.lifecycle.FragmentLifecycle;
import com.eaglesakura.sloth.annotation.FragmentLayout;
import com.eaglesakura.sloth.annotation.FragmentMenu;
import com.eaglesakura.util.StringUtil;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.View;

import rx.functions.Action1;

/**
 * View/Menuバインディングをサポートする
 */
public class ViewBindingSupport {
    public static void bind(FragmentLifecycle lifecycle, Fragment target, Callback callback) {
        lifecycle.subscribe(new Impl(lifecycle, target, callback));
    }

    private static class Impl implements Action1<LifecycleEvent> {
        FragmentLifecycle mLifecycle;

        Fragment mTarget;

        Callback mCallback;

        @LayoutRes
        private int mInjectionLayoutId;

        @MenuRes
        private int mInjectionOptionMenuId;

        public Impl(FragmentLifecycle lifecycle, Fragment target, Callback callback) {
            mLifecycle = lifecycle;
            mTarget = target;
            mCallback = callback;

            // メニュー有無を確定させる
            FragmentMenu menu = mTarget.getClass().getAnnotation(FragmentMenu.class);
            if (menu != null) {
                target.setHasOptionsMenu(true);
            }
        }

        @Override
        public void call(LifecycleEvent lifecycleEvent) {
            switch (lifecycleEvent.getState()) {
                case OnCreateView:
                    onCreateView(((OnCreateViewEvent) lifecycleEvent));
                    break;
                case OnCreateOptionsMenu:
                    onCreateOptionsMenu(((OnCreateOptionsMenuEvent) lifecycleEvent));
                    break;
            }
        }

        void onCreateView(OnCreateViewEvent event) {
            int injectionLayoutId = getInjectionLayoutId();
            if (injectionLayoutId != 0) {
                View view = event.getInflater().inflate(injectionLayoutId, event.getContainer(), false);
                MargarineKnife.from(view).to(mTarget).bind();
                mCallback.onAfterViews(view);
            }
        }

        void onCreateOptionsMenu(OnCreateOptionsMenuEvent event) {
            int injectionOptionMenuId = getInjectionOptionMenuId();
            if (injectionOptionMenuId != 0) {
                Menu menu = event.getMenu();
                event.getInflater().inflate(mInjectionOptionMenuId, menu);
                MargarineKnife.bindMenu(menu, mTarget);
                mCallback.onAfterBindMenu(menu);
            }
        }

        Context getContext() {
            Context result = mTarget.getContext();
            if (result == null) {
                result = Sloth.getApplication();
            }
            return result;
        }

        int getInjectionLayoutId() {
            // load layout
            if (mInjectionLayoutId == 0) {
                FragmentLayout layout = mTarget.getClass().getAnnotation(FragmentLayout.class);
                if (layout != null) {
                    mInjectionLayoutId = layout.value();
                    if (mInjectionLayoutId == 0 && !StringUtil.isEmpty(layout.resName())) {
                        mInjectionLayoutId = ContextUtil.getLayoutFromName(getContext(), layout.resName());
                    }

                    if (mInjectionLayoutId == 0) {
                        throw new IllegalArgumentException("R.id." + layout.resName() + " / Not found.");
                    }
                }
            }

            return mInjectionLayoutId;
        }

        int getInjectionOptionMenuId() {
            if (mInjectionOptionMenuId == 0) {
                FragmentMenu menu = mTarget.getClass().getAnnotation(FragmentMenu.class);
                if (menu != null) {
                    mInjectionOptionMenuId = menu.value();
                    if (mInjectionOptionMenuId == 0 && !StringUtil.isEmpty(menu.resName())) {
                        mInjectionOptionMenuId = ContextUtil.getMenuFromName(getContext(), menu.resName());
                    }

                    if (mInjectionOptionMenuId == 0) {
                        throw new IllegalArgumentException("R.menu." + menu.resName() + " / Not found.");
                    }
                }
            }
            return mInjectionOptionMenuId;
        }
    }

    public interface Callback {
        void onAfterViews(View rootView);

        void onAfterBindMenu(Menu menu);
    }
}
