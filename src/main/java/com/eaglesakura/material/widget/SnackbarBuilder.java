package com.eaglesakura.material.widget;

import com.eaglesakura.sloth.R;
import com.eaglesakura.android.util.ResourceUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

/**
 * SnackBarを制御する
 */
@SuppressLint("NewApi")
public class SnackbarBuilder {

    protected String mMessage;

    protected View mContainer;

    protected int mDuration = Snackbar.LENGTH_SHORT;

    protected String mActionText;

    protected Runnable mAction;

    protected Snackbar mSnackbar;

    public SnackbarBuilder(View container) {
        mContainer = container;
    }

    public SnackbarBuilder duration(int duration) {
        mDuration = duration;
        return this;
    }

    public SnackbarBuilder message(String message) {
        mMessage = message;
        return this;
    }

    public SnackbarBuilder message(@StringRes int resId, Object... arg) {
        return message(mContainer.getResources().getString(resId, arg));
    }

    public SnackbarBuilder action(String text, Runnable callback) {
        mActionText = text;
        mAction = callback;
        return this;
    }

    public SnackbarBuilder show() {
        mSnackbar = Snackbar.make(mContainer, mMessage, mDuration);
        View snackbarView = mSnackbar.getView();
        snackbarView.setBackground(new ColorDrawable(ResourceUtil.argb(mContainer.getContext(), R.color.EsMaterial_Grey_50)));
        ((TextView) snackbarView.findViewById(R.id.snackbar_text)).setTextColor(ResourceUtil.argb(mContainer.getContext(), R.color.EsMaterial_Grey_800));
        if (mAction != null) {
            mSnackbar.setAction(mActionText, view -> mAction.run());
        }
        mSnackbar.show();
        return this;
    }

    public static SnackbarBuilder from(View view) {
        return new SnackbarBuilder(view);
    }

    public static SnackbarBuilder from(Activity activity) {
        return from(activity.findViewById(R.id.Content_Holder_Root));
    }

    public static SnackbarBuilder from(Fragment fragment) {
        return from(fragment.getActivity());
    }
}
