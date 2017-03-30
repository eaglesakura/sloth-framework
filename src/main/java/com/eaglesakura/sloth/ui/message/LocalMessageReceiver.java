package com.eaglesakura.sloth.ui.message;

import com.eaglesakura.sloth.FwLog;
import com.eaglesakura.util.StringUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class LocalMessageReceiver extends BroadcastReceiver {
    public static final int REQUEST_RUNTIMEPERMISSION_UPDATE = 0x1100 + 1;

    public static final String ACTION_RUNTIMEPERMISSION_UPDATE = "ACTION_RUNTIMEPERMISSION_UPDATE";

    public static final String EXTRA_RUNTIMEPERMISSION_GRANTED_LIST = "EXTRA_RUNTIMEPERMISSION_GRANTED_LIST";

    public static final String EXTRA_RUNTIMEPERMISSION_DENIED_LIST = "EXTRA_RUNTIMEPERMISSION_DENIED_LIST";

    final LocalBroadcastManager localBroadcastManager;

    public LocalMessageReceiver(Context context) {
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @Override
    final public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (StringUtil.isEmpty(action)) {
            return;
        }

        if (ACTION_RUNTIMEPERMISSION_UPDATE.equals(action)) {
            String[] granted = intent.getStringArrayExtra(EXTRA_RUNTIMEPERMISSION_GRANTED_LIST);
            String[] denied = intent.getStringArrayExtra(EXTRA_RUNTIMEPERMISSION_DENIED_LIST);

            if (granted == null) {
                granted = new String[0];
            }
            if (denied == null) {
                denied = new String[0];
            }

            for (String g : granted) {
                FwLog.system("RuntimePermission Granted / " + g);
            }

            for (String d : denied) {
                FwLog.system("RuntimePermission Denied / " + d);
            }

            onRuntimePermissionUpdated(granted, denied);
        }
    }

    public void connect() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RUNTIMEPERMISSION_UPDATE);
        localBroadcastManager.registerReceiver(this, filter);
    }

    public void disconnect() {
        localBroadcastManager.unregisterReceiver(this);
    }

    protected void onRuntimePermissionUpdated(String[] granted, String[] denied) {
    }
}
