package top.harrylei.community.core.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 请求上下文测试
 *
 * @author harry
 */
@DisplayName("ReqInfoContext 测试")
class ReqInfoContextTest {

    @AfterEach
    void tearDown() {
        ReqInfoContext.clear();
    }

    @Nested
    @DisplayName("setContext 和 getContext 方法测试")
    class ContextManagementTest {

        @Test
        @DisplayName("设置上下文后应能获取")
        void shouldGetContextAfterSet() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUserId(123L);
            reqInfo.setClientIp("127.0.0.1");

            ReqInfoContext.setContext(reqInfo);

            ReqInfoContext.ReqInfo result = ReqInfoContext.getContext();
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(123L);
            assertThat(result.getClientIp()).isEqualTo("127.0.0.1");
        }

        @Test
        @DisplayName("未设置上下文时应自动创建空上下文")
        void shouldCreateEmptyContextWhenNotSet() {
            ReqInfoContext.clear();

            ReqInfoContext.ReqInfo result = ReqInfoContext.getContext();

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isNull();
        }

        @Test
        @DisplayName("多次获取应返回同一上下文对象")
        void shouldReturnSameContextOnMultipleGets() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUserId(456L);
            ReqInfoContext.setContext(reqInfo);

            ReqInfoContext.ReqInfo first = ReqInfoContext.getContext();
            ReqInfoContext.ReqInfo second = ReqInfoContext.getContext();

