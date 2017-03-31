package com.eaglesakura.sloth.util;

import com.eaglesakura.cerberus.error.TaskCanceledException;
import com.eaglesakura.json.JSON;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.database.property.model.PropertySource;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.RawRes;

import java.io.IOException;
import java.io.InputStream;

/**
 * アプリ開発でよく用いるUtil
 */
public class AppSupportUtil {

    /**
     * Prop Sourceを読みだす
     */
    @SuppressLint("NewApi")
    public static PropertySource loadPropertySource(Context context, @RawRes int resId) {
        try (InputStream is = context.getResources().openRawResource(resId)) {
            return JSON.decode(is, PropertySource.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void assertNotCanceled(CancelCallback cancelCallback) throws TaskCanceledException {
        if (CallbackUtils.isCanceled(cancelCallback)) {
            throw new TaskCanceledException();
        }
    }

    /**
     * オブジェクトをJSONを介してBundleに登録する
     *
     * @param dst 保存先Bundle
     * @param key 保存Key
     * @param obj 保存するオブジェクト
     */
    public static void putBundle(Bundle dst, String key, Object obj) {
        dst.putString(key, JSON.encodeOrNull(obj));
    }

    /**
     * オブジェクトをJSONを介してIntentに登録する
     *
     * @param dst 保存先Bundle
     * @param key 保存Key
     * @param obj 保存するオブジェクト
     */
    public static void putExtra(Intent dst, String key, Object obj) {
        dst.putExtra(key, JSON.encodeOrNull(obj));
    }

    /**
     * Bundle/JSONからオブジェクトを復元する
     *
     * @param src   保存されたオブジェクト
     * @param key   保存されたキー
     * @param clazz パースするクラス
     */
    public static <T> T getBundle(Bundle src, String key, Class<T> clazz) {
        return JSON.decodeOrNull(src.getString(key), clazz);
    }

    /**
     * Intent/JSONからオブジェクトを復元する
     *
     * @param src   保存されたオブジェクト
     * @param key   保存されたキー
     * @param clazz パースするクラス
     */
    public static <T> T getExtra(Intent src, String key, Class<T> clazz) {
        return JSON.decodeOrNull(src.getStringExtra(key), clazz);
    }
}
