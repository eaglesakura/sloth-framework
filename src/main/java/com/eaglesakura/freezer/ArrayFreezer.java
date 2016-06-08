package com.eaglesakura.freezer;

import com.eaglesakura.util.ReflectionUtil;

import android.os.Bundle;
import android.os.Parcelable;

import java.lang.reflect.Field;
import java.util.ArrayList;

class ArrayFreezer implements Freezer {
    @Override
    public void onSaveInstance(Bundle state, String key, Object srcObject, Field srcField) throws Throwable {
        Object value = srcField.get(srcObject);

        if (value instanceof boolean[]) {
            state.putBooleanArray(key, (boolean[]) value);
        } else if (value instanceof byte[]) {
            state.putByteArray(key, (byte[]) value);
        } else if (value instanceof short[]) {
            state.putShortArray(key, (short[]) value);
        } else if (value instanceof int[]) {
            state.putIntArray(key, (int[]) value);
        } else if (value instanceof long[]) {
            state.putLongArray(key, (long[]) value);
        } else if (value instanceof float[]) {
            state.putFloatArray(key, (float[]) value);
        } else if (value instanceof double[]) {
            state.putDoubleArray(key, (double[]) value);
        } else if (value instanceof String[]) {
            state.putStringArray(key, (String[]) value);
        } else if (value instanceof Parcelable[]) {
            state.putParcelableArray(key, (Parcelable[]) value);
        } else if (value instanceof ArrayList) {
            Class genericClass = ReflectionUtil.getListGenericClass(srcField);
            if (String.class.equals(genericClass)) {
                state.putStringArrayList(key, (ArrayList<String>) value);
            } else if (Integer.class.equals(genericClass)) {
                state.putIntegerArrayList(key, (ArrayList<Integer>) value);
            } else if (Parcelable.class.equals(genericClass)) {
                state.putParcelableArrayList(key, (ArrayList<Parcelable>) value);
            } else {
                throw new IllegalStateException("key:" + key);
            }
        } else {
            throw new IllegalStateException("key:" + key);
        }
    }

    @Override
    public void onRestoreInstance(Bundle state, String key, Object dstObject, Field dstField) throws Throwable {
        Class<?> type = dstField.getType();
        Object value;

        if (boolean[].class.equals(type)) {
            value = state.getBooleanArray(key);
        } else if (byte[].class.equals(type)) {
            value = state.getByteArray(key);
        } else if (short[].class.equals(type)) {
            value = state.getShortArray(key);
        } else if (int[].class.equals(type)) {
            value = state.getIntArray(key);
        } else if (long[].class.equals(type)) {
            value = state.getLongArray(key);
        } else if (float[].class.equals(type)) {
            value = state.getFloatArray(key);
        } else if (double[].class.equals(type)) {
            value = state.getDoubleArray(key);
        } else if (String[].class.equals(type)) {
            value = state.getStringArray(key);
        } else if (Parcelable[].class.equals(type)) {
            value = state.getParcelableArray(key);
        } else if (ArrayList.class.equals(type)) {
            Class genericClass = ReflectionUtil.getListGenericClass(dstField);
            if (String.class.equals(genericClass)) {
                value = state.getStringArrayList(key);
            } else if (Integer.class.equals(genericClass)) {
                value = state.getIntegerArrayList(key);
            } else if (Parcelable.class.equals(genericClass)) {
                value = state.getParcelableArrayList(key);
            } else {
                throw new IllegalStateException("key:" + key);
            }
        } else {
            throw new IllegalStateException("key:" + key);
        }

        dstField.set(dstObject, value);
    }
}