            assertThat(first).isSameAs(second);
        }
    }

    @Nested
    @DisplayName("clear 方法测试")
    class ClearContextTest {

        @Test
        @DisplayName("清除后获取应返回新的空上下文")
        void shouldReturnNewEmptyContextAfterClear() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUserId(789L);
            ReqInfoContext.setContext(reqInfo);

            ReqInfoContext.clear();

            ReqInfoContext.ReqInfo result = ReqInfoContext.getContext();
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isNull();
        }

        @Test
        @DisplayName("清除后设置新上下文应正常工作")
        void shouldWorkAfterClearAndReSet() {
            ReqInfoContext.ReqInfo first = new ReqInfoContext.ReqInfo();
            first.setUserId(100L);
            ReqInfoContext.setContext(first);

            ReqInfoContext.clear();

            ReqInfoContext.ReqInfo second = new ReqInfoContext.ReqInfo();
            second.setUserId(200L);
            ReqInfoContext.setContext(second);

            assertThat(ReqInfoContext.getContext().getUserId()).isEqualTo(200L);
        }
    }

    @Nested
    @DisplayName("ReqInfo.isAdmin 方法测试")
    class IsAdminTest {

        @Test
        @DisplayName("具有 ROLE_ADMIN 权限应返回 true")
        void shouldReturnTrueForAdminAuthority() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            assertThat(reqInfo.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("不具有 ROLE_ADMIN 权限应返回 false")
        void shouldReturnFalseWithoutAdminAuthority() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER")));

            assertThat(reqInfo.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("空权限列表应返回 false")
        void shouldReturnFalseForEmptyAuthorities() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setAuthorities(Collections.emptyList());

            assertThat(reqInfo.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("null 权限列表应返回 false")
        void shouldReturnFalseForNullAuthorities() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setAuthorities(null);

            assertThat(reqInfo.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("多个权限中包含 ROLE_ADMIN 应返回 true")
        void shouldReturnTrueWhenAdminAmongMultipleAuthorities() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setAuthorities(List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_EDITOR")
            ));

            assertThat(reqInfo.isAdmin()).isTrue();
        }
    }

    @Nested
    @DisplayName("ReqInfo.isLoggedIn 方法测试")
    class IsLoggedInTest {

        @Test
        @DisplayName("userId > 0 应返回 true")
        void shouldReturnTrueForPositiveUserId() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUserId(1L);

            assertThat(reqInfo.isLoggedIn()).isTrue();
        }

        @Test
        @DisplayName("userId = 0 应返回 false")
        void shouldReturnFalseForZeroUserId() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUserId(0L);

            assertThat(reqInfo.isLoggedIn()).isFalse();
        }

        @Test
        @DisplayName("userId < 0 应返回 false")
        void shouldReturnFalseForNegativeUserId() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUserId(-1L);

            assertThat(reqInfo.isLoggedIn()).isFalse();
        }

        @Test
        @DisplayName("userId = null 应返回 false")
        void shouldReturnFalseForNullUserId() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUserId(null);

            assertThat(reqInfo.isLoggedIn()).isFalse();
        }
    }

    @Nested
    @DisplayName("ReqInfo 属性测试")
    class ReqInfoPropertiesTest {

        @Test
        @DisplayName("链式调用应正常工作")
        void chainSettersShouldWork() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo()
                    .setUserId(100L)
                    .setClientIp("192.168.1.1")
                    .setHost("localhost")
                    .setPath("/api/test")
                    .setReferer("https://example.com")
                    .setUserAgent("Mozilla/5.0");

            assertThat(reqInfo.getUserId()).isEqualTo(100L);
            assertThat(reqInfo.getClientIp()).isEqualTo("192.168.1.1");
            assertThat(reqInfo.getHost()).isEqualTo("localhost");
            assertThat(reqInfo.getPath()).isEqualTo("/api/test");
            assertThat(reqInfo.getReferer()).isEqualTo("https://example.com");
            assertThat(reqInfo.getUserAgent()).isEqualTo("Mozilla/5.0");
        }

        @Test
        @DisplayName("authorities 默认值应为空列表")
        void authoritiesShouldDefaultToEmptyList() {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();

            assertThat(reqInfo.getAuthorities()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("多线程测试")
    class MultiThreadTest {

        @Test
        @DisplayName("不同线程应有独立的上下文")
        void differentThreadsShouldHaveIndependentContexts() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(2);
            AtomicReference<Long> thread1UserId = new AtomicReference<>();
            AtomicReference<Long> thread2UserId = new AtomicReference<>();

            Thread t1 = new Thread(() -> {
                ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
                reqInfo.setUserId(111L);
                ReqInfoContext.setContext(reqInfo);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                thread1UserId.set(ReqInfoContext.getContext().getUserId());
                ReqInfoContext.clear();
                latch.countDown();
            });

            Thread t2 = new Thread(() -> {
                ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
                reqInfo.setUserId(222L);
                ReqInfoContext.setContext(reqInfo);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                thread2UserId.set(ReqInfoContext.getContext().getUserId());
                ReqInfoContext.clear();
                latch.countDown();
            });

            t1.start();
            t2.start();

            latch.await(5, TimeUnit.SECONDS);

            assertThat(thread1UserId.get()).isEqualTo(111L);
            assertThat(thread2UserId.get()).isEqualTo(222L);
        }

        @Test
        @DisplayName("使用 TransmittableThreadLocal 应支持线程池传递")
        void shouldSupportThreadPoolTransmission() throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(1);

            try {
                // 主线程设置上下文
                ReqInfoContext.ReqInfo mainReqInfo = new ReqInfoContext.ReqInfo();
                mainReqInfo.setUserId(999L);
                ReqInfoContext.setContext(mainReqInfo);

                AtomicReference<Long> asyncUserId = new AtomicReference<>();
                CountDownLatch latch = new CountDownLatch(1);

                // 注意：TransmittableThreadLocal 需要配合 TtlRunnable 使用
                // 这里只测试基本的线程隔离
                executor.submit(() -> {
                    // 子线程获取上下文（使用 TTL 时应能获取父线程值）
                    asyncUserId.set(ReqInfoContext.getContext().getUserId());
                    latch.countDown();
                });

                latch.await(5, TimeUnit.SECONDS);

                // 由于使用了 TransmittableThreadLocal，子线程应能获取父线程的上下文
                // 如果没有使用 TtlExecutors 包装，则可能获取不到
                // 这个测试验证了上下文的存在性
                assertThat(asyncUserId.get()).isNotNull();
            } finally {
                executor.shutdown();
                ReqInfoContext.clear();
            }
        }
    }

    @Nested
    @DisplayName("ADMIN 常量测试")
    class AdminConstantTest {

        @Test
        @DisplayName("ADMIN 常量应为 ROLE_ADMIN")
        void adminConstantShouldBeCorrect() {
            assertThat(ReqInfoContext.ADMIN).isEqualTo("ROLE_ADMIN");
        }
    }
}
