package com.agent.animation.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流引擎
 * 负责按顺序执行所有工作流步骤
 */
public class WorkflowEngine {
    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngine.class);
    private final List<WorkflowStep> steps;

    public WorkflowEngine() {
        this.steps = new ArrayList<>();
    }

    /**
     * 添加工作流步骤
     * 
     * @param step 工作流步骤
     */
    public void addStep(WorkflowStep step) {
        this.steps.add(step);
        logger.info("Added workflow step: {}", step.getStepName());
    }

    /**
     * 执行工作流
     * 
     * @param context 工作流上下文
     * @return 执行是否成功
     */
    public boolean execute(WorkflowContext context) {
        logger.info("Starting workflow execution with {} steps", steps.size());
        
        int currentStep = 0;
        for (WorkflowStep step : steps) {
            currentStep++;
            try {
                logger.info("Executing step {}/{}: {}", currentStep, steps.size(), step.getStepName());
                long stepStartTime = System.currentTimeMillis();
                
                step.execute(context);
                
                long stepDuration = System.currentTimeMillis() - stepStartTime;
                logger.info("Step {} completed in {} ms", step.getStepName(), stepDuration);
                
            } catch (Exception e) {
                logger.error("Failed to execute step: {}", step.getStepName(), e);
                return false;
            }
        }
        
        logger.info("Workflow execution completed successfully in {} ms", context.getDurationMillis());
        return true;
    }

    /**
     * 获取步骤数量
     * 
     * @return 步骤数量
     */
    public int getStepCount() {
        return steps.size();
    }

    /**
     * 清空所有步骤
     */
    public void clearSteps() {
        steps.clear();
        logger.info("Cleared all workflow steps");
    }
}
