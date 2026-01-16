package com.agent.animation.workflow;

import com.agent.animation.dto.Character;
import com.agent.animation.dto.ScriptInput;
import com.agent.animation.dto.Storyboard;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工作流上下文
 * 存储整个任务生命周期中的中间状态和最终结果
 */
public class WorkflowContext {
    private ScriptInput scriptInput;
    private List<Character> characters;
    private Storyboard storyboard;
    private String finalVideoUrl;
    private String finalVideoPath;

    public WorkflowContext() {
        this.characters = new ArrayList<>();
    }

    public WorkflowContext(ScriptInput scriptInput) {
        this.scriptInput = scriptInput;
        this.characters = new ArrayList<>();
    }

    /**
     * 根据角色名称列表从全局角色库中筛选出对应的角色对象
     * 用于关键帧生成和视频生成时提取相关的 IP 参考资产
     */
    public List<Character> getCharactersForScene(List<String> names) {
        if (names == null || names.isEmpty() || characters == null) {
            return new ArrayList<>();
        }
        return characters.stream()
                .filter(c -> names.contains(c.getName()))
                .collect(Collectors.toList());
    }

    public ScriptInput getScriptInput() { return scriptInput; }
    public void setScriptInput(ScriptInput scriptInput) { this.scriptInput = scriptInput; }

    public List<Character> getCharacters() { return characters; }
    public void setCharacters(List<Character> characters) { this.characters = characters; }

    public void addCharacter(Character character) {
        if (this.characters == null) this.characters = new ArrayList<>();
        this.characters.add(character);
    }

    public Storyboard getStoryboard() { return storyboard; }
    public void setStoryboard(Storyboard storyboard) { this.storyboard = storyboard; }

    public String getFinalVideoUrl() { return finalVideoUrl; }
    public void setFinalVideoUrl(String finalVideoUrl) { this.finalVideoUrl = finalVideoUrl; }

    public String getFinalVideoPath() { return finalVideoPath; }
    public void setFinalVideoPath(String finalVideoPath) { this.finalVideoPath = finalVideoPath; }
}