package com.eaglesakura.sloth.app.lifecycle.event;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class OnCreateViewEvent implements LifecycleEvent {
    @NonNull
    final LayoutInflater inflater;

    @Nullable
    final ViewGroup container;

    @Nullable
    final Bundle savedInstanceState;

    public OnCreateViewEvent(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        this.savedInstanceState = savedInstanceState;
    }

    @Override
    public State getState() {
        return State.OnCreateView;
    }

    @NonNull
    public LayoutInflater getInflater() {
        return inflater;
    }

    @Nullable
    public ViewGroup getContainer() {
        return container;
    }

    @Nullable
    public Bundle getSavedInstanceState() {
        return savedInstanceState;
    }
}
