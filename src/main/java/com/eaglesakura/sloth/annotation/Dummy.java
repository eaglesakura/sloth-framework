package com.eaglesakura.sloth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 型付け等、言語機能的なサポートのために設定されているフィールド
 *
 * 将来的に削除やrenameを行う可能性が高い
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Dummy {
}
