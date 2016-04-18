package com.eaglesakura.android.framework.ui.delegate;

import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.garnet.Garnet;
import com.eaglesakura.android.margarine.MargarineKnife;
import com.eaglesakura.android.oari.ActivityResult;
import com.eaglesakura.android.res.LayoutId;
import com.eaglesakura.android.res.MenuId;
import com.eaglesakura.android.rx.ObserveTarget;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.util.ReflectionUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import icepick.Icepick;
import icepick.State;

/**
 * startActivityForResultを行う場合、ParentFragmentが存在していたらそちらのstartActivityForResultを呼び出す。
 * <br>
 * これはchildFragmentの場合にonActivityResultが呼ばれない不具合を可能な限り回避するため。
 * <br>
 * ただし、複数のonActivityResultがハンドリングされる恐れが有るため、RequestCodeの重複には十分に注意すること
 */
public class SupportFragmentDelegate extends LifecycleDelegate {

    public interface SupportFragmentCompat {
        int FLAG_AFTERVIEW_INITIALIZE = 0x01 << 0;
        int FLAG_AFTERVIEW_RESTORE = 0x01 << 1;

        @NonNull
        Fragment getFragment(SupportFragmentDelegate self);


        @NonNull
        ActionBar getActionBar();

        void setHasOptionsMenu(boolean set);

        /**
         * BackstackIndexが確定した
         *
         * SupportFragmentDelegate.setBackStackIndex(int)を呼び出す
         */
        void setBackStackIndex(int index);

        /**
         * Viewバインドが完了した
         *
         * @param flags FLAG_AFTERVIEW_INITIALIZE, FLAG_AFTERVIEW_RESTORE
         */
        void onAfterViews(SupportFragmentDelegate self, int flags);

        /**
         * メニューのバインドが完了した
         */
        void onAfterBindMenu(SupportFragmentDelegate self, Menu menu);

        /**
         * 依存注入が完了した
         */
        void onAfterInjection(SupportFragmentDelegate self);

        /**
         * 依存注入用のBuilderを開始する
         */
        @Nullable
        Garnet.Builder newInjectionBuilder(SupportFragmentDelegate self, Context context);
    }

    static final int BACKSTACK_NONE = 0xFEFEFEFE;

    @NonNull
    private SupportFragmentCompat mCompat;

    private int mBackStackIndex = BACKSTACK_NONE;

    /**
     * 既に依存構築済であればtrue
     */
    private boolean mInjectedInstance = false;

    @State
    boolean mInitializedViews = false;

    @State
    @LayoutRes
    int mInjectionLayoutId;

    @State
    @MenuRes
    int mInjectionOptionMenuId;

    public SupportFragmentDelegate(@NonNull SupportFragmentCompat compat) {
        mCompat = compat;
    }

    public SupportFragmentDelegate(@NonNull SupportFragmentCompat compat, @Nullable LayoutId layoutId, @Nullable MenuId menuId) {
        mCompat = compat;

        if (layoutId != null) {
            mInjectionLayoutId = layoutId.getId();
        }

        if (menuId != null) {
            mInjectionOptionMenuId = menuId.getId();
            mCompat.setHasOptionsMenu(true);
        }
    }

    /**
     * パース対象のLayoutIdを指定する
     */
    public void setLayoutId(@LayoutRes int injectionLayoutId) {
        mInjectionLayoutId = injectionLayoutId;
    }

