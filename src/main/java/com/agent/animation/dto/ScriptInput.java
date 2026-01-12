package com.agent.animation.dto;

/**
 * 用户输入的脚本数据对象
 */
public class ScriptInput {
    private String scriptContent;
    private String title;
    private String description;

    public ScriptInput() {
    }

    public ScriptInput(String scriptContent) {
        this.scriptContent = scriptContent;
    }

    public ScriptInput(String scriptContent, String title, String description) {
        this.scriptContent = scriptContent;
        this.title = title;
        this.description = description;
    }

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ScriptInput{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", scriptLength=" + (scriptContent != null ? scriptContent.length() : 0) +
                '}';
    }
}
