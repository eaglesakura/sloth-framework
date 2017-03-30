package com.eaglesakura.sloth.content.property;

import com.eaglesakura.sloth.content.ApplicationDataProvider;
import com.eaglesakura.android.property.PropertyStore;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

/**
 * ContentProviderと通信してプロパティを管理する
 */
public class ProviderPropertyStore implements PropertyStore {
    final Context mContext;

    final String mStoreKey;

    final Uri mPropertyUri;

    public ProviderPropertyStore(Context context, String storeKey, Uri propertyUri) {
        mContext = context;
        mStoreKey = storeKey;
        mPropertyUri = propertyUri;
    }

    @Override
    public String getStringProperty(String key) {
        byte[] buffer = ApplicationDataProvider.query(mContext, mPropertyUri, PropertyProviderHandler.COMMAND_GET, new String[]{
                mStoreKey,
                key
        });
        if (buffer != null) {
            return new String(buffer);
        }
        return null;
    }

    @Override
    public void setProperty(String key, String value) {
        ContentValues values = new ContentValues();
        values.put("storeKey", mStoreKey);
        values.put("propKey", key);
        values.put("value", value);

        ApplicationDataProvider.insert(mContext, mPropertyUri, PropertyProviderHandler.COMMAND_SET, values);
    }

    @Override
    public void clear() {
        ContentValues values = new ContentValues();
        values.put("storeKey", mStoreKey);
        ApplicationDataProvider.insert(mContext, mPropertyUri, PropertyProviderHandler.COMMAND_CLEAR, values);
    }

    @Override
    public void commit() {
        ContentValues values = new ContentValues();
        values.put("storeKey", mStoreKey);
        ApplicationDataProvider.insert(mContext, mPropertyUri, PropertyProviderHandler.COMMAND_COMMIT, values);
    }
}
