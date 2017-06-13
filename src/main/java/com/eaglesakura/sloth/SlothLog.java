package com.eaglesakura.sloth;

import com.eaglesakura.log.Logger;
import com.eaglesakura.util.EnvironmentUtil;
import com.eaglesakura.util.StringUtil;

import android.util.Log;

public class SlothLog {
    private static final Logger.Impl sAppLogger;

    static {
        if (EnvironmentUtil.isRunningRobolectric()) {
            sAppLogger = new Logger.RobolectricLogger() {
                @Override
                protected int getStackDepth() {
                    return super.getStackDepth();
                }
            };
        } else {
            sAppLogger = new Logger.AndroidLogger(Log.class) {
                @Override
                protected int getStackDepth() {
                    return super.getStackDepth();
                }
            }.setStackInfo(BuildConfig.DEBUG);
        }
    }

    public static void widget(String fmt, Object... args) {
        String tag = "Fw.Widget";
        sAppLogger.out(Logger.LEVEL_DEBUG, tag, StringUtil.format(fmt, args));
    }

    public static void system(String fmt, Object... args) {
        String tag = "Fw.System";
        sAppLogger.out(Logger.LEVEL_DEBUG, tag, StringUtil.format(fmt, args));
    }


    public static void image(String fmt, Object... args) {
        String tag = "Fw.Image";
        sAppLogger.out(Logger.LEVEL_DEBUG, tag, StringUtil.format(fmt, args));
    }

    public static void debug(String fmt, Object... args) {
        String tag = "Fw.Debug";
        sAppLogger.out(Logger.LEVEL_DEBUG, tag, StringUtil.format(fmt, args));
    }

    public static void cursor(String fmt, Object... args) {
//        if (true) {
//            return;
//        }
//        String tag = "Fw.DB.Cursor";
//        sAppLogger.out(Logger.LEVEL_DEBUG, tag, StringUtil.format(fmt, args));
    }

    public static void google(String fmt, Object... args) {
        String tag = "Fw.Google";
        sAppLogger.out(Logger.LEVEL_DEBUG, tag, StringUtil.format(fmt, args));
    }
}
