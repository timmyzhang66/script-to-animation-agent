package com.agent.animation.workflow;

import com.agent.animation.dto.Character;
import com.agent.animation.dto.ScriptInput;
import com.agent.animation.dto.Storyboard;

/**
 * 工作流上下文
 * 保存整个工作流程中的所有数据
 */
public class WorkflowContext {
    private ScriptInput scriptInput;
    private Character mainCharacter;
    private Storyboard storyboard;
    private String finalVideoPath;
    private long startTime;
    private long endTime;

    public WorkflowContext(ScriptInput scriptInput) {
        this.scriptInput = scriptInput;
        this.startTime = System.currentTimeMillis();
    }

    public ScriptInput getScriptInput() {
        return scriptInput;
    }

    public void setScriptInput(ScriptInput scriptInput) {
        this.scriptInput = scriptInput;
    }

    public Character getMainCharacter() {
        return mainCharacter;
    }

    public void setMainCharacter(Character mainCharacter) {
        this.mainCharacter = mainCharacter;
    }

    public Storyboard getStoryboard() {
        return storyboard;
    }

    public void setStoryboard(Storyboard storyboard) {
        this.storyboard = storyboard;
    }

    public String getFinalVideoPath() {
        return finalVideoPath;
    }

    public void setFinalVideoPath(String finalVideoPath) {
        this.finalVideoPath = finalVideoPath;
        this.endTime = System.currentTimeMillis();
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getDurationMillis() {
        if (endTime == 0) {
            return System.currentTimeMillis() - startTime;
        }
        return endTime - startTime;
    }

    @Override
    public String toString() {
        return "WorkflowContext{" +
                "scriptInput=" + scriptInput +
                ", mainCharacter=" + mainCharacter +
                ", storyboard=" + storyboard +
                ", finalVideoPath='" + finalVideoPath + '\'' +
                ", durationMillis=" + getDurationMillis() +
                '}';
    }
}
