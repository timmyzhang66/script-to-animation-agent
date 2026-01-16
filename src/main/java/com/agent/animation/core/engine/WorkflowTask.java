package com.agent.animation.core.engine;

import com.agent.animation.workflow.WorkflowContext;

public class WorkflowTask {
    public enum Status { PENDING, RUNNING, COMPLETED, FAILED }
    private String taskId;
    private Status status = Status.PENDING;
    private WorkflowContext context;

    public WorkflowTask(String taskId, WorkflowContext context) {
        this.taskId = taskId;
        this.context = context;
    }

    public String getTaskId() { return taskId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public WorkflowContext getContext() { return context; }
}