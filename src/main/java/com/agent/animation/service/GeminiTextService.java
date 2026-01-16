package com.agent.animation.service;

import com.agent.animation.config.AppConfig;
import com.agent.animation.util.RetryUtils;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeminiTextService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiTextService.class);
    private final AppConfig config;
    private final Client client;
    private final String modelName;

    public GeminiTextService() {
        this.config = AppConfig.getInstance();
        String apiKey = config.getGeminiApiKey();
        this.modelName = config.getTextModel();
        this.client = Client.builder().apiKey(apiKey).vertexAI(true).build();
    }

    // --- 新增：工业化分镜生成方法 ---
    public String generateIndustrialStoryboard(String scriptContent, String actorContext) throws Exception {
        String systemPrompt = "You are a professional Animation Director. Your goal is to convert scripts into instructional storyboards for AI Video Generation.";

        String userPrompt = "### IP Actor Context (Prioritize these characters):\n" + actorContext + "\n\n" +
                "### Script:\n" + scriptContent + "\n\n" +
                "### Task:\n" +
                "Break this into scenes. For each scene, you MUST provide:\n" +
                "1. sceneNumber (int)\n" +
                "2. description (brief summary)\n" +
                "3. visualDescription (cinematic 2D anime style, camera angle)\n" +
                "4. dialogue (Standard format: 'Character says, \"...\"')\n" +
                "5. actionCode: Select ONE from [LOOK_DOWN_CONTEMPT, SMIRK, ANGRY_SLAM, SHIFTY_EYES, SHOCK, CALM_REPLY]\n\n" +
                "Output ONLY valid JSON.";

        String response = generateText(systemPrompt, userPrompt);
        return cleanJsonResponse(response);
    }

    public String generateText(String systemPrompt, String userPrompt) throws Exception {
        return RetryUtils.executeWithRetry(() -> {
            String fullPrompt = systemPrompt + "\n\n" + userPrompt;
            GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                    .temperature(0.7f).maxOutputTokens(8192).build();
            GenerateContentResponse response = client.models.generateContent(modelName, fullPrompt, contentConfig);
            return response.text().trim();
        });
    }

    public String analyzeCharacters(String scriptContent) throws Exception {
        String systemPrompt = "Identify ALL characters and provide detailed visual descriptions for image generation.";
        String userPrompt = "Analyze script:\n" + scriptContent + "\nOutput JSON: {characters: [{name, description, role, isMainCharacter}]}";
        return cleanJsonResponse(generateText(systemPrompt, userPrompt));
    }

    public String analyzeSceneCharacters(String sceneDescription, String allCharacterNames) throws Exception {
        String systemPrompt = "Identify which characters appear in this scene.";
        String userPrompt = "Available characters: " + allCharacterNames + "\nScene: " + sceneDescription + "\nOutput JSON: {characterNames: []}";
        return cleanJsonResponse(generateText(systemPrompt, userPrompt));
    }

    private String cleanJsonResponse(String response) {
        response = response.trim();
        if (response.startsWith("```json")) response = response.substring(7);
        else if (response.startsWith("```")) response = response.substring(3);
        if (response.endsWith("```")) response = response.substring(0, response.length() - 3);
        return response.trim();
    }
}