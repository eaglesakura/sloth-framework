package com.eaglesakura.android.framework;

import com.eaglesakura.log.Logger;
import com.eaglesakura.util.EnvironmentUtil;

import android.util.Log;

public class FwLog {
    private static final Logger.Impl sAppLogger;

    static {
        if (EnvironmentUtil.isRunningRobolectric()) {
            sAppLogger = new Logger.RobolectricLogger() {
                @Override
                protected int getStackDepth() {
                    return super.getStackDepth() + 1;
                }
            };
        } else {
            sAppLogger = new Logger.AndroidLogger(Log.class) {
                @Override
                protected int getStackDepth() {
                    return super.getStackDepth() + 1;
                }
            }.setStackInfo(BuildConfig.DEBUG);
        }
    }

    public static void widget(String fmt, Object... args) {
        String tag = "Fw.Widget";
        Logger.out(Logger.LEVEL_DEBUG, tag, fmt, args);
    }

    public static void system(String fmt, Object... args) {
        String tag = "Fw.System";
        Logger.out(Logger.LEVEL_DEBUG, tag, fmt, args);
    }


    public static void image(String fmt, Object... args) {
        String tag = "Fw.Image";
        Logger.out(Logger.LEVEL_DEBUG, tag, fmt, args);
    }

    public static void debug(String fmt, Object... args) {
        String tag = "Fw.Debug";
        Logger.out(Logger.LEVEL_DEBUG, tag, fmt, args);
    }

    public static void cursor(String fmt, Object... args) {
        if (true) {
            return;
        }
        String tag = "Fw.DB.Cursor";
        Logger.out(Logger.LEVEL_DEBUG, tag, fmt, args);
    }

    public static void google(String fmt, Object... args) {
        String tag = "Fw.Google";
        Logger.out(Logger.LEVEL_DEBUG, tag, fmt, args);
    }
}
