package com.eaglesakura.bundle;

import android.os.Bundle;

import java.lang.reflect.Field;

public interface Collector {
    void onSaveInstance(Bundle state, String key, Object srcObject, Field srcField) throws Throwable;

    void onRestoreInstance(Bundle state, String key, Object dstObject, Field dstField) throws Throwable;
}