    /**
     * パース対象のMenuIdを指定する
     */
    public void setOptionMenuId(@MenuRes int injectionOptionMenuId) {
        mInjectionOptionMenuId = injectionOptionMenuId;
        getFragment().setHasOptionsMenu(true);
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mInjectionLayoutId != 0) {
            View result = inflater.inflate(mInjectionLayoutId, container, false);
            MargarineKnife.from(result).to(mCompat).bind();
            // getView対策で、１クッション置いて実行する
            runOnUiThread(() -> {
                onAfterViews();
            });
            return result;
        } else {
            return null;
        }
    }

    /**
     * Created Menu
     */
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (mInjectionOptionMenuId != 0) {
            inflater.inflate(mInjectionOptionMenuId, menu);
            MargarineKnife.bindMenu(menu, mCompat);
            runOnUiThread(() -> {
                mCompat.onAfterBindMenu(this, menu);
            });
        }
    }

    @NonNull
    @CallSuper
    public FragmentManager getFragmentManager() {
        return getFragment().getFragmentManager();
    }

    @NonNull
    @CallSuper
    public FragmentManager getChildFragmentManager() {
        return getFragment().getChildFragmentManager();
    }

    @NonNull
    @CallSuper
    public Fragment getFragment() {
        return mCompat.getFragment(this);
    }

    @Nullable
    @CallSuper
    public Fragment getParentFragment() {
        return getFragment().getParentFragment();
    }

    @CallSuper
    public Activity getActivity() {
        return getFragment().getActivity();
    }

    @CallSuper
    @Nullable
    public View getView() {
        return getFragment().getView();
    }

    @CallSuper
    public <T extends Activity> T getActivity(@NonNull Class<T> clazz) {
        return (T) getActivity();
    }

    @Nullable
    public <T extends View> T findViewById(Class<T> clazz, int id) {
        return (T) getView().findViewById(id);
    }

    @Nullable
    public <T extends View> T findViewByIdFromActivity(Class<T> clazz, int id) {
        return (T) getActivity().findViewById(id);
    }

    /**
     * 親クラスを特定のインターフェースに変換する
     *
     * 変換できない場合、このメソッドはnullを返却する
     */
    @Nullable
    public <T> T getParent(@NonNull Class<T> clazz) {
        Fragment fragment = getParentFragment();
        Activity activity = getActivity();
        if (ReflectionUtil.instanceOf(fragment, clazz)) {
            return (T) fragment;
        }

        if (ReflectionUtil.instanceOf(activity, clazz)) {
            return (T) activity;
        }

        return null;
    }

    /**
     * 親クラスを特定のインターフェースに変換する
     *
     * 変換できない場合、このメソッドはnullを返却する
     */
    @NonNull
    public <T> T getParentOrThrow(@NonNull Class<T> clazz) {
        Fragment fragment = getParentFragment();
        Activity activity = getActivity();
        if (ReflectionUtil.instanceOf(fragment, clazz)) {
            return (T) fragment;
        }

        if (ReflectionUtil.instanceOf(activity, clazz)) {
            return (T) activity;
        }

        throw new IllegalStateException(clazz.getName());
    }

    /**
     * ActionBarを取得する
     */
    public ActionBar getActionBar() {
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            return ((AppCompatActivity) activity).getSupportActionBar();
        } else {
            return null;
        }
    }

    /**
     * View構築が完了した
     */
    @CallSuper
    @UiThread
    protected void onAfterViews() {
        if (!mInitializedViews) {
            mCompat.onAfterViews(this, SupportFragmentCompat.FLAG_AFTERVIEW_INITIALIZE);
            mInitializedViews = true;
        } else {
            mCompat.onAfterViews(this, SupportFragmentCompat.FLAG_AFTERVIEW_RESTORE);
        }
    }

    @CallSuper
    @UiThread
    public void onSaveInstanceState(Bundle outState) {
        Icepick.saveInstanceState(mCompat, outState);
        Icepick.saveInstanceState(this, outState);
    }

    @CallSuper
    @UiThread
    public void onAttach(Context context) {
        if (!mInjectedInstance) {
            Garnet.Builder builder = mCompat.newInjectionBuilder(this, context);
            if (builder == null) {
                builder = Garnet.create(mCompat);
            }
            builder.depend(Context.class, context).inject();
            mInjectedInstance = true;
            runOnUiThread(() -> {
                mCompat.onAfterInjection(this);
            });
        }
    }

    @CallSuper
    @UiThread
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate();
        Icepick.restoreInstanceState(mCompat, savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @CallSuper
    @UiThread
    @Override
    public void onStart() {
        super.onStart();
    }

    @CallSuper
    @UiThread
    @Override
    public void onResume() {
        super.onResume();
        compactAutoDismissDialogs();
    }

    @CallSuper
    @UiThread
    @Override
    public void onStop() {
        super.onStop();
    }

    @CallSuper
    @UiThread
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * backstack idを指定する
     */
    public void setBackStackIndex(int backStackIndex) {
        this.mBackStackIndex = backStackIndex;
    }

    /**
     * Backstackを持つならばtrue
     */
    boolean hasBackstackIndex() {
        return mBackStackIndex != BACKSTACK_NONE;
    }

    public int getBackStackIndex() {
        return mBackStackIndex;
    }

    /**
     * バックスタックが一致したらtrue
     */
    public boolean isCurrentBackstack() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment == null) {
            return mBackStackIndex == getFragment().getFragmentManager().getBackStackEntryCount();
        } else {
            return mBackStackIndex == parentFragment.getFragmentManager().getBackStackEntryCount();
        }
    }

    /**
     * 自身のFragmentから排除する
     *
     * @param withBackStack backstack階層も含めて排除する場合はtrue
     */
    public void detatchSelf(final boolean withBackStack) {
        UIHandler.postUIorRun(() -> {
            if (withBackStack && hasBackstackIndex()) {
                getFragmentManager().popBackStack(mBackStackIndex, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                getFragmentManager().beginTransaction().remove(getFragment()).commit();
            }
        });
    }

    protected boolean hasChildBackStack() {
        return getChildFragmentManager().getBackStackEntryCount() > 0;
    }

    public final String createSimpleTag() {
        return ((Object) this).getClass().getSimpleName();
    }

    /**
     * 戻るボタンのハンドリングを行う
     *
     * @return ハンドリングを行えたらtrue
     */
    public boolean handleBackButton() {
        if (hasChildBackStack()) {
            // backStackを解放する
            getChildFragmentManager().popBackStack();
            return true;
        } else {
            return false;
        }
    }

    public void startActivityForResult(Intent intent, int requestCode) {
        if (getParentFragment() != null) {
            getParentFragment().startActivityForResult(intent, requestCode);
        } else {
            getFragment().startActivityForResult(intent, requestCode);
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return ActivityResult.invokeRecursive(getFragment(), requestCode, resultCode, data);
    }

    /**
     * Runtime Permissionの更新を行わせる
     *
     * @return パーミッション取得を開始した場合はtrue
     */
    public boolean requestRuntimePermission(String[] permissions) {
        return AppSupportUtil.requestRuntimePermissions(getActivity(), permissions);
    }

    /**
     * Runtime Permissionのブロードキャストを行わせる
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        AppSupportUtil.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults);
    }

    public boolean requestRuntimePermission(PermissionUtil.PermissionType type) {
        return requestRuntimePermission(type.getPermissions());
    }
}