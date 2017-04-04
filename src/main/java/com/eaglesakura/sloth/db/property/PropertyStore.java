package com.eaglesakura.sloth.db.property;

/**
 * Textで管理されたプロパティを扱う
 */
public interface PropertyStore {
    /**
     * 文字列のプロパティを取得する
     */
    String getStringProperty(String key);

    /**
     * プロパティを保存する
     *
     * @param key   プロパティのキー値
     * @param value プロパティの値
     */
    void setProperty(String key, String value);

    /**
     * 値を全てデフォルト化する
     */
    void clear();

    /**
     * 値を全て不揮発化する
     */
    void commit();
}
