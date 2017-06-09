package com.eaglesakura.sloth.app.lifecycle.event;

import android.content.Context;
import android.support.annotation.NonNull;

public class OnAttachEvent implements LifecycleEvent {

    @NonNull
    Context mContext;

    public OnAttachEvent(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public State getState() {
        return State.OnAttach;
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }
}
