package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Character;
import com.agent.animation.service.NanoBananaProService;
import com.agent.animation.service.GeminiTextService;
import com.agent.animation.service.GCSService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色生成工作流步骤
 * 分析脚本中的所有角色并生成角色图像
 */
public class CharacterGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(CharacterGenerationStep.class);
    private final GeminiTextService geminiTextService;
    private final NanoBananaProService nanoBananaProService;
    private final GCSService gcsService;
    private final AppConfig config;
    private final Gson gson;

    public CharacterGenerationStep() throws Exception {
        this.geminiTextService = new GeminiTextService();
        this.nanoBananaProService = new NanoBananaProService();
        this.gcsService = new GCSService();
        this.config = AppConfig.getInstance();
        this.gson = new Gson();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting character generation step");
        
        String scriptContent = context.getScriptInput().getScriptContent();
        
        // 1. 使用 Gemini 3 Flash 分析脚本中的所有角色
        logger.info("Analyzing all characters in the script...");
        String charactersJson = geminiTextService.analyzeCharacters(scriptContent);
        
        logger.debug("Characters JSON: {}", charactersJson);
        
        // 2. 解析 JSON 响应
        JsonObject jsonResponse = gson.fromJson(charactersJson, JsonObject.class);
        JsonArray charactersArray = jsonResponse.getAsJsonArray("characters");
        
        if (charactersArray == null || charactersArray.isEmpty()) {
            throw new Exception("No characters found in the script");
        }
        
        logger.info("Found {} characters in the script", charactersArray.size());
        
        // 3. 为每个角色生成图像
        List<Character> characters = new ArrayList<>();
        
        for (int i = 0; i < charactersArray.size(); i++) {
            JsonObject charJson = charactersArray.get(i).getAsJsonObject();
            
            String name = charJson.get("name").getAsString();
            String description = charJson.get("description").getAsString();
            String role = charJson.get("role").getAsString();
            boolean isMainCharacter = charJson.get("isMainCharacter").getAsBoolean();
            
            logger.info("Generating image for character {}/{}: {} ({})", 
                    i + 1, charactersArray.size(), name, role);
            
            // 创建角色对象
            Character character = new Character(name, description, role, isMainCharacter);
            
            // 生成角色图像
            String characterImagePath = config.getTempDir() + File.separator + 
                    "character_" + sanitizeFilename(name) + ".jpg";
            
            // 增强角色描述
            String enhancedPrompt = buildCharacterPrompt(description, name, role);
            
            // 使用 Nano Banana Pro 生成图像
            String aspectRatio = config.getNanoBananaProAspectRatio();
            String resolution = config.getNanoBananaProResolution();
            
            try {
                String base64Image = nanoBananaProService.generateImage(
                        enhancedPrompt,
                        aspectRatio,
                        resolution
                );
                
                // 保存图像到本地
                nanoBananaProService.saveImageToFile(base64Image, characterImagePath);
                character.setImagePath(characterImagePath);
                
                // 上传到 GCS 并获取 URL
                logger.info("Uploading character image to GCS: {}", name);
                String characterImageUrl = gcsService.uploadFile(characterImagePath, null, false);
                character.setImageUrl(characterImageUrl);
                
                logger.info("Character image generated and uploaded: {}", name);
                logger.info("  Local path: {}", characterImagePath);
                logger.info("  GCS URL: {}", characterImageUrl);
                
                // 添加到角色列表
                characters.add(character);
                
            } catch (Exception e) {
                logger.error("Failed to generate image for character: {}", name, e);
                // 继续处理其他角色，不中断整个流程
            }
        }
        
        if (characters.isEmpty()) {
            throw new Exception("Failed to generate images for all characters");
        }
        
        // 4. 保存所有角色到上下文
        context.setCharacters(characters);
        
        logger.info("Character generation completed for {} characters", characters.size());
        
        // 打印角色摘要
        logger.info("Character Summary:");
        for (Character character : characters) {
            logger.info("  - {} ({}): {}", 
                    character.getName(), 
                    character.getRole(), 
                    character.isMainCharacter() ? "MAIN" : "SUPPORTING");
            logger.info("    Image: {}", character.getImagePath());
            logger.info("    URL: {}", character.getImageUrl());
        }
    }

    /**
     * 构建角色生成的增强提示词
     */
    private String buildCharacterPrompt(String description, String name, String role) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加角色名称和角色类型
        prompt.append("Character: ").append(name).append(" (").append(role).append(")\n\n");
        
        // 添加角色描述
        prompt.append(description);
        
        // 添加质量和风格要求
        prompt.append("\n\nStyle requirements:");
        prompt.append("\n- High-quality character design");
        prompt.append("\n- Professional animation style");
        prompt.append("\n- Clear and detailed features");
        prompt.append("\n- Consistent and memorable appearance");
        prompt.append("\n- Suitable for animation and storytelling");
        prompt.append("\n- Full body or portrait view with clear visibility of key features");
        prompt.append("\n- Neutral background to focus on the character");
        
        return prompt.toString();
    }
    
    /**
     * 清理文件名，移除非法字符
     */
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }

    @Override
    public String getStepName() {
        return "Character Generation (Multi-Character)";
    }
}
