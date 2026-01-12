package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Character;
import com.agent.animation.service.NanoBananaProService;
import com.agent.animation.service.GeminiTextService;
import com.agent.animation.service.GCSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 角色生成工作流步骤
 * 使用 Gemini 3 Flash 提取角色描述
 * 使用 Nano Banana Pro 生成角色图像
 */
public class CharacterGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(CharacterGenerationStep.class);
    private final GeminiTextService geminiTextService;
    private final NanoBananaProService nanoBananaProService;
    private final GCSService gcsService;
    private final AppConfig config;

    public CharacterGenerationStep() throws Exception {
        this.geminiTextService = new GeminiTextService();
        this.nanoBananaProService = new NanoBananaProService();
        this.gcsService = new GCSService();
        this.config = AppConfig.getInstance();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting character generation step");
        
        String scriptContent = context.getScriptInput().getScriptContent();
        
        // 1. 使用 Gemini 3 Flash 提取主要角色描述
        logger.info("Extracting main character description from script using Gemini 3 Flash");
        String characterDescription = geminiTextService.extractCharacterDescription(scriptContent);
        
        // 2. 创建角色对象
        Character mainCharacter = new Character("Main Character", characterDescription);
        
        // 3. 使用 Nano Banana Pro 生成角色图像
        logger.info("Generating character image with Nano Banana Pro");
        String characterImagePath = config.getTempDir() + File.separator + "character_main.jpg";
        
        // 增强角色描述，添加质量和风格要求
        String enhancedPrompt = buildCharacterPrompt(characterDescription);
        
        // 生成图像
        String aspectRatio = config.getNanoBananaProAspectRatio();
        String resolution = config.getNanoBananaProResolution();
        
        String base64Image = nanoBananaProService.generateImage(
                enhancedPrompt,
                aspectRatio,
                resolution
        );
        
        // 保存图像到本地
        nanoBananaProService.saveImageToFile(base64Image, characterImagePath);
        mainCharacter.setImagePath(characterImagePath);
        
        // 4. 上传到 GCS 并获取 URL（用于后续关键帧生成）
        logger.info("Uploading character image to GCS");
        String characterImageUrl = gcsService.uploadFile(characterImagePath, null, false);
        mainCharacter.setImageUrl(characterImageUrl);
        
        // 5. 保存到上下文
        context.setMainCharacter(mainCharacter);
        
        logger.info("Character generation completed: {}", mainCharacter);
        logger.info("Character image URL: {}", characterImageUrl);
    }

    /**
     * 构建角色生成的增强提示词
     */
    private String buildCharacterPrompt(String characterDescription) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加角色描述
        prompt.append(characterDescription);
        
        // 添加质量和风格要求
        prompt.append("\n\nStyle requirements:");
        prompt.append("\n- High-quality character design");
        prompt.append("\n- Professional animation style");
        prompt.append("\n- Clear and detailed features");
        prompt.append("\n- Consistent and memorable appearance");
        prompt.append("\n- Suitable for animation and storytelling");
        prompt.append("\n- Full body or portrait view with clear visibility of key features");
        
        return prompt.toString();
    }

    @Override
    public String getStepName() {
        return "Character Generation (Nano Banana Pro)";
    }
}
