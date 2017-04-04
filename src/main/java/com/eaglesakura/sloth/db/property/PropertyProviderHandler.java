package com.eaglesakura.sloth.db.property;

import com.eaglesakura.sloth.db.UriHandler;
import com.eaglesakura.util.StringUtil;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class PropertyProviderHandler extends RemotePropertyManager implements UriHandler {
    /**
     * 対応するURI
     */
    final Uri mSupportUri;

    public static final String COMMAND_GET = "prop.GET";

    public static final String COMMAND_SET = "prop.SET";

    public static final String COMMAND_COMMIT = "prop.COMMIT";

    public static final String COMMAND_CLEAR = "prop.COMMAND_CLEAR";

    public PropertyProviderHandler(Context context, Uri supportUri) {
        super(context);
        mSupportUri = supportUri;
    }

    @Override
    public boolean isSupport(Uri uri) {
        return uri.equals(mSupportUri);
    }


    @Nullable
    @Override
    public byte[] query(@NonNull Uri uri, @NonNull String command, @NonNull String[] argments) {
        if (COMMAND_GET.equals(command)) {
            String storeKey = argments[0];
            String propKey = argments[1];

            String value = getStringValue(storeKey, propKey);
            if (value == null) {
                return new byte[0];
            } else {
                return value.getBytes();
            }
        }
        return new byte[0];
    }

    @Override
    public void insert(@NonNull Uri uri, @NonNull String command, @NonNull ContentValues values) {
        if (command.equals(COMMAND_SET)) {
            String storeKey = values.getAsString("storeKey");
            String propKey = values.getAsString("propKey");
            String value = values.getAsString("value");

            // 全てデータが揃ったらput
            if (StringUtil.allNotEmpty(storeKey, propKey, value)) {
                putStringValue(storeKey, propKey, value);
            }
        } else if (command.equals(COMMAND_CLEAR)) {
            // データをクリアする
            String storeKey = values.getAsString("storeKey");
            if (StringUtil.isEmpty(storeKey)) {
                clear();
            } else {
                PropertyStore store = getStore(storeKey);
                if (store != null) {
                    store.clear();
                }
            }
        } else if (command.equals(COMMAND_COMMIT)) {
            // データをコミットする
            String storeKey = values.getAsString("storeKey");
            if (StringUtil.isEmpty(storeKey)) {
                commit();
            } else {
                PropertyStore store = getStore(storeKey);
                if (store != null) {
                    store.commit();
                }
            }
        }
    }

    @Override
    public void onShutdown() {

    }
}
