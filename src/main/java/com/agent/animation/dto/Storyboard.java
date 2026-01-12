package com.agent.animation.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 分镜数据对象
 * 包含多个场景
 */
public class Storyboard {
    private List<Scene> scenes;

    public Storyboard() {
        this.scenes = new ArrayList<>();
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public void setScenes(List<Scene> scenes) {
        this.scenes = scenes;
    }

    public void addScene(Scene scene) {
        this.scenes.add(scene);
    }

    public int getSceneCount() {
        return scenes.size();
    }

    @Override
    public String toString() {
        return "Storyboard{" +
                "sceneCount=" + scenes.size() +
                '}';
    }
}
