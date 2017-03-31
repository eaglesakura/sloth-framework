package com.eaglesakura.sloth.ui.support.annotation;

import android.support.annotation.Keep;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fragmentからアクセス可能なFragmentManagerツリーの中にインターフェースを見つける
 */
@Keep
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface BindInterface {
    /**
     * 見つからないことを許容する
     */
    boolean nullable() default false;
}
