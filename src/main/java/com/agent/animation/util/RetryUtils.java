package com.agent.animation.util;

import com.agent.animation.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 工业化重试工具类
 * 适配 Singleton 模式的 AppConfig，提供指数退避重试逻辑
 */
public class RetryUtils {
    private static final Logger logger = LoggerFactory.getLogger(RetryUtils.class);

    /**
     * 执行带重试机制的任务
     * 默认重试 3 次，初始等待 1 秒，采用指数倍增
     */
    public static <T> T executeWithRetry(Callable<T> task) throws Exception {
        return executeWithRetry(task, 3, 1000L, 2000L);
    }

    /**
     * 高级重试方法
     * @param task 执行的任务
     * @param maxRetries 最大重试次数
     * @param initialDelayMs 初始延迟时间
     * @param maxDelayMs 最大延迟时间
     */
    public static <T> T executeWithRetry(Callable<T> task, int maxRetries, long initialDelayMs, long maxDelayMs) throws Exception {
        int attempt = 0;
        long delay = initialDelayMs;

        while (attempt <= maxRetries) {
            try {
                return task.call();
            } catch (Exception e) {
                attempt++;
                if (attempt > maxRetries) {
                    logger.error("All {} retry attempts failed.", maxRetries);
                    throw e;
                }

                // 判断是否为 429 或 500 类错误（这里可以根据实际异常类型扩展）
                logger.warn("Attempt {} failed: {}. Retrying in {}ms...", attempt, e.getMessage(), delay);

                Thread.sleep(delay);

                // 指数退避：延迟加倍，但不超过最大值
                delay = Math.min(delay * 2, maxDelayMs);
            }
        }
        throw new Exception("Retry logic failed unexpectedly.");
    }
}