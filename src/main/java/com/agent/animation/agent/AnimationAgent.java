package com.agent.animation.agent;

import com.agent.animation.core.engine.AsyncWorkflowEngine;
import com.agent.animation.core.engine.WorkflowTask;
import com.agent.animation.dto.ScriptInput;
import com.agent.animation.workflow.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工业化动画代理类
 * 负责编排异步工作流并提交执行任务
 */
public class AnimationAgent {
    private static final Logger logger = LoggerFactory.getLogger(AnimationAgent.class);
    private final AsyncWorkflowEngine engine;

    public AnimationAgent() throws Exception {
        this.engine = new AsyncWorkflowEngine();
        initializeWorkflow();
    }

    /**
     * 初始化工业化工作流步骤
     */
    private void initializeWorkflow() throws Exception {
        // 1. 角色生成（优先匹配 actors.json 资产）
        engine.addStep(new CharacterGenerationStep());
        // 2. 工业化分镜（生成带 actionCode 的指令）
        engine.addStep(new StoryboardGenerationStep());
        // 3. 关键帧生成（基于固定 IP 参考图）
        engine.addStep(new KeyframeGenerationStep());
        // 4. 指令化视频生成（根据 actionCode 增强表演）
        engine.addStep(new VideoGenerationStep());
        // 5. 视频合并
        engine.addStep(new VideoMergingStep());
    }

    /**
     * 提交生成任务
     * @param scriptPath 脚本文件路径
     * @return 返回任务 ID，用于后续状态查询
     */
    public String generateAnimation(String scriptPath) {
        logger.info("Submitting new animation task for script: {}", scriptPath);

        // 修正逻辑：使用无参构造函数并手动注入 ScriptInput
        WorkflowContext context = new WorkflowContext();
        context.setScriptInput(new ScriptInput(scriptPath));

        return engine.submit(context);
    }

    /**
     * 获取任务当前状态
     */
    public WorkflowTask getTaskStatus(String taskId) {
        return engine.getTask(taskId);
    }
}