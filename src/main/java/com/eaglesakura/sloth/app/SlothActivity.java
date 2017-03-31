package com.eaglesakura.sloth.app;

import com.eaglesakura.cerberus.LifecycleState;
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

    public ActivityLifecycle getLifecycle() {
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

    /**
     * ライフサイクル状態を取得する
     */
    public LifecycleState getLifecycleState() {
        return mLifecycle.getLifecycleState();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        getLifecycle().onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getLifecycle().onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLifecycle().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getLifecycle().onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getLifecycle().onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getLifecycle().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLifecycle().onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getLifecycle().onActivityResult(requestCode, resultCode, data);
    }

    public PendingCallbackQueue getCallbackQueue() {
        return getLifecycle().getCallbackQueue();
    }
}
