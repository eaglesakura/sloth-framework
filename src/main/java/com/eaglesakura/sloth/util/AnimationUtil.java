package com.eaglesakura.sloth.util;

import com.eaglesakura.sloth.R;

import android.support.v4.app.FragmentTransaction;

/**
 *
 */
public class AnimationUtil {

    /**
     * Fragmentが上から表示される
     */
    public static FragmentTransaction fragmentFromUpper(FragmentTransaction transaction) {
        transaction.setCustomAnimations(
                R.anim.fragment_fromupper_upper_enter,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_fromupper_upper_exit
        );
        return transaction;
    }

    /**
     * Fragmentが下から表示される
     */
    public static FragmentTransaction fragmentFromLower(FragmentTransaction transaction) {
        transaction.setCustomAnimations(
                R.anim.fragment_fromlower_upper_enter,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_fromlower_upper_exit
        );
        return transaction;
    }
}
