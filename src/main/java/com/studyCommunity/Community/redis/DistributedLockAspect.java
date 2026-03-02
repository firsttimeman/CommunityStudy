package com.studyCommunity.Community.redis;


import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redisson;

    private final ExpressionParser parser = new SpelExpressionParser();// wtf?
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();// wtf?

    @Around("@annotation(lockAnn)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributeLock lockAnn) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String lockName = evaluateKey(lockAnn.key(), method, joinPoint.getArgs());

        RLock lock = lockAnn.fair()
                ? redisson.getFairLock(lockName)
                : redisson.getLock(lockName);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(lockAnn.waitTime(), lockAnn.leaseTime(), lockAnn.timeUnit());
            if (!acquired) {
                // 과부하 시 429로 매핑해도 좋음
                throw new IllegalStateException("LOCK_TIMEOUT: " + lockName);
            }
            return joinPoint.proceed();
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    //todo check해보기
    private String evaluateKey(String spel, Method method, Object[] args) {
        EvaluationContext context = new StandardEvaluationContext();

        String[] paramNames = nameDiscoverer.getParameterNames(method);
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        Object value = parser.parseExpression(spel).getValue(context);
        if (value == null) throw new IllegalArgumentException("Lock key evaluated to null. spel=" + spel);

        // ✅ 락 key prefix 권장 (운영에서 키 충돌 방지)
        return "lock:" + value;

    }

}

