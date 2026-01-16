package com.agent.animation.core.engine;

import com.agent.animation.workflow.WorkflowContext;
import com.agent.animation.workflow.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class AsyncWorkflowEngine {
    private static final Logger logger = LoggerFactory.getLogger(AsyncWorkflowEngine.class);
    private final List<WorkflowStep> steps = new ArrayList<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Map<String, WorkflowTask> tasks = new ConcurrentHashMap<>();

    public void addStep(WorkflowStep step) { steps.add(step); }

    public String submit(WorkflowContext context) {
        String taskId = "task_" + System.currentTimeMillis();
        WorkflowTask task = new WorkflowTask(taskId, context);
        tasks.put(taskId, task);

        executor.submit(() -> {
            task.setStatus(WorkflowTask.Status.RUNNING);
            try {
                for (WorkflowStep step : steps) {
                    logger.info("[{}] Running: {}", taskId, step.getStepName());
                    step.execute(context);
                }
                task.setStatus(WorkflowTask.Status.COMPLETED);
            } catch (Exception e) {
                task.setStatus(WorkflowTask.Status.FAILED);
                logger.error("Task {} failed", taskId, e);
            }
        });
        return taskId;
    }

    public WorkflowTask getTask(String taskId) { return tasks.get(taskId); }
}