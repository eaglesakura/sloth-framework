package com.eaglesakura.android.framework.content;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.util.StringUtil;

import android.database.AbstractCursor;
import android.database.Cursor;

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
public class ByteArrayCursor extends AbstractCursor {
    List<byte[]> mSerializedData = new ArrayList<>();

    int mCursorIndex = -1;

    public static final String COLUMN_NAME_VALUE_FRAGMENT = "values";

    public ByteArrayCursor(byte[] buffer) {
        FwLog.cursor("ByteArrayCursor[%d bytes]", buffer.length);

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
        FwLog.cursor("ByteArrayCursor.getCount[%d]", mSerializedData.size());
        return mSerializedData.size();
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{
                COLUMN_NAME_VALUE_FRAGMENT
        };
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        FwLog.cursor("ByteArrayCursor.onMove old[%d] new[%d]", oldPosition, newPosition);
        mCursorIndex = newPosition;
        return super.onMove(oldPosition, newPosition);
    }

    @Override
    public byte[] getBlob(int column) {
        FwLog.cursor("ByteArrayCursor.getString column[%d], %d bytes", column, mSerializedData.get(mCursorIndex).length);
        return null;
    }

    @Override
    public String getString(int column) {
//        FwLog.system("ByteArrayCursor.getString column[%d], %d bytes", column, mSerializedData.get(mCursorIndex).length);
        return StringUtil.toString(mSerializedData.get(mCursorIndex));
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
    public boolean isNull(int i) {
        return false;
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
//
        try {
            do {
                byte[] blob = StringUtil.toByteArray(cursor.getString(0));
                os.write(blob);
            } while (cursor.moveToNext());

            return os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
