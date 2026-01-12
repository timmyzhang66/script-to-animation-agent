package com.agent.animation.workflow;

import com.agent.animation.dto.Scene;
import com.agent.animation.dto.Storyboard;
import com.agent.animation.service.OpenAIService;
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
    private final OpenAIService openAIService;
    private final Gson gson;

    public StoryboardGenerationStep() {
        this.openAIService = new OpenAIService();
        this.gson = new Gson();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting storyboard generation step");
        
        String scriptContent = context.getScriptInput().getScriptContent();
        
        // 1. 使用 OpenAI 生成分镜
        logger.info("Generating storyboard with OpenAI");
        String storyboardJson = openAIService.generateStoryboard(scriptContent);
        
        // 2. 解析 JSON 响应
        Storyboard storyboard = parseStoryboard(storyboardJson);
        
        // 3. 为每个场景生成视觉描述
        String characterDescription = context.getMainCharacter().getDescription();
        for (Scene scene : storyboard.getScenes()) {
            logger.info("Generating visual description for scene {}", scene.getSceneNumber());
            String visualDescription = openAIService.generateVisualDescription(
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
            // 提取 JSON 数组（可能被包裹在代码块中）
            String cleanJson = extractJsonArray(jsonString);
            
            JsonArray jsonArray = gson.fromJson(cleanJson, JsonArray.class);
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

    /**
     * 从响应中提取 JSON 数组
     * 处理可能的 Markdown 代码块包裹
     * 
     * @param response 原始响应
     * @return 清理后的 JSON 字符串
     */
    private String extractJsonArray(String response) {
        String cleaned = response.trim();
        
        // 移除 Markdown 代码块标记
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        cleaned = cleaned.trim();
        
        // 确保是 JSON 数组
        if (!cleaned.startsWith("[")) {
            // 尝试查找第一个 [
            int startIndex = cleaned.indexOf('[');
            if (startIndex != -1) {
                cleaned = cleaned.substring(startIndex);
            }
        }
        
        return cleaned;
    }

    @Override
    public String getStepName() {
        return "Storyboard Generation";
    }
}
