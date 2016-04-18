package com.eaglesakura.android.playservice;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import com.eaglesakura.android.framework.FwLog;
import com.eaglesakura.android.framework.ui.SupportFragment;
import com.eaglesakura.android.framework.ui.delegate.SupportFragmentDelegate;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;

public class GoogleApiFragment extends SupportFragment {
    protected GoogleApiClientToken googleApiClientToken;

    Callback callback;

    protected static final int REQUEST_GOOGLEPLAYSERVICE_RECOVER = 0x1100;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Callback) {
            this.callback = (Callback) context;
        } else if (getParentFragment() instanceof Callback) {
            this.callback = (Callback) getParentFragment();
        } else {
            throw new IllegalStateException("Callback not impl!!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleApiClientToken = callback.newClientToken(this);

        final int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (statusCode != ConnectionResult.SUCCESS) {
            showGoogleErrorDialog(statusCode);
        } else {
            FwLog.google("Google Play Service OK!");
        }
    }

    @Override
    public void onAfterViews(SupportFragmentDelegate self, int flags) {

    }

    @Override
    public void onAfterBindMenu(SupportFragmentDelegate self, Menu menu) {

    }

    @Override
    public void onAfterInjection(SupportFragmentDelegate self) {

    }

    /**
     * エラーダイアログを表示する
     */
    protected void showGoogleErrorDialog(final int statusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode, getActivity(), REQUEST_GOOGLEPLAYSERVICE_RECOVER, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                callback.onGooglePlayServiceRecoverCanceled(GoogleApiFragment.this, statusCode);
            }
        });
        dialog.show();
    }

    public GoogleApiClientToken getGoogleApiClientToken() {
        return googleApiClientToken;
    }

    public interface Callback {
        GoogleApiClientToken newClientToken(GoogleApiFragment self);

        void onGooglePlayServiceRecoverCanceled(GoogleApiFragment self, int statusCode);
    }
}
