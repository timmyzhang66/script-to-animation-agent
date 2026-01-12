package com.agent.animation.dto;

/**
 * 场景数据对象
 * 代表分镜中的一个场景
 */
public class Scene {
    private int sceneNumber;
    private String description;
    private String visualDescription;
    private String dialogue;
    private String keyframePath;
    private String keyframeUrl;  // GCS URL 或公开 URL
    private String videoPath;

    public Scene() {
    }

    public Scene(int sceneNumber, String description) {
        this.sceneNumber = sceneNumber;
        this.description = description;
    }
    
    public Scene(int sceneNumber, String description, String visualDescription, String keyframePath, String keyframeUrl) {
        this.sceneNumber = sceneNumber;
        this.description = description;
        this.visualDescription = visualDescription;
        this.keyframePath = keyframePath;
        this.keyframeUrl = keyframeUrl;
    }

    public int getSceneNumber() {
        return sceneNumber;
    }

    public void setSceneNumber(int sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVisualDescription() {
        return visualDescription;
    }

    public void setVisualDescription(String visualDescription) {
        this.visualDescription = visualDescription;
    }

    public String getDialogue() {
        return dialogue;
    }

    public void setDialogue(String dialogue) {
        this.dialogue = dialogue;
    }

    public String getKeyframePath() {
        return keyframePath;
    }

    public void setKeyframePath(String keyframePath) {
        this.keyframePath = keyframePath;
    }
    
    public String getKeyframeUrl() {
        return keyframeUrl;
    }
    
    public void setKeyframeUrl(String keyframeUrl) {
        this.keyframeUrl = keyframeUrl;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    @Override
    public String toString() {
        return "Scene{" +
                "sceneNumber=" + sceneNumber +
                ", description='" + description + '\'' +
                ", keyframePath='" + keyframePath + '\'' +
                ", keyframeUrl='" + keyframeUrl + '\'' +
                ", videoPath='" + videoPath + '\'' +
                '}';
    }
}
