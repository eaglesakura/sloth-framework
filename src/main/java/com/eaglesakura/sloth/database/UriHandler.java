package com.eaglesakura.sloth.database;

import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Providerの特定URIに反応するProvider
 */
public interface UriHandler {
    /**
     * 指定されたURIがサポートされていたらtrue
     */
    boolean isSupport(Uri uri);

    /**
     * get操作を行う
     *
     * @param uri      対象URI
     * @param command  対象コマンド
     * @param argments 引数リスト
     * @return Cursorとして返すデータ
     */
    @Nullable
    byte[] query(@NonNull Uri uri, @NonNull String command, @NonNull String[] argments);

    /**
     * 挿入操作を行う
     *
     * @param uri     対象URI
     * @param command 対象コマンド
     * @param values  送信されてきたデータ
     */
    void insert(@NonNull Uri uri, @NonNull String command, @NonNull ContentValues values);

    /**
     * ContentProviderがシャットダウンされる
     */
    void onShutdown();
}
