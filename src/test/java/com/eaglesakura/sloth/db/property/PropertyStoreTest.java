package com.eaglesakura.sloth.db.property;

import com.eaglesakura.json.JSON;
import com.eaglesakura.sloth.UnitTestCase;
import com.eaglesakura.sloth.db.property.model.PropertySource;
import com.eaglesakura.util.CollectionUtil;

import org.junit.Test;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@SuppressLint("NewApi")
public class PropertyStoreTest extends UnitTestCase {

    void assertValues(PropertyStore store) throws Throwable {
        Properties props = new Properties();
        props.setPropertyStore(store);

        assertEquals(props.getIntProperty("Sample.intKey"), 123);
        assertEquals(props.getLongProperty("Sample.longKey"), 456);
        assertEquals(props.getStringProperty("Sample.stringKey"), "nil");
        assertEquals(props.getFloatProperty("Sample.floatKey"), 1.234, 0.0001);
        assertEquals(props.getDoubleProperty("Sample.doubleKey"), 2.345, 0.0001);
        assertEquals(props.getBooleanProperty("Sample.booleanKey"), false);
        assertEquals(props.getBooleanProperty("Debug.booleanKey"), true);

        // 値を変更できる
        store.setProperty("Sample.intKey", "234");
        assertEquals(props.getIntProperty("Sample.intKey"), 234);

        store.setProperty("Sample.booleanKey", "true");
        assertEquals(props.getBooleanProperty("Sample.booleanKey"), true);
    }

    @Test
    public void プロパティシートからKeyValueを生成できる() throws Throwable {
        assertValues(new TextPropertyStore().loadProperties(loadProperties()));
    }

    @Test
    public void プロパティシートのシリアライズとデシリアライズができる() throws Throwable {
        TextPropertyStore store = new TextPropertyStore().loadProperties(loadProperties());
        assertValues(store);

        byte[] serialize = store.serialize();
        assertFalse(CollectionUtil.isEmpty(serialize));

        // リロードする
        store.loadProperties(loadProperties());
        assertValues(store);

        store.loadProperties(loadProperties());
        store.deserialize(serialize);

        // 最後の値を取得できている
        {
            Properties props = new Properties();
            props.setPropertyStore(store);
            assertEquals(props.getIntProperty("Sample.intKey"), 234);
            assertEquals(props.getBooleanProperty("Sample.booleanKey"), true);
        }

    }

    @Test
    public void プロパティシートからDBのKeyValueを生成できる() throws Throwable {

        // 複数回clearとcommitで値リセットが行える
        for (int i = 0; i < 2; ++i) {
            {
                TextDatabasePropertyStore props = (TextDatabasePropertyStore) new TextDatabasePropertyStore(getContext(), "test-props.db").loadProperties(loadProperties());
                assertValues(props);

                // DBに書き出す
                props.commit();
            }

            // ロード値をテストする
            {
                Properties props = new Properties();
                props.setPropertyStore(new TextDatabasePropertyStore(getContext(), "test-props.db").loadProperties(loadProperties()));

                // 最後の値を取得できている
                assertEquals(props.getIntProperty("Sample.intKey"), 234);
                assertEquals(props.getBooleanProperty("Sample.booleanKey"), true);

                props.clear();
                props.commit();
            }
        }
    }

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


    private File getPropertyJson() {
        return getTestAsset("properties.json");
    }

    @SuppressLint("NewApi")
    private PropertySource loadProperties() throws Throwable {
        try (InputStream is = new FileInputStream(getPropertyJson())) {
            return JSON.decode(is, PropertySource.class);
        }
    }

}