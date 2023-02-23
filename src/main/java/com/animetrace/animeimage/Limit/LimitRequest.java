package com.animetrace.animeimage.Limit;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

public @interface LimitRequest {
    long time() default 6000;
    int count() default 1;
    String content() default "访问频繁，请稍后重试";

    int status() default 1022;
    //mode 1是页面返回，0是API返回
    int mode() default 0;
}
