package com.kindtail.adoptmate.common.lock;

import com.kindtail.adoptmate.common.exception.CustomException;
import com.kindtail.adoptmate.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributedLockTemplateTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @InjectMocks
    private DistributedLockTemplate distributedLockTemplate;

    @Test
    @DisplayName("락 획득 성공 시 주어진 작업을 정상 수행하고 락을 반납한다")
    void execute_Success() throws InterruptedException {
        // given
        String key = "testKey";
        given(redissonClient.getLock("LOCK:" + key)).willReturn(rLock);
        given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
        given(rLock.isHeldByCurrentThread()).willReturn(true);

        // when
        String result = distributedLockTemplate.execute(key, () -> "success");

        // then
        assertThat(result).isEqualTo("success");
        verify(rLock).unlock();
    }

    @Test
    @DisplayName("락 획득 실패 시 LOCK_ACQUISITION_FAILED 예외를 던진다")
    void execute_LockAcquisitionFailed() throws InterruptedException {
        // given
        String key = "testKey";
        given(redissonClient.getLock("LOCK:" + key)).willReturn(rLock);
        given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> distributedLockTemplate.execute(key, () -> "fail"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOCK_ACQUISITION_FAILED);

        verify(rLock, never()).unlock();
    }

    @Test
    @DisplayName("Runnable 작업도 정상 수행된다")
    void executeWithoutResult_Success() throws InterruptedException {
        // given
        String key = "testKey";
        given(redissonClient.getLock("LOCK:" + key)).willReturn(rLock);
        given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
        given(rLock.isHeldByCurrentThread()).willReturn(true);

        AtomicBoolean executed = new AtomicBoolean(false);

        // when
        distributedLockTemplate.executeWithoutResult(key, () -> executed.set(true));

        // then
        assertThat(executed.get()).isTrue();
        verify(rLock).unlock();
    }
}
