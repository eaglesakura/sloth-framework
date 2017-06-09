package com.eaglesakura.sloth.app.lifecycle.event;

import android.os.Bundle;
import android.support.annotation.NonNull;

public class OnRestoreEvent implements LifecycleEvent {
    @NonNull
    final Bundle mBundle;

    public OnRestoreEvent(Bundle bundle) {
        mBundle = bundle;
    }

    @Override
    public State getState() {
        return State.OnRestoreInstanceState;
    }

    @NonNull
    public Bundle getBundle() {
        return mBundle;
    }
}
