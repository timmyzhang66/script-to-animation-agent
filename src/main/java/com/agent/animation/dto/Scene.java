package com.agent.animation.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 场景数据对象
 * 代表分镜中的一个场景，包含涉及的角色、关键帧、表演指令和视频信息
 */
public class Scene {
    private int sceneNumber;
    private String description;
    private String visualDescription;
    private String dialogue;
    private String actionCode; // 新增：表演指令码，如 LOOK_DOWN_CONTEMPT

    // 场景涉及的角色名称列表
    private List<String> characterNames;

    // 关键帧信息
    private String keyframePath;      // 本地文件路径
    private String keyframeUrl;       // GCS/OSS URL

    // 视频信息
    private String videoPath;         // 本地文件路径
    private String videoUrl;          // GCS/OSS URL

    public Scene() {
        this.characterNames = new ArrayList<>();
    }

    public Scene(int sceneNumber, String description) {
        this.sceneNumber = sceneNumber;
        this.description = description;
        this.characterNames = new ArrayList<>();
    }

    // --- 新增 Getter 和 Setter ---
    public String getActionCode() { return actionCode; }
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }

    public int getSceneNumber() { return sceneNumber; }
    public void setSceneNumber(int sceneNumber) { this.sceneNumber = sceneNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVisualDescription() { return visualDescription; }
    public void setVisualDescription(String visualDescription) { this.visualDescription = visualDescription; }
    public String getDialogue() { return dialogue; }
    public void setDialogue(String dialogue) { this.dialogue = dialogue; }
    public List<String> getCharacterNames() { return characterNames; }
    public void setCharacterNames(List<String> characterNames) { this.characterNames = characterNames; }

    public void addCharacterName(String characterName) {
        if (!this.characterNames.contains(characterName)) {
            this.characterNames.add(characterName);
        }
    }

    public String getKeyframePath() { return keyframePath; }
    public void setKeyframePath(String keyframePath) { this.keyframePath = keyframePath; }
    public String getKeyframeUrl() { return keyframeUrl; }
    public void setKeyframeUrl(String keyframeUrl) { this.keyframeUrl = keyframeUrl; }
    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    @Override
    public String toString() {
        return "Scene{" +
                "sceneNumber=" + sceneNumber +
                ", actionCode='" + actionCode + '\'' +
                ", description='" + description + '\'' +
                ", characterNames=" + characterNames +
                ", videoUrl='" + videoUrl + '\'' +
                '}';
    }
}