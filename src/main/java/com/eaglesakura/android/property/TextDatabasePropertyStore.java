package com.eaglesakura.android.property;

import com.eaglesakura.android.db.DBOpenType;
import com.eaglesakura.android.db.TextKeyValueStore;
import com.eaglesakura.android.property.model.PropertySource;

import android.content.Context;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * データベースを利用したKey-Value Store
 */
public class TextDatabasePropertyStore extends TextPropertyStore {
    Context mContext;

    File mDatabasePath;

    final Object lock = new Object();

    public TextDatabasePropertyStore(Context context, String dbName) {
        this(context, context.getDatabasePath(dbName));
    }

    public TextDatabasePropertyStore(Context context, File databasePath) {
        mContext = context;
        mDatabasePath = databasePath;
    }

    @Override
    public TextPropertyStore loadProperties(PropertySource src) {
        synchronized (lock) {
            super.loadProperties(src);

            TextKeyValueStore kvs = new TextKeyValueStore(mContext, mDatabasePath, TextKeyValueStore.TABLE_NAME_DEFAULT);
            try {
                kvs.open(DBOpenType.Read);

                Iterator<Map.Entry<String, Property>> iterator = mPropMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Property value = iterator.next().getValue();
                    // リロードする。読み込めなかった場合は規定のデフォルト値を持たせる
                    value.mValue = kvs.get(value.mKey, value.mDefaultValue);
                    // sync直後なのでcommit対象ではない
                    value.mModified = false;
                }
            } finally {
                kvs.close();
            }
        }
        return this;
    }

    @Override
    public void commit() {
        synchronized (lock) {
            final Map<String, String> commitValues = new HashMap<>();

            // Commitする内容を抽出する
            {
                Iterator<Map.Entry<String, Property>> iterator = mPropMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Property property = iterator.next().getValue();
                    if (property.mModified) {
                        commitValues.put(property.mKey, property.mValue);
                    }
                }
            }

            // 不要であれば何もしない
            if (commitValues.isEmpty()) {
                return;
            }

            // 保存する
            TextKeyValueStore kvs = new TextKeyValueStore(mContext, mDatabasePath, TextKeyValueStore.TABLE_NAME_DEFAULT);
            try {
                kvs.open(DBOpenType.Write);
                kvs.putInTx(commitValues);

                // コミットが成功したらmodified属性を元に戻す
                {
                    Iterator<Map.Entry<String, Property>> iterator = mPropMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Property property = iterator.next().getValue();
                        property.mModified = false;
                    }
                }
            } finally {
                kvs.close();
            }
        }
    }

    /**
     * 全てのプロパティを最新に保つ
     */
    public void commitAndLoad() {
        synchronized (lock) {
            Map<String, String> commitValues = new HashMap<>();
            TextKeyValueStore kvs = new TextKeyValueStore(mContext, mDatabasePath, TextKeyValueStore.TABLE_NAME_DEFAULT);
            try {
                kvs.open(DBOpenType.Read);

                Iterator<Map.Entry<String, Property>> iterator = mPropMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Property value = iterator.next().getValue();
                    // リロードする。読み込めなかった場合は規定のデフォルト値を持たせる
                    if (value.mModified) {
                        // 変更がある値はDBへ反映リストに追加
                        commitValues.put(value.mKey, value.mValue);
                    } else {
                        // 変更が無いならばDBから読み出す
                        value.mValue = kvs.get(value.mKey, value.mDefaultValue);
                    }
                    // sync直後なのでcommit対象ではない
                    value.mModified = false;
                }

                // 変更を一括更新
                kvs.putInTx(commitValues);
            } finally {
                kvs.close();
            }
        }
    }
}
