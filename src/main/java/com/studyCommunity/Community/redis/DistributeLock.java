package com.studyCommunity.Community.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeLock {
    String key();
    long waitTime() default 300;
    long leaseTime() default 3;
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    boolean fair() default false; // fifo 쓸거냐 아니면 빠른 스레드가 먼저 처리할거냐?
}
