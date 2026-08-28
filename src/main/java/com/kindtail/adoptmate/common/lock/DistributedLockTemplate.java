package com.kindtail.adoptmate.common.lock;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockTemplate {

    private static final String LOCK_PREFIX = "LOCK:";
    private static final long DEFAULT_WAIT_TIME = 5L;
    private static final long DEFAULT_LEASE_TIME = 3L;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    private final RedissonClient redissonClient;

    public <T> T execute(String key, Supplier<T> task) {
        return execute(key, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT, task);
    }

    public void executeWithoutResult(String key, Runnable task) {
        execute(key, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME, DEFAULT_TIME_UNIT, () -> {
            task.run();
            return null;
        });
    }

    public void executeWithoutResult(String key, long waitTime, long leaseTime, TimeUnit timeUnit, Runnable task) {
        execute(key, waitTime, leaseTime, timeUnit, () -> {
            task.run();
            return null;
        });
    }

    public <T> T execute(String key, long waitTime, long leaseTime, TimeUnit timeUnit, Supplier<T> task) {
        String lockKey = LOCK_PREFIX + key;
        RLock rLock = redissonClient.getLock(lockKey);

        try {
            boolean available = rLock.tryLock(waitTime, leaseTime, timeUnit);
            if (!available) {
                log.warn("[DistributedLock] Failed to acquire lock for key: {}", lockKey);
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            log.info("[DistributedLock] Successfully acquired lock for key: {}", lockKey);
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[DistributedLock] Thread interrupted while waiting for lock key: {}", lockKey, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            try {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                    log.info("[DistributedLock] Successfully released lock for key: {}", lockKey);
                }
            } catch (IllegalMonitorStateException e) {
                log.warn("[DistributedLock] Lock already un-held or expired for key: {}", lockKey, e);
            }
        }
    }
}
