package com.eaglesakura.sloth.app.lifecycle.event;

import android.os.Bundle;

/**
 * 現在のライフサイクルイベントを示す
 *
 * これは {@link android.app.Activity#onCreate(Bundle)} のメソッド引数も含めてイベントとして渡してハンドリングを行う。
 */
public interface LifecycleEvent {
    State getState();

    static LifecycleEvent wrap(State state) {
        return () -> state;
    }
}
