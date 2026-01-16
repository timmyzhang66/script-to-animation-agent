package com.agent.animation.workflow;

import com.agent.animation.dto.Scene;
import com.agent.animation.dto.Storyboard;
import com.agent.animation.service.GeminiTextService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StoryboardGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(StoryboardGenerationStep.class);
    private final GeminiTextService geminiTextService;
    private final Gson gson;

    public StoryboardGenerationStep() {
        this.geminiTextService = new GeminiTextService();
        this.gson = new Gson();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting industrial storyboard generation step");

        // 1. 准备已固定的角色上下文，确保 AI 导演识别这些 IP
        String actorContext = context.getCharacters().stream()
                .map(c -> c.getName() + " (" + c.getRole() + "): " + c.getDescription())
                .collect(Collectors.joining("\n"));

        String scriptContent = context.getScriptInput().getScriptContent();

        // 2. 调用工业化分镜生成
        logger.info("Directing scenes with action codes...");
        String storyboardJson = geminiTextService.generateIndustrialStoryboard(scriptContent, actorContext);

        JsonObject jsonResponse = gson.fromJson(storyboardJson, JsonObject.class);
        JsonArray scenesArray = jsonResponse.getAsJsonArray("scenes");

        List<Scene> scenes = new ArrayList<>();
        String allCharacterNames = context.getCharacters().stream()
                .map(com.agent.animation.dto.Character::getName)
                .collect(Collectors.joining(", "));

        for (int i = 0; i < scenesArray.size(); i++) {
            JsonObject sceneJson = scenesArray.get(i).getAsJsonObject();

            Scene scene = new Scene(sceneJson.get("sceneNumber").getAsInt(), sceneJson.get("description").getAsString());
            scene.setVisualDescription(sceneJson.get("visualDescription").getAsString());
            scene.setDialogue(sceneJson.has("dialogue") ? sceneJson.get("dialogue").getAsString() : "");

            // 核心：注入表演指令码
            if (sceneJson.has("actionCode")) {
                scene.setActionCode(sceneJson.get("actionCode").getAsString());
                logger.info("Scene {} assigned action: {}", scene.getSceneNumber(), scene.getActionCode());
            }

            // 3. 角色映射（直接通过名字包含关系，更高效）
            for (String charName : allCharacterNames.split(", ")) {
                if (scene.getVisualDescription().contains(charName)) {
                    scene.addCharacterName(charName);
                }
            }
            scenes.add(scene);
        }

        Storyboard storyboard = new Storyboard();
        storyboard.setScenes(scenes);
        context.setStoryboard(storyboard);
        logger.info("Storyboard ready with {} instructional scenes", scenes.size());
    }

    @Override
    public String getStepName() { return "Instructional Storyboard Generation"; }
}