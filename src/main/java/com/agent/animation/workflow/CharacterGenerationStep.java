package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Character;
import com.agent.animation.service.GeminiService;
import com.agent.animation.service.GeminiTextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 角色生成工作流步骤
 * 从脚本中提取主要角色并生成角色图像
 */
public class CharacterGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(CharacterGenerationStep.class);
    private final GeminiTextService geminiTextService;
    private final GeminiService geminiService;
    private final AppConfig config;

    public CharacterGenerationStep() {
        this.geminiTextService = new GeminiTextService();
        this.geminiService = new GeminiService();
        this.config = AppConfig.getInstance();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting character generation step");
        
        String scriptContent = context.getScriptInput().getScriptContent();
        
        // 1. 使用 Gemini 提取主要角色描述
        logger.info("Extracting main character description from script");
        String characterDescription = geminiTextService.extractCharacterDescription(scriptContent);
        
        // 2. 创建角色对象
        Character mainCharacter = new Character("Main Character", characterDescription);
        
        // 3. 使用 Gemini Imagen 生成角色图像
        logger.info("Generating character image with Gemini Imagen");
        String characterImagePath = config.getTempDir() + File.separator + "character_main.jpg";
        geminiService.generateImage(characterDescription, characterImagePath);
        
        mainCharacter.setImagePath(characterImagePath);
        
        // 4. 保存到上下文
        context.setMainCharacter(mainCharacter);
        
        logger.info("Character generation completed: {}", mainCharacter);
    }

    @Override
    public String getStepName() {
        return "Character Generation";
    }
}
