package com.eaglesakura.bundle;

import com.eaglesakura.util.ReflectionUtil;
import com.eaglesakura.util.StringUtil;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BundleCollector {

    private final Object mTarget;

    private final Bundle mState;

    private final Map<Class, Collector> mFreezers;

    private String mTag;

    BundleCollector(Object target, Bundle state, String keyTag, Map<Class, Collector> freezers) {
        mTarget = target;
        mState = state;
        mFreezers = freezers;
        mTag = keyTag;

        if (mTarget == null || mState == null || mFreezers == null) {
            throw new IllegalStateException();
        }
    }

    @NonNull
    String getKey(@NonNull Field field) {
        String result = mTarget.getClass().toString() + "@" + field.getName();

        if (StringUtil.isEmpty(mTag)) {
            return result;
        } else {
            return mTag + "@" + result;
        }
    }

    static boolean instanceOf(Class checkType, Class clazz) {
        try {
            return checkType.asSubclass(clazz) != null;
        } catch (Exception e) {
        }
        return false;
    }

    @NonNull
    Collector getFreezer(@NonNull Field field) {
        Class<?> type = field.getType();
        Collector result = mFreezers.get(type);
        if (result == null) {
            if (instanceOf(type, Parcelable.class)) {
                result = mFreezers.get(Parcelable.class);
            } else if (instanceOf(type, Serializable.class)) {
                result = mFreezers.get(Serializable.class);
            }
        }

        if (result == null) {
            throw new IllegalStateException("Field : " + field.getName());
        }

        return result;
    }

    /**
     * ステートの保存を行う
     */
    void save() {
        List<Field> fields = ReflectionUtil.listAnnotationFields(mTarget.getClass(), BundleState.class);
        for (Field field : fields) {
            field.setAccessible(true);
            String key = getKey(field);
            Collector freezer = getFreezer(field);
            if (freezer == null) {
                throw new IllegalStateException("freeze failed :: " + key);
            }

            try {
                freezer.onSaveInstance(mState, key, mTarget, field);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /***
     * ステートのレストアを行う
     */
    void restore() {
        List<Field> fields = ReflectionUtil.listAnnotationFields(mTarget.getClass(), BundleState.class);
        for (Field field : fields) {
            field.setAccessible(true);
            String key = getKey(field);
            Collector freezer = getFreezer(field);
            if (freezer == null) {
                throw new IllegalStateException("freeze failed :: " + key);
            }

            try {
                freezer.onRestoreInstance(mState, key, mTarget, field);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static Builder create(Bundle state) {
        return new Builder().state(state);
    }

    private static Map<Class, Collector> sDefaultFreezer;

    private static Map<Class, Collector> getDefaultFreezer() {
        if (sDefaultFreezer == null) {
            synchronized (BundleCollector.class) {
                if (sDefaultFreezer == null) {
                    sDefaultFreezer = new HashMap<>();

                    {
                        BundlePrimitiveCollector primitiveFreezer = new BundlePrimitiveCollector();
                        sDefaultFreezer.put(boolean.class, primitiveFreezer);
                        sDefaultFreezer.put(byte.class, primitiveFreezer);
                        sDefaultFreezer.put(short.class, primitiveFreezer);
                        sDefaultFreezer.put(int.class, primitiveFreezer);
                        sDefaultFreezer.put(long.class, primitiveFreezer);
                        sDefaultFreezer.put(float.class, primitiveFreezer);
                        sDefaultFreezer.put(double.class, primitiveFreezer);
                        sDefaultFreezer.put(String.class, primitiveFreezer);
                        sDefaultFreezer.put(Serializable.class, primitiveFreezer);
                        sDefaultFreezer.put(Parcelable.class, primitiveFreezer);
                    }
                    {
                        BundleArrayCollector arrayFreezer = new BundleArrayCollector();
                        sDefaultFreezer.put(boolean[].class, arrayFreezer);
                        sDefaultFreezer.put(byte[].class, arrayFreezer);
                        sDefaultFreezer.put(short[].class, arrayFreezer);
                        sDefaultFreezer.put(int[].class, arrayFreezer);
                        sDefaultFreezer.put(long[].class, arrayFreezer);
                        sDefaultFreezer.put(float[].class, arrayFreezer);
                        sDefaultFreezer.put(double[].class, arrayFreezer);
                        sDefaultFreezer.put(String[].class, arrayFreezer);
                        sDefaultFreezer.put(Parcelable[].class, arrayFreezer);
                        sDefaultFreezer.put(ArrayList.class, arrayFreezer);
                    }
                }
            }
        }
        return sDefaultFreezer;
    }

    public static class Builder {
        private Object mTargetObject;

        private Bundle mState;

        private Map<Class, Collector> mFreezerMap = new HashMap<>(getDefaultFreezer());

        private String mTag;

        Builder() {
            // デフォルトのフリーザを指定
        }

        private Builder state(Bundle bundle) {
            mState = bundle;
            return this;
        }

        public Builder target(Object target) {
            mTargetObject = target;
            if (target instanceof Fragment) {
                tag(((Fragment) target).getTag());
            }
            return this;
        }

        public Builder freezer(Class clazz, Collector freezer) {
            mFreezerMap.put(clazz, freezer);
            return this;
        }

        /**
         * 同じClassが複数ある場合、タグを指定することで保存・復旧時に競合が発生しないようにする
         */
        public Builder tag(String keyTag) {
            mTag = keyTag;
            return this;
        }

        public Builder save() {
            new BundleCollector(mTargetObject, mState, mTag, mFreezerMap).save();
            return this;
        }

        /**
         * restoreを行う。
         * Bundleがnullである場合は何もしない
         */
        public Builder restore() {
            if (mState != null) {
                new BundleCollector(mTargetObject, mState, mTag, mFreezerMap).restore();
            }
            return this;
        }
    }
}