package com.eaglesakura.sloth.db.property;

import com.eaglesakura.util.StringUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * 簡易設定用のプロパティを保持するためのクラス
 */
public class Properties {
    private PropertyStore mPropertyStore;

    public PropertyStore getPropertyStore() {
        return mPropertyStore;
    }

    public void setPropertyStore(PropertyStore propertyStore) {
        mPropertyStore = propertyStore;
    }

    public String getStringProperty(String key) {
        return mPropertyStore.getStringProperty(key);
    }

    public void setProperty(String key, Object value) {
        if (value instanceof Enum) {
            value = ((Enum) value).name();
        } else if (value instanceof Bitmap) {
            try {
                Bitmap bmp = (Bitmap) value;
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, os);

                value = os.toByteArray();
            } catch (Exception e) {
                value = null;
            }
        } else if (value instanceof Boolean) {
            // trueならば"1"、falseならば"0"としてしまう
            value = Boolean.TRUE.equals(value) ? "1" : "0";
        }

        if (value instanceof byte[]) {
            mPropertyStore.setProperty(key, StringUtil.toString((byte[]) value));
        } else {
            mPropertyStore.setProperty(key, value.toString());
        }
    }

    public void clear() {
        mPropertyStore.clear();
    }

    public void commit() {
        mPropertyStore.commit();
    }

    public int getIntProperty(String key) {
        return Integer.parseInt(getStringProperty(key));
    }

    public long getLongProperty(String key) {
        return Long.parseLong(getStringProperty(key));
    }

    public Date getDateProperty(String key) {
        return new Date(getLongProperty(key));
    }

    public float getFloatProperty(String key) {
        return Float.parseFloat(getStringProperty(key));
    }

    public boolean getBooleanProperty(String key) {
        String value = getStringProperty(key);

        // 保存速度を向上するため、0|1判定にも対応する
        if ("0".equals(value)) {
            return false;
        } else if ("1".equals(value)) {
            return true;
        }
        return Boolean.parseBoolean(getStringProperty(key));
    }

    public double getDoubleProperty(String key) {
        return Double.parseDouble(getStringProperty(key));
    }

    /**
     * 画像ファイル形式で保存してあるBitmapを取得する
     */
    @Nullable
    public Bitmap getImageProperty(String key) {
        byte[] imageFile = getByteArrayProperty(key);
        if (imageFile == null) {
            return null;
        }

        try {
            return BitmapFactory.decodeStream(new ByteArrayInputStream(imageFile));
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * base64エンコードオブジェクトを取得する
     */
    @Nullable
    public byte[] getByteArrayProperty(String key) {
        try {
            return StringUtil.toByteArray(getStringProperty(key));
        } catch (Exception e) {
            return null;
        }
    }
}
