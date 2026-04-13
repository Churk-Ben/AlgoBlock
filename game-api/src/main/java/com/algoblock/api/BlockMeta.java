package com.algoblock.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BlockMeta {
    String name();

    String signature() default "?"; // 例如：(Int, Int) -> Int

    String description() default ""; // 例如：将两个整数相加

    int arity() default 1;
}
