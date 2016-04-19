package com.eaglesakura.android.framework.delegate.service;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * CPUのウェイクアップ制御を行う
 */
public class CpuWakeDelegate {
    public interface CpuWakeDelegateCompat {
        Context getContext(@NonNull CpuWakeDelegate self);
    }
}
