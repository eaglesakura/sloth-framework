package com.eaglesakura.sloth.app;

import com.eaglesakura.lambda.Action1;
import com.eaglesakura.lambda.Action2;
import com.eaglesakura.lambda.Action3;
import com.eaglesakura.sloth.SlothLog;
import com.eaglesakura.sloth.app.lifecycle.Lifecycle;
import com.eaglesakura.sloth.app.lifecycle.event.OnCreateEvent;
import com.eaglesakura.util.ReflectionUtil;
import com.eaglesakura.util.StringUtil;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * findFragment系を代行し、透過的にFragment取得を行えるようにする。
 *
 * FragmentManagerからFragmentが見つからない場合、Fragmentを新規に生成する。
 * もしFragmentManagerからFragmentが見つかった場合、該当Fragmentを返却する。
 */
public abstract class FragmentHolder<T extends Fragment> {
    @Nullable
    T mFragment;

    @NonNull
    String mFragmentTag;

    @NonNull
    final Object mParent;

    @IdRes
    final int mHolderId;

    public FragmentHolder(@NonNull Activity parent, @IdRes int holderId, @NonNull String tag) {
        mParent = parent;
        mHolderId = holderId;
        mFragmentTag = tag;
    }

    public FragmentHolder(@NonNull Fragment parent, @IdRes int holderId, @NonNull String tag) {
        mParent = parent;
        mHolderId = holderId;
        mFragmentTag = tag;
    }

    private FragmentManager getFragmentManager() {
        if (mParent instanceof AppCompatActivity) {
            return ((AppCompatActivity) mParent).getSupportFragmentManager();
        } else if (mParent instanceof Fragment) {
            return ((Fragment) mParent).getChildFragmentManager();
        }
        throw new IllegalStateException();
    }

    @NonNull
    protected abstract T newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            try {
                mFragment = newFragmentInstance(savedInstanceState);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            if (mFragment != null) {
                if (mHolderId == 0) {
                    transaction.add(mFragment, mFragmentTag).commit();
                } else {
                    transaction.add(mHolderId, mFragment, mFragmentTag).commit();
                }
            }
        } else {
            // find fragment
            mFragment = ((T) getFragmentManager().findFragmentByTag(mFragmentTag));
        }
    }

    public void onResume() {
        mFragment = (T) getFragmentManager().findFragmentByTag(mFragmentTag);
    }

    /**
     * 生成済みである場合はtrue
     */
    public boolean isCreated() {
        return mFragment != null;
    }

    /**
     * コンテンツを切り替える
     */
    public <T2 extends T> void replace(@NonNull T2 fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(mHolderId, fragment, mFragmentTag)
                .commit();
        mFragment = fragment;
    }

    /**
     * コンテンツを切り替える
     *
     * @param fragment          新たなFragment
     * @param transactionAction (transaction, holderId, tag)を渡し、必要なトランザクション処理を行う
     */
    public <T2 extends T> void transaction(@NonNull T2 fragment, Action3<FragmentTransaction, Integer, String> transactionAction) {
        try {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transactionAction.action(transaction, mHolderId, mFragmentTag);
            transaction.commit();
            mFragment = fragment;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public T get() {
        if (mFragment == null) {
            throw new IllegalStateException();
        }

        return mFragment;
    }

    public boolean instanceOf(Class<?> clazz) {
        if (mFragment == null) {
            return false;
        } else {
            return ReflectionUtil.instanceOf(mFragment, clazz);
        }
    }

    /**
     * 現在管理しているFragmentがclazzもしくはサブクラスである場合にactionを実行する。
     * nullである場合には何も行わない。
     *
     * @param clazz  チェック対象class
     * @param action 実行するアクション
     * @param <T2>   キャスト対象のClass
     */
    public <T2 extends T> void ifPresent(Class<T2> clazz, Action1<T2> action) {
        if (mFragment == null) {
            return;
        }

        try {
            if (ReflectionUtil.instanceOf(mFragment, clazz)) {
                action.action((T2) mFragment);
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Fragmentを削除する
     */
    public void remove() {
        getFragmentManager()
                .beginTransaction()
                .remove(get())
                .commit();

        mFragment = null;
    }

    /**
     * Android Architecture Componentsに合わせ、 {@link FragmentHolder#subscribe(Lifecycle)} を推奨
     */
    @Deprecated
    public FragmentHolder<T> bind(Lifecycle delegate) {
        return subscribe(delegate);
    }

    /**
     * ライフサイクルに同期して必要な処理を行う
     */
    public FragmentHolder<T> subscribe(Lifecycle delegate) {
        delegate.subscribe(it -> {
            switch (it.getState()) {
                case OnCreate:
                    onCreate(((OnCreateEvent) it).getBundle());
                    return;
                case OnResume:
                    onResume();
                    return;
            }
        });
        return this;
    }


    public static <T extends Fragment> FragmentHolder<T> newStub(@NonNull Fragment parent, @IdRes int holderId, @NonNull String tag) {
        return new FragmentHolder<T>(parent, holderId, tag) {
            @NonNull
            @Override
            protected T newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
                return null;
            }
        };
    }

    public static <T extends Fragment> FragmentHolder<T> newInstance(@NonNull Fragment parent, @NonNull Class<? extends T> aClass, @IdRes int holderId) {
        return new FragmentHolder<T>(parent, holderId, aClass.getName()) {
            @NonNull
            @Override
            protected T newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
                return aClass.newInstance();
            }
        };
    }


    public static <T extends Fragment> FragmentHolder<T> newInstance(@NonNull Activity parent, @NonNull Class<? extends T> aClass, @IdRes int holderId) {
        return new FragmentHolder<T>(parent, holderId, aClass.getName()) {
            @NonNull
            @Override
            protected T newFragmentInstance(@Nullable Bundle savedInstanceState) throws Exception {
                return aClass.newInstance();
            }
        };
    }


}
