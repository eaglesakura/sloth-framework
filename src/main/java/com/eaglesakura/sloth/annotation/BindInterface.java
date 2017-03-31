package com.eaglesakura.sloth.annotation;

import android.support.annotation.Keep;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fragment Treeから指定インターフェースを検索し、オブジェクトとして登録する。
 */
@Keep
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface BindInterface {

    /**
     * nullを許容する場合true
     */
    boolean nullable() default false;
}