package com.eaglesakura.android.framework.content;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Cursorに乗せて送信する
 *
 * ApplicationDataProvider専用のCursorなので、最低限の実装しかされていない。
 */
public class ByteArrayCursor implements Cursor {
    List<byte[]> mSerializedData = new ArrayList<>();

    int mCursorIndex;

    public static final String COLUMN_NAME_VALUE_FRAGMENT = "values";

    public ByteArrayCursor(byte[] buffer) {

        if (buffer.length == 0) {
            // 空配列を挿入する
            mSerializedData.add(new byte[0]);
        } else {
            int offset = 0;
            int length = buffer.length;
            while (length > 0) {
                int range = Math.min(1024 * 256, length); // 適当な大きさに分解する
                mSerializedData.add(Arrays.copyOfRange(buffer, offset, offset + range));

                offset += range;
                length -= range;
            }
        }
    }


    @Override
    public int getCount() {
        return mSerializedData.size();
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{
                COLUMN_NAME_VALUE_FRAGMENT
        };
    }

    @Override
    public int getPosition() {
        return mCursorIndex;
    }

    @Override
    public boolean move(int pos) {
        if (pos >= 0 && pos < mSerializedData.size()) {
            mCursorIndex = pos;
            return true;
        }
        return false;
    }

    @Override
    public boolean moveToPosition(int pos) {
        if (pos >= 0 && pos < mSerializedData.size()) {
            mCursorIndex = pos;
            return true;
        }
        return false;
    }

    @Override
    public boolean moveToFirst() {
        mCursorIndex = 0;
        return true;
    }

    @Override
    public boolean moveToLast() {
        mCursorIndex = mSerializedData.size() - 1;
        return true;
    }

    @Override
    public boolean moveToNext() {
        if (!isLast()) {
            ++mCursorIndex;
            return true;
        }
        return false;
    }

    @Override
    public boolean moveToPrevious() {
        if (mCursorIndex > 0) {
            --mCursorIndex;
            return true;
        }
        return false;
    }

    @Override
    public boolean isFirst() {
        return mCursorIndex == 0;
    }

    @Override
    public boolean isLast() {
        return mCursorIndex == (mSerializedData.size() - 1);
    }

    @Override
    public boolean isBeforeFirst() {
        return false;
    }

    @Override
    public boolean isAfterLast() {
        return mCursorIndex >= mSerializedData.size();
    }

    @Override
    public int getColumnIndex(String s) {
        return 0;
    }

    @Override
    public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public String getColumnName(int i) {
        return null;
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public byte[] getBlob(int column) {
        return mSerializedData.get(mCursorIndex);
    }

    @Override
    public String getString(int column) {
        return null;
    }

    @Override
    public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {

    }

    @Override
    public short getShort(int i) {
        return 0;
    }

    @Override
    public int getInt(int i) {
        return 0;
    }

    @Override
    public long getLong(int i) {
        return 0;
    }

    @Override
    public float getFloat(int i) {
        return 0;
    }

    @Override
    public double getDouble(int i) {
        return 0;
    }

    @Override
    public int getType(int i) {
        return 0;
    }

    @Override
    public boolean isNull(int i) {
        return false;
    }

    @Override
    public void deactivate() {

    }

    @Override
    public boolean requery() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void registerContentObserver(ContentObserver contentObserver) {

    }

    @Override
    public void unregisterContentObserver(ContentObserver contentObserver) {

    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void setNotificationUri(ContentResolver contentResolver, Uri uri) {

    }

    @Override
    public Uri getNotificationUri() {
        return null;
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        return false;
    }

    @Override
    public void setExtras(Bundle bundle) {

    }

    @Override
    public Bundle getExtras() {
        return null;
    }

    @Override
    public Bundle respond(Bundle bundle) {
        return null;
    }

    /**
     * 分割して送られてきたデータを連結する
     *
     * @param cursor 連結するカーソル
     * @return 連結されたデータ
     */
    public static byte[] toByteArray(Cursor cursor) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        if (!cursor.moveToFirst()) {
            throw new IllegalStateException();
        }

        try {
            do {
                os.write(cursor.getBlob(0));
            } while (cursor.moveToNext());

            return os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
