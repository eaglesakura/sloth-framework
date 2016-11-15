package com.eaglesakura.android.framework.delegate.fragment;

import android.content.Context;

/**
 * Fragmentにタイトルをもたせるインターフェース
 */
public interface FragmentPagerTitle {
    CharSequence getTitle(Context context);
}
