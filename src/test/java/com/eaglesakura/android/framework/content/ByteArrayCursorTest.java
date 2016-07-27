package com.eaglesakura.android.framework.content;

import com.eaglesakura.android.framework.UnitTestCase;
import com.eaglesakura.util.EncodeUtil;
import com.eaglesakura.util.RandomUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ByteArrayCursorTest extends UnitTestCase {

    @Test
    public void データを分割して生成できる() throws Throwable {
        byte[] buffer = RandomUtil.randBytes(1024 * 1024 * 1 + 64);

        ByteArrayCursor cursor = new ByteArrayCursor(buffer);
        assertEquals(cursor.mSerializedData.size(), 5);
        assertEquals(cursor.mSerializedData.get(4).length, 64);
    }

    @Test
    public void Cursorから復元したデータが一致する() throws Throwable {
        final byte[] buffer = RandomUtil.randBytes(1024 * 1024 + 64);
        final String ORIGIN_HASH = EncodeUtil.genMD5(buffer);

        byte[] bytes = ByteArrayCursor.toByteArray(new ByteArrayCursor(buffer));
        final String DST_HASH = EncodeUtil.genMD5(bytes);
        assertEquals(buffer.length, bytes.length);
        assertEquals(ORIGIN_HASH, DST_HASH);
    }

    @Test
    public void 空配列を復元できる() throws Throwable {
        final byte[] buffer = new byte[0];
        final String ORIGIN_HASH = EncodeUtil.genMD5(buffer);

        byte[] bytes = ByteArrayCursor.toByteArray(new ByteArrayCursor(buffer));
        final String DST_HASH = EncodeUtil.genMD5(bytes);
        assertEquals(buffer.length, bytes.length);
        assertEquals(ORIGIN_HASH, DST_HASH);
    }
}