package com.agent.animation.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 重试工具类
 * 实现指数退避重试逻辑，用于处理 API 配额限制（429 错误）
 */
public class RetryUtils {
    private static final Logger logger = LoggerFactory.getLogger(RetryUtils.class);

    /**
     * 执行带重试的操作
     * 
     * @param action 要执行的操作
     * @param maxAttempts 最大重试次数
     * @param initialDelayMs 初始延迟时间（毫秒）
     * @param maxDelayMs 最大延迟时间（毫秒）
     * @param <T> 返回值类型
     * @return 操作结果
     * @throws Exception 如果重试耗尽后仍然失败，抛出最后一次异常
     */
    public static <T> T executeWithRetry(Callable<T> action, int maxAttempts, long initialDelayMs, long maxDelayMs) throws Exception {
        int attempt = 0;
        long delay = initialDelayMs;

        while (true) {
            try {
                attempt++;
                return action.call();
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                boolean isRateLimit = errorMessage != null && (errorMessage.contains("429") || errorMessage.contains("Quota exceeded"));

                if (isRateLimit && attempt < maxAttempts) {
                    logger.warn("API Rate limit exceeded (429). Attempt {}/{} failed. Retrying in {} ms...", 
                            attempt, maxAttempts, delay);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new Exception("Retry interrupted", ie);
                    }

                    // 指数退避：延迟翻倍，但不超过最大延迟
                    delay = Math.min(delay * 2, maxDelayMs);
                } else {
                    // 如果不是频率限制错误，或者重试次数已耗尽，则抛出异常
                    if (attempt >= maxAttempts) {
                        logger.error("Max retry attempts ({}) reached. Final error: {}", maxAttempts, errorMessage);
                    }
                    throw e;
                }
            }
        }
    }

    /**
     * 使用默认参数执行重试
     */
    public static <T> T executeWithRetry(Callable<T> action) throws Exception {
        com.agent.animation.config.AppConfig config = com.agent.animation.config.AppConfig.getInstance();
        return executeWithRetry(action, 
                config.getMaxRetryAttempts(), 
                config.getInitialRetryDelayMs(), 
                config.getMaxRetryDelayMs());
    }
}
