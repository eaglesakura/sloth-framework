package com.eaglesakura.sloth.database.property;

import com.eaglesakura.sloth.database.property.model.PropertySource;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

/**
 * 値が不変となるProperty
 */
public class ImmutableTextPropertyStore extends TextPropertyStore {
    public ImmutableTextPropertyStore(@NonNull PropertySource source, @Nullable Map<String, String> map) {
        loadProperties(source);
        if (map != null) {
            loadProperties(map);
        }
    }

    @Override
    public void setProperty(String key, String value) {
        throw new UnsupportedOperationException();
    }
}
