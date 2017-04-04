package com.eaglesakura.sloth.util;

import com.eaglesakura.sloth.SlothLog;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.Method;

/**
 * Created by eaglesakura on 2017/03/30.
 */

public class DebugUtil {

    /**
     * Deploygateのインストールを行う。
     * <br>
     * dependenciesが設定されていない場合、このメソッドはfalseを返す
     * <br>
     * debugCompile 'com.deploygate:sdk:3.1'
     *
     * @return 成功したらtrue
     */
    public static boolean requestDeploygateInstall(Context context) {
        return requestDeploygateInstall(context, true);
    }

    /**
     * Deploygateのインストールを行う。
     * <br>
     * dependenciesが設定されていない場合、このメソッドはfalseを返す
     * <br>
     * debugCompile 'com.deploygate:sdk:3.1'
     *
     * @return 成功したらtrue
     */
    public static boolean requestDeploygateInstall(Context context, boolean forceApply) {
        try {
            Class<?> DeployGateCallback = Class.forName("com.deploygate.sdk.DeployGateCallback");
            Class<?> DeployGate = Class.forName("com.deploygate.sdk.DeployGate");

            Method installMethod = DeployGate.getMethod("install", Application.class, DeployGateCallback, boolean.class);

            installMethod.invoke(DeployGate, context, null, forceApply);
            SlothLog.system("install success Deploygate");
            return true;
        } catch (Exception e) {
            SlothLog.system("not dependencies Deploygate");
            return false;
        }
    }

}
