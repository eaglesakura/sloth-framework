package com.eaglesakura.sloth.context;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * バージョンアップ情報
 */
public class VersionContext {
    private String mOldVersionName;

    private int mOldVersionCode;

    private String mVersionName;

    private int mVersionCode;

    public VersionContext(String oldVersionName, int oldVersionCode, String versionName, int vesionCode) {
        mOldVersionName = oldVersionName;
        mOldVersionCode = oldVersionCode;
        mVersionName = versionName;
        mVersionCode = vesionCode;
    }

    /**
     * 前回起動時のバージョン名を取得する
     */
    @Nullable
    public String getOldVersionName() {
        return mOldVersionName;
    }

    /**
     * 前回起動時のバージョンコードを取得する
     */
    public int getOldVersionCode() {
        return mOldVersionCode;
    }

    /**
     * 現在のバージョン名を取得する
     */
    @NonNull
    public String getVersionName() {
        return mVersionName;
    }

    /**
     * 現在のバージョンコードを取得する
     */
    public int getVersionCode() {
        return mVersionCode;
    }

    /**
     * 前回起動からバージョンアップされている場合true
     */
    public boolean isVersionUpdated() {
        return mOldVersionCode != mVersionCode;
    }
}
