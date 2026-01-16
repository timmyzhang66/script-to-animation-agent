package com.agent.animation.workflow;

import com.agent.animation.dto.Scene;
import com.agent.animation.dto.Storyboard;
import com.agent.animation.parser.ScriptParser;
import com.agent.animation.service.GeminiTextService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分镜生成工作流步骤
 * 将脚本分解为场景，并分析每个场景涉及的角色
 */
public class StoryboardGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(StoryboardGenerationStep.class);
    private final ScriptParser scriptParser;
    private final GeminiTextService geminiTextService;
    private final Gson gson;

    public StoryboardGenerationStep() {
        this.scriptParser = new ScriptParser();
        this.geminiTextService = new GeminiTextService();
        this.gson = new Gson();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting storyboard generation step");
        
        String scriptContent = context.getScriptInput().getScriptContent();
        
        // 1. 使用 ScriptParser 解析脚本
        logger.info("Parsing script to extract scenes and dialogues...");
        List<Scene> scenes = scriptParser.parse(scriptContent);
        
        logger.info("Parsed {} scenes from the script", scenes.size());
        
        // 2. 为每个场景匹配角色
        // 获取所有角色名称（用于场景角色分析）
        String allCharacterNames = context.getCharacters().stream()
                .map(c -> c.getName())
                .collect(Collectors.joining(", "));
        
        for (int i = 0; i < scenes.size(); i++) {
            Scene scene = scenes.get(i);
            
            // 3. 分析场景涉及的角色
            logger.info("Analyzing characters for scene {}/{}", i + 1, scenes.size());
            
            try {
                String sceneCharactersJson = geminiTextService.analyzeSceneCharacters(
                        scene.getVisualDescription() + " " + (scene.getDialogue() != null ? scene.getDialogue() : ""), 
                        allCharacterNames
                );
                
                JsonObject sceneCharJson = gson.fromJson(sceneCharactersJson, JsonObject.class);
                JsonArray characterNamesArray = sceneCharJson.getAsJsonArray("characterNames");
                
                if (characterNamesArray != null) {
                    List<String> characterNames = new ArrayList<>();
                    for (int j = 0; j < characterNamesArray.size(); j++) {
                        characterNames.add(characterNamesArray.get(j).getAsString());
                    }
                    scene.setCharacterNames(characterNames);
                    
                    logger.info("Scene {} involves characters: {}", scene.getSceneNumber(), characterNames);
                }
            } catch (Exception e) {
                logger.warn("Failed to analyze characters for scene {}: {}", scene.getSceneNumber(), e.getMessage());
                // 如果分析失败，默认使用主角
                if (!context.getCharacters().isEmpty()) {
                    scene.addCharacterName(context.getMainCharacter().getName());
                }
            }
        }
        
        // 5. 创建 Storyboard 对象并保存到上下文
        Storyboard storyboard = new Storyboard();
        storyboard.setScenes(scenes);
        context.setStoryboard(storyboard);
        
        logger.info("Storyboard generation completed with {} scenes", scenes.size());
        
        // 打印场景摘要
        logger.info("Scene Summary:");
        for (Scene scene : scenes) {
            logger.info("  Scene {}: {}", scene.getSceneNumber(), scene.getDescription());
            logger.info("    Characters: {}", scene.getCharacterNames());
        }
    }

    @Override
    public String getStepName() {
        return "Storyboard Generation";
    }
}
