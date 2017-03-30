package com.eaglesakura.sloth.content.property;

import com.eaglesakura.android.property.PropertyStore;
import com.eaglesakura.android.property.TextDatabasePropertyStore;
import com.eaglesakura.android.property.model.PropertySource;
import com.eaglesakura.json.JSON;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RemotePropertyManager {
    final Context mContext;

    /**
     *
     */
    protected Map<String, PropertyStore> mPropertyStore = new HashMap<>();

    public RemotePropertyManager(Context context) {
        mContext = context;
    }

    /**
     * 管理するStoreを追加する
     *
     * @param key   Storeのキー
     * @param store ストア本体
     */
    public RemotePropertyManager addStore(@NonNull String key, @NonNull PropertyStore store) {
        synchronized (mPropertyStore) {
            mPropertyStore.put(key, store);
        }
        return this;
    }

    /**
     * 管理するStoreをAssetsから追加する。
     *
     * DBによって保存管理される
     *
     * @param storeKey  Storeを特定するキー
     * @param dbName    データベースファイル名
     * @param assetPath プロパティリストファイル
     */
    @SuppressLint("NewApi")
    public RemotePropertyManager addDatabaseStore(@NonNull String storeKey, @NonNull String dbName, @NonNull String assetPath) {
        try (InputStream is = mContext.getAssets().open(assetPath)) {
            PropertySource source = JSON.decode(is, PropertySource.class);

            TextDatabasePropertyStore store = new TextDatabasePropertyStore(mContext, dbName);
            return addStore(storeKey, store);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 管理対象のStoreを取得する
     */
    public PropertyStore getStore(@NonNull String key) {
        synchronized (mPropertyStore) {
            return mPropertyStore.get(key);
        }
    }

    /**
     * 管理対象の文字列を取得する
     */
    public String getStringValue(@NonNull String storeKey, @NonNull String propKey) {
        PropertyStore store = getStore(storeKey);
        if (store == null) {
            return null;
        }

        synchronized (store) {
            return store.getStringProperty(propKey);
        }
    }

    /**
     * 文字列を保存する
     */
    public void putStringValue(@NonNull String storeKey, @NonNull String propKey, @NonNull String value) {
        PropertyStore store = getStore(storeKey);
        if (store == null) {
            return;
        }

        synchronized (store) {
            store.setProperty(propKey, value);
        }
    }

    /**
     * 結果を保存する
     */
    public void clear() {
        synchronized (mPropertyStore) {
            for (Map.Entry<String, PropertyStore> entry : mPropertyStore.entrySet()) {
                entry.getValue().clear();
            }
        }
    }

    /**
     * 結果を保存する
     */
    public void commit() {
        synchronized (mPropertyStore) {
            for (Map.Entry<String, PropertyStore> entry : mPropertyStore.entrySet()) {
                entry.getValue().commit();
            }
        }
    }

}
