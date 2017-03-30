package com.eaglesakura.android.property;

import com.eaglesakura.android.property.model.PropertySource;
import com.eaglesakura.util.SerializeUtil;
import com.eaglesakura.util.StringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Text Key-Valueのプロパティを保持する
 */
public class TextPropertyStore implements PropertyStore {

    @Override
    public String getStringProperty(String key) {
        return mPropMap.get(key).mValue;
    }

    @Override
    public void setProperty(String key, String value) {
        Property prop = mPropMap.get(key);
        prop.mValue = value;
        prop.mModified = true;
    }

    /**
     * デフォルト値のロードを行う
     *
     * @param src 元データ
     */
    public TextPropertyStore loadProperties(PropertySource src) {
        for (PropertySource.Group group : src.groups) {
            for (PropertySource.Property prop : group.properties) {
                addProperty(group.name + "." + prop.name, prop.value);
            }
        }
        return this;
    }

    /**
     * プロパティをまとめて登録する
     *
     * @param values key-value一覧
     */
    public void addProperty(Map<String, String> values) {
        for (Map.Entry<String, String> item : values.entrySet()) {
            addProperty(item.getKey(), item.getValue());
        }
    }

    /**
     * プロパティを追加する
     */
    public void addProperty(String key, String defaultValue) {
        mPropMap.put(key, new Property(key, defaultValue));
    }

    @Override
    public void clear() {
        Iterator<Map.Entry<String, Property>> iterator = mPropMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Property> entry = iterator.next();
            Property prop = entry.getValue();
            if (prop.mValue != null && !prop.mValue.equals(prop.mDefaultValue)) {
                prop.mModified = true;
            }
            prop.mValue = prop.mDefaultValue;
        }
    }

    /**
     * Key-ValueのMAPに変換する
     */
    public Map<String, String> asMap() {
        Map<String, String> datas = new HashMap<>();
        Iterator<Map.Entry<String, Property>> itr = mPropMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Property> entry = itr.next();
            Property prop = entry.getValue();
            if (!StringUtil.isEmpty(prop.mValue)) {
                datas.put(prop.mKey, prop.mValue);
            } else {
                datas.put(prop.mKey, prop.mDefaultValue);
            }
        }
        return datas;
    }

    /**
     * Mapから復元する
     */
    public void loadProperties(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Property property = mPropMap.get(entry.getKey());
            if (property != null) {
                property.mValue = entry.getValue();
                property.mModified = true;
            }
        }
    }

    /**
     * プロパティ一覧をシリアライズする
     */
    public byte[] serialize() {
        Map<String, byte[]> datas = new HashMap<>();
        Iterator<Map.Entry<String, Property>> itr = mPropMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Property> entry = itr.next();
            Property prop = entry.getValue();
            if (!StringUtil.isEmpty(prop.mValue)) {
                datas.put(prop.mKey, prop.mValue.getBytes());
            }
        }
        return SerializeUtil.toByteArray(datas);
    }

    /**
     * シリアライズしたバッファを元に戻す
     */
    public void deserialize(byte[] buffer) throws IOException {
        try {
            Map<String, byte[]> datas = SerializeUtil.toKeyValue(buffer);

            Iterator<Map.Entry<String, Property>> itr = mPropMap.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, Property> entry = itr.next();
                Property prop = entry.getValue();

                byte[] value = datas.get(prop.mKey);
                if (value != null) {
                    // 値が書き込まれていた場合はそちらを優先
                    prop.mValue = new String(value);
                } else {
                    // 値が書き込まれていないので、デフォルトを復元
                    prop.mValue = prop.mDefaultValue;
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void commit() {
        // not working
    }

    /**
     * ロード済みのプロパティ
     */
    protected final Map<String, Property> mPropMap = new HashMap<>();


    /**
     * テキストで保持されたプロパティ
     */
    protected static class Property {
        /**
         * 現在の値
         */
        String mValue;

        /**
         * デフォルト値
         */
        final String mDefaultValue;

        /**
         * データベース用のkey
         */
        final String mKey;

        /**
         * 読み込み後、値を更新していたらtrue
         */
        boolean mModified = false;

        Property(String key, String defaultValue) {
            this.mKey = key;
            this.mValue = defaultValue;
            this.mDefaultValue = defaultValue;
        }
    }
}
