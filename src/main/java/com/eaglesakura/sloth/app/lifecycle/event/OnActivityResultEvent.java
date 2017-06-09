package com.eaglesakura.sloth.app.lifecycle.event;

import android.content.Intent;

/**
 * OnActivityResult
 */
public class OnActivityResultEvent implements LifecycleEvent {
    int mRequestCode;

    int mResult;

    Intent mData;

    public OnActivityResultEvent(int requestCode, int result, Intent data) {
        mRequestCode = requestCode;
        mResult = result;
        mData = data;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public int getResult() {
        return mResult;
    }

    public Intent getData() {
        return mData;
    }

    @Override
    public State getState() {
        return State.OnActivityResult;
    }
}
