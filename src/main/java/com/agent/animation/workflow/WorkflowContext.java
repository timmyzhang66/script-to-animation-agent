package com.agent.animation.workflow;

import com.agent.animation.dto.Character;
import com.agent.animation.dto.ScriptInput;
import com.agent.animation.dto.Storyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流上下文
 * 保存整个工作流程中的所有数据，包括多角色、资源链接等
 */
public class WorkflowContext {
    private ScriptInput scriptInput;
    
    // 所有角色（包括主角和配角）
    private List<Character> characters;
    
    // 角色名称到角色对象的映射（便于快速查找）
    private Map<String, Character> characterMap;
    
    // 分镜板
    private Storyboard storyboard;
    
    // 最终视频信息
    private String finalVideoPath;      // 本地文件路径
    private String finalVideoUrl;       // GCS/OSS URL
    
    // 时间统计
    private long startTime;
    private long endTime;

    public WorkflowContext(ScriptInput scriptInput) {
        this.scriptInput = scriptInput;
        this.characters = new ArrayList<>();
        this.characterMap = new HashMap<>();
        this.startTime = System.currentTimeMillis();
    }

    // ==================== ScriptInput ====================
    
    public ScriptInput getScriptInput() {
        return scriptInput;
    }

    public void setScriptInput(ScriptInput scriptInput) {
        this.scriptInput = scriptInput;
    }

    // ==================== Characters ====================
    
    /**
     * 获取所有角色
     */
    public List<Character> getCharacters() {
        return characters;
    }
    
    /**
     * 设置所有角色
     */
    public void setCharacters(List<Character> characters) {
        this.characters = characters;
        // 更新映射
        this.characterMap.clear();
        for (Character character : characters) {
            this.characterMap.put(character.getName(), character);
        }
    }
    
    /**
     * 添加角色
     */
    public void addCharacter(Character character) {
        this.characters.add(character);
        this.characterMap.put(character.getName(), character);
    }
    
    /**
     * 根据名称获取角色
     */
    public Character getCharacter(String name) {
        return characterMap.get(name);
    }
    
    /**
     * 获取主角
     */
    public Character getMainCharacter() {
        for (Character character : characters) {
            if (character.isMainCharacter()) {
                return character;
            }
        }
        return characters.isEmpty() ? null : characters.get(0);
    }
    
    /**
     * 设置主角（兼容旧代码）
     */
    public void setMainCharacter(Character mainCharacter) {
        mainCharacter.setMainCharacter(true);
        addCharacter(mainCharacter);
    }
    
    /**
     * 获取场景涉及的角色列表
     */
    public List<Character> getCharactersForScene(List<String> characterNames) {
        List<Character> result = new ArrayList<>();
        for (String name : characterNames) {
            Character character = characterMap.get(name);
            if (character != null) {
                result.add(character);
            }
        }
        return result;
    }

    // ==================== Storyboard ====================
    
    public Storyboard getStoryboard() {
        return storyboard;
    }

    public void setStoryboard(Storyboard storyboard) {
        this.storyboard = storyboard;
    }

    // ==================== Final Video ====================
    
    public String getFinalVideoPath() {
        return finalVideoPath;
    }

    public void setFinalVideoPath(String finalVideoPath) {
        this.finalVideoPath = finalVideoPath;
        this.endTime = System.currentTimeMillis();
    }
    
    public String getFinalVideoUrl() {
        return finalVideoUrl;
    }
    
    public void setFinalVideoUrl(String finalVideoUrl) {
        this.finalVideoUrl = finalVideoUrl;
    }

    // ==================== Time Statistics ====================
    
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

    // ==================== Summary ====================
    
    /**
     * 生成上下文摘要（用于日志和调试）
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("Workflow Context Summary\n");
        sb.append("=".repeat(60)).append("\n\n");
        
        // 脚本信息
        sb.append("Script:\n");
        sb.append("  Length: ").append(scriptInput.getScriptContent().length()).append(" characters\n\n");
        
        // 角色信息
        sb.append("Characters (").append(characters.size()).append("):\n");
        for (Character character : characters) {
            sb.append("  - ").append(character.getName());
            if (character.isMainCharacter()) {
                sb.append(" [MAIN]");
            }
            sb.append("\n");
            sb.append("    Role: ").append(character.getRole()).append("\n");
            sb.append("    Image: ").append(character.getImagePath()).append("\n");
            sb.append("    URL: ").append(character.getImageUrl()).append("\n");
        }
        sb.append("\n");
        
        // 场景信息
        if (storyboard != null) {
            sb.append("Scenes (").append(storyboard.getSceneCount()).append("):\n");
            for (int i = 0; i < storyboard.getScenes().size(); i++) {
                var scene = storyboard.getScenes().get(i);
                sb.append("  Scene ").append(i + 1).append(":\n");
                sb.append("    Characters: ").append(scene.getCharacterNames()).append("\n");
                sb.append("    Keyframe: ").append(scene.getKeyframePath()).append("\n");
                sb.append("    Keyframe URL: ").append(scene.getKeyframeUrl()).append("\n");
                sb.append("    Video: ").append(scene.getVideoPath()).append("\n");
                sb.append("    Video URL: ").append(scene.getVideoUrl()).append("\n");
            }
            sb.append("\n");
        }
        
        // 最终视频
        sb.append("Final Video:\n");
        sb.append("  Path: ").append(finalVideoPath).append("\n");
        sb.append("  URL: ").append(finalVideoUrl).append("\n\n");
        
        // 时间统计
        sb.append("Duration: ").append(getDurationMillis() / 1000.0).append(" seconds\n");
        sb.append("=".repeat(60)).append("\n");
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return "WorkflowContext{" +
                "characters=" + characters.size() +
                ", scenes=" + (storyboard != null ? storyboard.getSceneCount() : 0) +
                ", finalVideoPath='" + finalVideoPath + '\'' +
                ", finalVideoUrl='" + finalVideoUrl + '\'' +
                ", durationMillis=" + getDurationMillis() +
                '}';
    }
}
