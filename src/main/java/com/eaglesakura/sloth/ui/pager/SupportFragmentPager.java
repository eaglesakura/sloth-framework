package com.eaglesakura.sloth.ui.pager;

import com.eaglesakura.cerberus.LifecycleState;
import com.eaglesakura.sloth.SlothLog;
import com.eaglesakura.sloth.Sloth;
import com.eaglesakura.sloth.app.SlothFragment;

import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewPager機能をサポートする
 *
 * 専用Adapter生成, Fragmentライフサイクル管理を行う。
 * 内部では {@link FragmentStatePagerAdapter} の利用をベースに生成される。
 */
public class SupportFragmentPager {

    @NonNull
    protected final List<PagerFragmentHolder> mFragments = new ArrayList<>();

    @IdRes
    protected final int mContainerId;

    public SupportFragmentPager(@IdRes int pagerId) {
        mContainerId = pagerId;
    }

    /**
     * Fragment生成器を追加する
     */
    public void addFragment(FragmentCreator creator) {
        mFragments.add(new PagerFragmentHolder(creator));
    }

    /**
     * Fragmentを取得する
     */
    public Fragment getFragment(FragmentManager fragmentManager, int index) {

        // 既存のFragmentを検索する
        String tag = getPagerFragmentTag(index);
        Fragment managedFragment = fragmentManager.findFragmentByTag(tag);
        if (managedFragment != null) {
            return managedFragment;
        } else {
            return mFragments.get(index).get(index, fragmentManager);
        }
    }

    public CharSequence getFragmentTitle(FragmentManager fragmentManager, int index) {
        Fragment fragment = getFragment(fragmentManager, index);
        if (fragment instanceof FragmentPagerTitle) {
            return ((FragmentPagerTitle) fragment).getTitle(Sloth.getApplication());
        } else {
            return null;
        }
    }

    /**
     * Fragment数を取得する
     */
    @IntRange(from = 0)
    public int size() {
        return mFragments.size();
    }

    /**
     * Adapterを生成する
     */
    @NonNull
    public PagerAdapter newAdapter(FragmentManager fragmentManager) {
        return new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                return getFragment(fragmentManager, position);
            }

            @Override
            public int getCount() {
                return size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return getFragmentTitle(fragmentManager, position);
            }
        };
    }

    public static FragmentCreator newFragmentCreator(Class<? extends Fragment> clazz) {
        return (self) -> {
            try {
                return clazz.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        };
    }

    private String getPagerFragmentTag(int index) {
        return "android:switcher:" + mContainerId + ":" + index;
    }

    private void compactCaches() {
        for (PagerFragmentHolder holder : mFragments) {
            if (holder.mCacheFragment instanceof SlothFragment) {
                SlothFragment cacheFragment = (SlothFragment) holder.mCacheFragment;
                LifecycleState lifecycleState = cacheFragment.getLifecycleState();

                if (lifecycleState.ordinal() >= LifecycleState.OnDestroy.ordinal()) {
                    // 廃棄済みのため、キャッシュを削除すべき
                    SlothLog.system("ViewPager FragmentCacheClean[%s] lifecycle[%s]", cacheFragment.toString(), lifecycleState.toString());
                    holder.mCacheFragment = null;
                }
            }
        }
    }

    private class PagerFragmentHolder {
        /**
         * キャッシュが無い場合に生成させる
         */
        private final FragmentCreator mCreator;

        /**
         * Fragmentキャッシュ
         */
        private Fragment mCacheFragment;

        PagerFragmentHolder(FragmentCreator creator) {
            mCreator = creator;
        }

        /**
         * ページ用Fragmentを取得する
         */
        Fragment get(int index, FragmentManager fragmentManager) {
            String tag = getPagerFragmentTag(index);
            // まずはFragmentManagerから探す
            Fragment addedFragment = fragmentManager.findFragmentByTag(tag);
            if (addedFragment != null) {
                mCacheFragment = addedFragment;
            }

            // キャッシュクリーンを行う
            compactCaches();

            if (mCacheFragment == null) {
                // キャッシュにも無いならば、生成させる
                mCacheFragment = mCreator.newInstance(SupportFragmentPager.this);
                if (mCacheFragment == null) {
                    throw new IllegalStateException();
                }
                SlothLog.system("ViewPager Create TabFragment[%s]", mCacheFragment.toString());
            }
            return mCacheFragment;
        }
    }

    public interface FragmentCreator {
        /**
         * Fragmentを新たに生成する
         */
        @NonNull
        Fragment newInstance(SupportFragmentPager self);
    }
}
