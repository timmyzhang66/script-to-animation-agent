package com.agent.animation.service;

import com.agent.animation.config.AppConfig;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gemini 文本服务类
 * 负责调用 Gemini API 进行文本生成和脚本分析
 */
public class GeminiTextService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiTextService.class);
    
    private final AppConfig config;
    private final Client client;
    private final String modelName;

    public GeminiTextService() {
        this.config = AppConfig.getInstance();
        String apiKey = config.getGeminiApiKey();
        this.modelName = config.getTextModel();
        
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable is not set");
        }
        
        // 创建 Gemini Client
        // 支持 Vertex AI Express Mode（使用 API Key 访问 Vertex AI）
        this.client = Client.builder()
                .apiKey(apiKey)
                .vertexAI(true)  // 启用 Vertex AI Express Mode
                .build();
        
        logger.info("GeminiTextService initialized with model: {}", modelName);
    }

    /**
     * 调用 Gemini API 生成文本
     * 
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @return 生成的文本内容
     * @throws Exception 如果 API 调用失败
     */
    public String generateText(String systemPrompt, String userPrompt) throws Exception {
        try {
            logger.info("Generating text with Gemini model: {}", modelName);
            
            // 构建完整的提示词
            String fullPrompt = systemPrompt + "\n\n" + userPrompt;
            
            // 配置生成参数
            GenerateContentConfig contentConfig = GenerateContentConfig.builder()
                    .temperature(0.7f)
                    .topK(40f)
                    .topP(0.95f)
                    .maxOutputTokens(8192)
                    .build();
            
            // 调用 API
            GenerateContentResponse response = client.models.generateContent(
                    modelName, 
                    fullPrompt, 
                    contentConfig
            );
            
            // 提取响应文本
            String responseText = response.text();
            
            if (responseText == null || responseText.trim().isEmpty()) {
                throw new Exception("Empty content returned from Gemini");
            }
            
            logger.info("Text generation completed, length: {} characters", responseText.length());
            return responseText.trim();
            
        } catch (Exception e) {
            logger.error("Gemini API call failed", e);
            throw new Exception("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * 从脚本中提取主要角色描述
     * 
     * @param scriptContent 脚本内容
     * @return 角色描述文本
     * @throws Exception 如果提取失败
     */
    public String extractCharacterDescription(String scriptContent) throws Exception {
        String systemPrompt = "You are an expert in analyzing scripts and extracting character descriptions. " +
                "Your task is to identify the main character from the script and provide a detailed visual description " +
                "that can be used for image generation.";
        
        String userPrompt = "Analyze the following script and extract a detailed description of the MAIN CHARACTER. " +
                "Include physical appearance, clothing, distinctive features, and any visual characteristics mentioned. " +
                "Provide the description in a single paragraph, suitable for image generation.\n\n" +
                "Script:\n" + scriptContent + "\n\n" +
                "Character Description:";
        
        return generateText(systemPrompt, userPrompt);
    }

    /**
     * 生成分镜脚本
     * 
     * @param scriptContent 原始脚本内容
     * @return JSON 格式的分镜数据
     * @throws Exception 如果生成失败
     */
    public String generateStoryboard(String scriptContent) throws Exception {
        String systemPrompt = "You are an expert storyboard artist. Your task is to break down scripts into scenes " +
                "with detailed visual descriptions. Each scene should have a scene number, description, visual details, " +
                "and dialogue. Output ONLY valid JSON format.";
        
        String userPrompt = "Break down the following script into scenes. For each scene, provide:\n" +
                "1. sceneNumber: sequential number starting from 1\n" +
                "2. description: what happens in the scene\n" +
                "3. visualDescription: detailed visual description for image generation (camera angle, lighting, composition, colors, mood)\n" +
                "4. dialogue: any spoken words in the scene\n\n" +
                "Output format (JSON only, no markdown):\n" +
                "{\n" +
                "  \"scenes\": [\n" +
                "    {\n" +
                "      \"sceneNumber\": 1,\n" +
                "      \"description\": \"...\",\n" +
                "      \"visualDescription\": \"...\",\n" +
                "      \"dialogue\": \"...\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "Script:\n" + scriptContent + "\n\n" +
                "Storyboard JSON:";
        
        String response = generateText(systemPrompt, userPrompt);
        
        // 清理响应，移除可能的 markdown 代码块标记
        response = response.trim();
        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }
        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }
        
        return response.trim();
    }

    /**
     * 增强场景的视觉描述
     * 
     * @param sceneDescription 场景描述
     * @param characterDescription 角色描述（用于保持一致性）
     * @return 增强后的视觉描述
     * @throws Exception 如果生成失败
     */
    public String enhanceVisualDescription(String sceneDescription, String characterDescription) throws Exception {
        String systemPrompt = "You are an expert at creating detailed visual descriptions for image generation. " +
                "Your descriptions should be specific, vivid, and suitable for AI image generation models.";
        
        String userPrompt = "Enhance the following scene description with detailed visual elements. " +
                "Include camera angle, lighting, composition, colors, mood, and any relevant details. " +
                "Make sure the main character matches this description: " + characterDescription + "\n\n" +
                "Scene: " + sceneDescription + "\n\n" +
                "Enhanced Visual Description:";
        
        return generateText(systemPrompt, userPrompt);
    }

    /**
     * 关闭服务（清理资源）
     */
    public void close() {
        // Gemini SDK 不需要显式关闭
        logger.info("GeminiTextService closed");
    }
}
