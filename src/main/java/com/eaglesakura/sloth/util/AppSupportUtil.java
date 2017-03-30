package com.eaglesakura.sloth.util;

import com.eaglesakura.android.property.model.PropertySource;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.cerberus.error.TaskCanceledException;
import com.eaglesakura.json.JSON;
import com.eaglesakura.lambda.CallbackUtils;
import com.eaglesakura.lambda.CancelCallback;
import com.eaglesakura.sloth.ui.message.LocalMessageReceiver;
import com.eaglesakura.util.CollectionUtil;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RawRes;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * アプリ開発でよく用いるUtil
 */
public class AppSupportUtil {

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean requestRuntimePermissions(Activity activity, String[] permissions) {
        if (!PermissionUtil.isRuntimePermissionGranted(activity, permissions)) {
            activity.requestPermissions(permissions, LocalMessageReceiver.REQUEST_RUNTIMEPERMISSION_UPDATE);
            return true;
        } else {
            return false;
        }
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LocalMessageReceiver.REQUEST_RUNTIMEPERMISSION_UPDATE) {
            Intent intent = new Intent();
            intent.setAction(LocalMessageReceiver.ACTION_RUNTIMEPERMISSION_UPDATE);
            List<String> granted = new ArrayList<>();
            List<String> denied = new ArrayList<>();

            for (int i = 0; i < permissions.length; ++i) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted.add(permissions[i]);
                } else {
                    denied.add(permissions[i]);
                }
            }

            intent.putExtra(LocalMessageReceiver.EXTRA_RUNTIMEPERMISSION_GRANTED_LIST, CollectionUtil.asArray(granted, new String[granted.size()]));
            intent.putExtra(LocalMessageReceiver.EXTRA_RUNTIMEPERMISSION_DENIED_LIST, CollectionUtil.asArray(denied, new String[denied.size()]));

            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }
    }

    public static <T extends View> T findViewByChildAt(View root, int... indices) {
        for (int index : indices) {
            root = ((ViewGroup) root).getChildAt(index);
        }

        return (T) root;
    }

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
