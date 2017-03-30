package com.eaglesakura.sloth.delegate.fragment;

import android.content.Context;

/**
 * Fragmentにタイトルをもたせるインターフェース
 */
public interface FragmentPagerTitle {
    CharSequence getTitle(Context context);
}
