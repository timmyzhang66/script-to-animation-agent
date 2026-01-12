package com.agent.animation.workflow;

/**
 * 工作流步骤接口
 * 每个步骤实现此接口来处理特定的任务
 */
public interface WorkflowStep {
    /**
     * 执行工作流步骤
     * 
     * @param context 工作流上下文
     * @throws Exception 执行过程中的异常
     */
    void execute(WorkflowContext context) throws Exception;

    /**
     * 获取步骤名称
     * 
     * @return 步骤名称
     */
    String getStepName();
}
