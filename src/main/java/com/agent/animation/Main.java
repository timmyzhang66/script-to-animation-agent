package com.agent.animation;

import com.agent.animation.agent.AnimationAgent;
import com.agent.animation.core.engine.WorkflowTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 程序入口
 * 演示异步提交与任务进度追踪
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            logger.info("=== Starting Professional Animation Agent ===");

            AnimationAgent agent = new AnimationAgent();

            // 1. 提交任务（以职场吐槽脚本为例）
            String scriptPath = "example_script.txt";
            String taskId = agent.generateAnimation(scriptPath);
            logger.info("Task submitted! Task ID: {}", taskId);

            // 2. 轮询状态（模拟前端进度条显示）
            WorkflowTask task;
            while (true) {
                task = agent.getTaskStatus(taskId);
                WorkflowTask.Status status = task.getStatus();

                logger.info("Current Status: [{}]", status);

                if (status == WorkflowTask.Status.COMPLETED) {
                    logger.info("SUCCESS! Animation is ready.");
                    String finalVideo = task.getContext().getFinalVideoUrl();
                    logger.info("Final Video OSS URL: {}", finalVideo);
                    break;
                } else if (status == WorkflowTask.Status.FAILED) {
                    logger.error("FAILED! Check logs for details.");
                    break;
                }

                // 每 5 秒查询一次进度
                Thread.sleep(5000);
            }

            logger.info("=== Task Processing Finished ===");

        } catch (Exception e) {
            logger.error("System Error", e);
            System.exit(1);
        }
    }
}