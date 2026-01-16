package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.core.registry.ActorRegistry;
import com.agent.animation.domain.Actor;
import com.agent.animation.dto.Character;
import com.agent.animation.service.NanoBananaProService;
import com.agent.animation.service.GeminiTextService;
import com.agent.animation.service.OSSService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 工业化角色生成工作流步骤
 * 优先从数字演员库匹配固定 IP，匹配失败时才调用 AI 生成新形象
 */
public class CharacterGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(CharacterGenerationStep.class);
    private final GeminiTextService geminiTextService;
    private final NanoBananaProService nanoBananaProService;
    private final OSSService ossService;
    private final AppConfig config;
    private final Gson gson;

    public CharacterGenerationStep() throws Exception {
        this.geminiTextService = new GeminiTextService();
        this.nanoBananaProService = new NanoBananaProService();
        this.ossService = new OSSService();
        this.config = AppConfig.getInstance();
        this.gson = new Gson();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting industrial character generation step");

        // 初始化并加载数字演员注册表
        ActorRegistry registry = new ActorRegistry();
        // 建议：actors.json 的路径可以从 AppConfig 中动态获取
        registry.loadConfig("src/main/resources/actors.json");

        String scriptContent = context.getScriptInput().getScriptContent();

        // 1. 使用 Gemini 分析脚本中的所有角色
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

        // 3. 角色处理逻辑（匹配或生成）
        List<Character> characters = new ArrayList<>();

        for (int i = 0; i < charactersArray.size(); i++) {
            JsonObject charJson = charactersArray.get(i).getAsJsonObject();
            String name = charJson.get("name").getAsString();
            String description = charJson.get("description").getAsString();
            String role = charJson.get("role").getAsString();
            boolean isMainCharacter = charJson.get("isMainCharacter").getAsBoolean();

            logger.info("Processing character {}/{}: {} ({})", i + 1, charactersArray.size(), name, role);

            // --- 核心工业化逻辑：尝试从库中匹配固定演员 ---
            Actor actor = registry.matchActor(name);
            Character character;

            if (actor != null) {
                logger.info("Industrial Match SUCCESS: Detected persistent IP for '{}'", name);
                // 匹配成功：直接引用库中定义的固定描述和 OSS 锚点图
                character = new Character(actor.getName(), actor.getVisualAnchor(), "Persistent_IP", actor.isMain());
                character.setImageUrl(actor.getFixedOssUrl());
            } else {
                logger.info("No persistent IP found for '{}', generating new image...", name);
                // 匹配失败：调用封装好的生成逻辑
                character = generateNewCharacterImage(name, description, role, isMainCharacter);
            }

            if (character != null) {
                characters.add(character);
            }
        }

        if (characters.isEmpty()) {
            throw new Exception("Failed to generate or map any characters");
        }

        // 4. 保存所有角色到上下文
        context.setCharacters(characters);
        logger.info("Industrial character processing completed for {} characters", characters.size());

        // 打印结果摘要
        for (Character c : characters) {
            logger.info("Character Ready - Name: {}, Source: {}, URL: {}",
                    c.getName(), c.getRole(), c.getImageUrl());
        }
    }

    /**
     * 封装原来的生成逻辑：调用 AI 模型生成全新的角色形象并上传
     */
    private Character generateNewCharacterImage(String name, String description, String role, boolean isMainCharacter) {
        try {
            Character character = new Character(name, description, role, isMainCharacter);

            // 准备本地存储路径
            String characterImagePath = config.getTempDir() + File.separator +
                    "gen_character_" + sanitizeFilename(name) + ".jpg";

            // 增强 Prompt
            String enhancedPrompt = buildCharacterPrompt(description, name, role);

            // 调用图像生成服务 (Gemini 2.5 Flash Image / Nano Banana Pro)
            String base64Image = nanoBananaProService.generateImage(
                    enhancedPrompt,
                    config.getNanoBananaProAspectRatio(),
                    config.getNanoBananaProResolution()
            );

            // 保存并上传资产
            nanoBananaProService.saveImageToFile(base64Image, characterImagePath);
            character.setImagePath(characterImagePath);

            logger.info("Uploading generated character image to OSS: {}", name);
            String characterImageUrl = ossService.uploadFile(characterImagePath, null);
            character.setImageUrl(characterImageUrl);

            return character;
        } catch (Exception e) {
            logger.error("Failed to generate new AI image for character: {}", name, e);
            return null; // 返回 null 允许工作流尝试继续处理其他角色
        }
    }

    /**
     * 构建角色生成的增强提示词
     */
    private String buildCharacterPrompt(String description, String name, String role) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Character: ").append(name).append(" (").append(role).append(")\n\n");
        prompt.append(description);
        prompt.append("\n\nStyle requirements:");
        prompt.append("\n- High-quality character design, Professional animation style");
        prompt.append("\n- Clear features, Consistent appearance, Neutral background");
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
        return "Industrial Character Generation";
    }
}