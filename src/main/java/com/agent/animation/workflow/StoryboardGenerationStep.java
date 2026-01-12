package com.agent.animation.workflow;

import com.agent.animation.dto.Scene;
import com.agent.animation.dto.Storyboard;
import com.agent.animation.service.GeminiTextService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分镜生成工作流步骤
 * 将脚本分解为多个场景
 */
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
        logger.info("Starting storyboard generation step");
        
        String scriptContent = context.getScriptInput().getScriptContent();
        
        // 1. 使用 Gemini 生成分镜
        logger.info("Generating storyboard with Gemini");
        String storyboardJson = geminiTextService.generateStoryboard(scriptContent);
        
        // 2. 解析 JSON 响应
        Storyboard storyboard = parseStoryboard(storyboardJson);
        
        // 3. 为每个场景生成视觉描述
        String characterDescription = context.getMainCharacter().getDescription();
        for (Scene scene : storyboard.getScenes()) {
            logger.info("Generating visual description for scene {}", scene.getSceneNumber());
            String visualDescription = geminiTextService.enhanceVisualDescription(
                    scene.getDescription(),
                    characterDescription
            );
            scene.setVisualDescription(visualDescription);
        }
        
        // 4. 保存到上下文
        context.setStoryboard(storyboard);
        
        logger.info("Storyboard generation completed with {} scenes", storyboard.getSceneCount());
    }

    /**
     * 解析分镜 JSON 数据
     * 
     * @param jsonString JSON 字符串
     * @return 分镜对象
     * @throws Exception 解析失败时抛出异常
     */
    private Storyboard parseStoryboard(String jsonString) throws Exception {
        try {
            // 清理 JSON 字符串
            String cleanJson = jsonString.trim();
            
            // 移除 Markdown 代码块标记
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();
            
            // 解析 JSON 对象
            JsonObject rootObj = gson.fromJson(cleanJson, JsonObject.class);
            JsonArray jsonArray = rootObj.getAsJsonArray("scenes");
            
            Storyboard storyboard = new Storyboard();
            
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject sceneObj = jsonArray.get(i).getAsJsonObject();
                
                Scene scene = new Scene();
                scene.setSceneNumber(sceneObj.has("sceneNumber") ? 
                        sceneObj.get("sceneNumber").getAsInt() : i + 1);
                scene.setDescription(sceneObj.has("description") ? 
                        sceneObj.get("description").getAsString() : "");
                scene.setDialogue(sceneObj.has("dialogue") ? 
                        sceneObj.get("dialogue").getAsString() : "");
                
                // visualDescription 可能在这一步还没有，会在后面生成
                if (sceneObj.has("visualDescription")) {
                    scene.setVisualDescription(sceneObj.get("visualDescription").getAsString());
                }
                
                storyboard.addScene(scene);
            }
            
            return storyboard;
            
        } catch (Exception e) {
            logger.error("Failed to parse storyboard JSON", e);
            throw new Exception("Failed to parse storyboard: " + e.getMessage(), e);
        }
    }

    @Override
    public String getStepName() {
        return "Storyboard Generation";
    }
}
