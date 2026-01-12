package com.agent.animation.agent;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.ScriptInput;
import com.agent.animation.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 动画生成 Agent 主类
 * 负责协调整个脚本到动画的生成流程
 */
public class AnimationAgent {
    private static final Logger logger = LoggerFactory.getLogger(AnimationAgent.class);
    private final WorkflowEngine workflowEngine;
    private final AppConfig config;

    public AnimationAgent() throws Exception {
        this.config = AppConfig.getInstance();
        this.workflowEngine = new WorkflowEngine();
        
        logger.info("Initializing workflow steps...");
        
        // 添加所有工作流步骤
        workflowEngine.addStep(new CharacterGenerationStep());
        workflowEngine.addStep(new StoryboardGenerationStep());
        workflowEngine.addStep(new KeyframeGenerationStep());
        workflowEngine.addStep(new VideoGenerationStep());
        workflowEngine.addStep(new VideoMergingStep());
        
        logger.info("Workflow initialized with {} steps", workflowEngine.getStepCount());
    }

    /**
     * 确保必要的目录存在
     */
    private void ensureDirectories() {
        String tempDir = config.getTempDir();
        String outputDir = config.getOutputDir();
        
        new File(tempDir).mkdirs();
        new File(outputDir).mkdirs();
        new File("logs").mkdirs();
        
        logger.info("Directories ensured: temp={}, output={}", tempDir, outputDir);
    }

    /**
     * 添加自定义工作流步骤
     * 
     * @param step 工作流步骤
     */
    public void addWorkflowStep(WorkflowStep step) {
        workflowEngine.addStep(step);
    }

    /**
     * 从脚本生成动画视频
     * 
     * @param scriptInput 用户输入的脚本
     * @return 生成的视频文件路径
     */
    public String generateAnimation(ScriptInput scriptInput) {
        logger.info("Starting animation generation for script: {}", scriptInput);
        
        // 创建工作流上下文
        WorkflowContext context = new WorkflowContext(scriptInput);
        
        // 执行工作流
        boolean success = workflowEngine.execute(context);
        
        if (success) {
            logger.info("Animation generation completed successfully");
            logger.info("Final video path: {}", context.getFinalVideoPath());
            logger.info("Total duration: {} ms ({} seconds)", 
                    context.getDurationMillis(), 
                    context.getDurationMillis() / 1000.0);
            return context.getFinalVideoPath();
        } else {
            logger.error("Animation generation failed");
            return null;
        }
    }

    /**
     * 获取配置信息
     * 
     * @return 应用配置
     */
    public AppConfig getConfig() {
        return config;
    }

    /**
     * 获取工作流引擎
     * 
     * @return 工作流引擎
     */
    public WorkflowEngine getWorkflowEngine() {
        return workflowEngine;
    }
}
