package com.eaglesakura.android.property.model;

import com.eaglesakura.android.db.UnitTestCase;
import com.eaglesakura.json.JSON;
import com.eaglesakura.util.CollectionUtil;

import org.junit.Test;

import android.annotation.SuppressLint;

import java.io.FileInputStream;
import java.io.InputStream;

@SuppressLint("NewApi")
public class PropertySourceTest extends UnitTestCase {

    @Test
    public void プロパティシートをパースできる() throws Throwable {
        PropertySource src;
        try (InputStream is = new FileInputStream(getPropertyJson())) {
            src = JSON.decode(is, PropertySource.class);
        }

        assertFalse(CollectionUtil.isEmpty(src.groups));
        assertFalse(CollectionUtil.isEmpty(src.groups.get(0).properties));
        assertFalse(CollectionUtil.isEmpty(src.groups.get(1).properties));
    }
}