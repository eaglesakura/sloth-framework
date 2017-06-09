package com.eaglesakura.sloth.app.lifecycle.event;

import android.os.Bundle;
import android.support.annotation.NonNull;

public class OnSaveInstanceStateEvent implements LifecycleEvent {
    @NonNull
    final Bundle mBundle;

    public OnSaveInstanceStateEvent(Bundle bundle) {
        mBundle = bundle;
    }

    @Override
    public State getState() {
        return State.OnSaveInstanceState;
    }

    @NonNull
    public Bundle getBundle() {
        return mBundle;
    }
}
