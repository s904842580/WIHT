package com.waht.platform.audit;

import java.lang.annotation.*;

/** Explicit allowlist: never capture request bodies, query strings, tokens or response text. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    String module();
    String action();
    String resource() default "";
}
