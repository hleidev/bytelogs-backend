package top.harrylei.forum.core.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.harrylei.forum.core.common.constans.RedisKeyConstants;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Redis健康状态检查器
 *
 * @author harry
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthMonitor {

    private final RedisUtil redisUtil;

    /**
     * 是否启用健康检查
     */
    @Getter
    @Value("${redis.health.check.enabled:true}")
    private boolean healthCheckEnabled;

    /**
     * Redis健康检查键
     */
    private static final String HEALTH_CHECK_KEY = RedisKeyConstants.HEALTH_CHECK;

    /**
     * Redis健康检查值
     */
    private static final String HEALTH_CHECK_VALUE = "healthy";


    /**
     * Redis健康状态标识，true表示健康，false表示不健康
     */
    private final AtomicBoolean healthy = new AtomicBoolean(true);

    /**
     * 定时心跳检查Redis健康状态
     */
    @Scheduled(fixedRateString = "${redis.health.check.interval:30000}")
    public void performHealthCheck() {
        if (!healthCheckEnabled) {
            log.debug("Redis健康检查已禁用");
            return;
        }

        try {
            // 执行读写操作检查Redis连接
            redisUtil.set(HEALTH_CHECK_KEY, HEALTH_CHECK_VALUE);
            String result = redisUtil.get(HEALTH_CHECK_KEY, String.class);

            boolean currentHealthy = HEALTH_CHECK_VALUE.equals(result);
            boolean previousHealthy = healthy.getAndSet(currentHealthy);

            // 健康状态发生变化时记录日志
            if (previousHealthy != currentHealthy) {
                if (currentHealthy) {
                    log.info("Redis健康状态恢复正常");
                } else {
                    log.error("Redis健康状态异常，连接不可用");
                }
            } else if (log.isDebugEnabled()) {
                log.debug("Redis健康检查完成，状态：{}", currentHealthy ? "健康" : "异常");
            }

        } catch (Exception e) {
            boolean previousHealthy = healthy.getAndSet(false);

            if (previousHealthy) {
                log.error("Redis健康检查失败，连接异常", e);
            } else if (log.isDebugEnabled()) {
                log.debug("Redis健康检查失败，状态保持异常", e);
            }
        }
    }

    /**
     * 检查Redis是否健康
     *
     * @return true表示Redis健康可用，false表示Redis异常不可用
     */
    public boolean isHealthy() {
        if (!healthCheckEnabled) {
            return true;
        }
        return healthy.get();
    }

    /**
     * 检查Redis是否不健康
     *
     * @return true表示Redis不健康，false表示Redis健康
     */
    public boolean isUnhealthy() {
        return !isHealthy();
    }
}