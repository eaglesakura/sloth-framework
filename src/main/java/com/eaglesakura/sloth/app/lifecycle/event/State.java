package com.eaglesakura.sloth.app.lifecycle.event;


/**
 * Activity/Fragmentのライフサイクル状態を示す
 */
public enum State {
    /**
     * Newされたばかり
     */
    NewObject,
    OnAttach,
    OnCreateView,
    OnCreate,
    OnActivityResult,
    OnStart,
    OnRestoreInstanceState,
    OnResume,
    OnCreateOptionsMenu,
    OnSaveInstanceState,
    OnPause,
    OnStop,
    OnDestroyView,
    OnDetach,
    OnDestroy,
}
