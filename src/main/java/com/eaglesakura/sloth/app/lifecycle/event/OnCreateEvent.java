package com.eaglesakura.sloth.app.lifecycle.event;

import android.os.Bundle;
import android.support.annotation.Nullable;

public class OnCreateEvent implements LifecycleEvent {
    @Nullable
    final Bundle mBundle;

    public OnCreateEvent(Bundle bundle) {
        mBundle = bundle;
    }

    @Override
    public State getState() {
        return State.OnCreate;
    }

    @Nullable
    public Bundle getBundle() {
        return mBundle;
    }
}
