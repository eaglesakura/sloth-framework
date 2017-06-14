package com.eaglesakura.sloth.persistence;

import com.eaglesakura.util.ThrowableRunnable;
import com.eaglesakura.util.ThrowableRunner;

import android.arch.persistence.room.RoomDatabase;

import java.io.Closeable;

/**
 * 設計上の注意点：
 * {@link android.arch.persistence.room.Query} に対して "AS"文を含めるとビルドが終了しない不具合がある(alpha1)
 */
public abstract class SlothRoomDatabase extends RoomDatabase {

    /**
     * try-with-resourceで使用するためのClosableを生成する
     * 実際のOpenは自動的に行われるが、try(Closable token = db.open()){...} とすることでcloseされることが明示されるめ。
     */
    public Closeable open() {
        return () -> this.close();
    }

    /**
     * 戻り値と例外を許容してトランザクション実行を行う
     */
    public <RetType, ErrType extends Exception> RetType runInTx(ThrowableRunnable<RetType, ErrType> runnable) throws ErrType {
        ThrowableRunner<RetType, ErrType> runner = new ThrowableRunner<>(runnable);
        try {
            beginTransaction();
            runner.run();
            RetType result = runner.getOrThrow();
            setTransactionSuccessful();
            return result;
        } finally {
            endTransaction();
        }
    }

}
