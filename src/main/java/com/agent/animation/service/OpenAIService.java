package com.agent.animation.service;

import com.agent.animation.config.AppConfig;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionMessageParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI 服务类
 * 负责调用 OpenAI API 进行文本生成和脚本分析
 */
public class OpenAIService {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);
    private final OpenAIClient client;
    private final AppConfig config;

    public OpenAIService() {
        this.config = AppConfig.getInstance();
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(config.getOpenAiApiKey())
                .build();
        logger.info("OpenAIService initialized");
    }

    /**
     * 从脚本中提取主要角色信息
     * 
     * @param scriptContent 脚本内容
     * @return 角色描述文本
     * @throws Exception 调用失败时抛出异常
     */
    public String extractMainCharacter(String scriptContent) throws Exception {
        logger.info("Extracting main character from script");
        
        String systemPrompt = "You are an expert script analyzer. Extract the main character's description from the given script. " +
                "Provide a detailed visual description suitable for image generation, including appearance, clothing, age, and distinctive features. " +
                "Format: Return ONLY the character description in plain text, no additional commentary.";
        
        String userPrompt = "Script:\n" + scriptContent + "\n\nExtract the main character's visual description:";
        
        String response = generateCompletion(systemPrompt, userPrompt);
        logger.info("Main character extracted: {}", response.substring(0, Math.min(100, response.length())));
        return response;
    }

    /**
     * 将脚本分解为分镜
     * 
     * @param scriptContent 脚本内容
     * @return 分镜描述列表（JSON 格式）
     * @throws Exception 调用失败时抛出异常
     */
    public String generateStoryboard(String scriptContent) throws Exception {
        logger.info("Generating storyboard from script");
        
        String systemPrompt = "You are an expert storyboard artist. Break down the given script into individual scenes for animation. " +
                "Each scene should include: scene number, description, visual description (for image generation), and dialogue. " +
                "Format: Return a JSON array of scenes with fields: sceneNumber, description, visualDescription, dialogue.";
        
        String userPrompt = "Script:\n" + scriptContent + "\n\nGenerate storyboard scenes in JSON format:";
        
        String response = generateCompletion(systemPrompt, userPrompt);
        logger.info("Storyboard generated with {} characters", response.length());
        return response;
    }

    /**
     * 生成场景的视觉描述（用于图像生成）
     * 
     * @param sceneDescription 场景描述
     * @param characterDescription 角色描述（用于保持一致性）
     * @return 视觉描述文本
     * @throws Exception 调用失败时抛出异常
     */
    public String generateVisualDescription(String sceneDescription, String characterDescription) throws Exception {
        logger.info("Generating visual description for scene");
        
        String systemPrompt = "You are an expert at creating detailed visual descriptions for image generation. " +
                "Given a scene description and character description, create a detailed prompt suitable for AI image generation. " +
                "Include composition, lighting, mood, colors, and ensure character consistency. " +
                "Format: Return ONLY the visual description in plain text, optimized for image generation.";
        
        String userPrompt = String.format(
                "Character: %s\n\nScene: %s\n\nGenerate visual description:",
                characterDescription,
                sceneDescription
        );
        
        String response = generateCompletion(systemPrompt, userPrompt);
        logger.debug("Visual description generated: {}", response.substring(0, Math.min(100, response.length())));
        return response;
    }

    /**
     * 调用 OpenAI API 生成文本补全
     * 
     * @param systemPrompt 系统提示
     * @param userPrompt 用户提示
     * @return 生成的文本
     * @throws Exception 调用失败时抛出异常
     */
    private String generateCompletion(String systemPrompt, String userPrompt) throws Exception {
        try {
            List<ChatCompletionMessageParam> messages = new ArrayList<>();
            messages.add(ChatCompletionMessageParam.ofChatCompletionSystemMessageParam(
                    ChatCompletionMessageParam.ChatCompletionSystemMessageParam.builder()
                            .content(ChatCompletionMessageParam.ChatCompletionSystemMessageParam.Content.ofTextContent(systemPrompt))
                            .build()
            ));
            messages.add(ChatCompletionMessageParam.ofChatCompletionUserMessageParam(
                    ChatCompletionMessageParam.ChatCompletionUserMessageParam.builder()
                            .content(ChatCompletionMessageParam.ChatCompletionUserMessageParam.Content.ofTextContent(userPrompt))
                            .build()
            ));

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(config.getGptModel())
                    .messages(messages)
                    .temperature(0.7)
                    .maxTokens(2000)
                    .build();

            ChatCompletion completion = client.chat().completions().create(params);
            
            if (completion.choices().isEmpty()) {
                throw new Exception("No completion choices returned from OpenAI");
            }

            String content = completion.choices().get(0).message().content().orElse("");
            if (content.isEmpty()) {
                throw new Exception("Empty content returned from OpenAI");
            }

            return content.trim();
            
        } catch (Exception e) {
            logger.error("Failed to generate completion", e);
            throw new Exception("OpenAI API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * 关闭客户端连接
     */
    public void close() {
        // OpenAI 客户端通常不需要显式关闭
        logger.info("OpenAIService closed");
    }
}
