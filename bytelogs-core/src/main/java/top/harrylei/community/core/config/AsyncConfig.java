package top.harrylei.community.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步配置
 *
 * @author harry
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 统计专用线程池
     * 用于：阅读统计、点赞统计、收藏统计、用户行为统计等
     * 特点：允许丢失、低优先级、高吞吐量
     */
    @Bean("statisticsExecutor")
    public Executor statisticsExecutor() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：基于CPU核心数
        executor.setCorePoolSize(Math.max(2, availableProcessors / 4));
        // 最大线程数：核心线程数的2倍
        executor.setMaxPoolSize(Math.max(4, availableProcessors / 2));
        // 队列容量：允许较大缓冲
        executor.setQueueCapacity(200);
        // 线程名前缀
        executor.setThreadNamePrefix("Statistics-");
        // 线程空闲时间：60秒后回收非核心线程
        executor.setKeepAliveSeconds(60);
        // 拒绝策略：直接丢弃并记录日志
        executor.setRejectedExecutionHandler((r, exec) -> {
            log.warn("统计任务被拒绝执行，队列已满: activeCount={}, poolSize={}, queueSize={}",
                     exec.getActiveCount(), exec.getPoolSize(), exec.getQueue().size());
        });
        // 优雅关闭：等待任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }
}