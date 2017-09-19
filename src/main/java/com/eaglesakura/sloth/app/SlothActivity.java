package com.eaglesakura.sloth.app;

import com.eaglesakura.cerberus.PendingCallbackQueue;
import com.eaglesakura.sloth.app.lifecycle.ActivityLifecycle;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

/**
 * ライフサイクル系のサポートを行うActivity
 */
public abstract class SlothActivity extends AppCompatActivity {
    private ActivityLifecycle mLifecycle;

    public ActivityLifecycle getActivityLifecycle() {
        if (mLifecycle == null) {
            synchronized (this) {
                if (mLifecycle == null) {
                    mLifecycle = new ActivityLifecycle();
                    onCreateLifecycle(mLifecycle);
                }
            }
        }
        return mLifecycle;
    }

    /**
     * ライフサイクルが新規生成された
     */
    protected void onCreateLifecycle(@NonNull ActivityLifecycle lifecycle) {
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        getActivityLifecycle().onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityLifecycle().onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getActivityLifecycle().onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getActivityLifecycle().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getActivityLifecycle().onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getActivityLifecycle().onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getActivityLifecycle().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getActivityLifecycle().onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivityLifecycle().onActivityResult(requestCode, resultCode, data);
    }

    public PendingCallbackQueue getCallbackQueue() {
        return getActivityLifecycle().getCallbackQueue();
    }
}
