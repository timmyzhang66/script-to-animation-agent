package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Character;
import com.agent.animation.dto.Scene;
import com.agent.animation.service.NanoBananaProService;
import com.agent.animation.service.OSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关键帧生成工作流步骤
 * 使用 Nano Banana Pro 为每个场景生成关键帧图像
 * 根据场景涉及的角色，使用相应的参考图像
 */
public class KeyframeGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(KeyframeGenerationStep.class);
    private final NanoBananaProService nanoBananaProService;
    private final OSSService ossService;
    private final AppConfig config;

    public KeyframeGenerationStep() throws Exception {
        this.nanoBananaProService = new NanoBananaProService();
        this.ossService = new OSSService();
        this.config = AppConfig.getInstance();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting keyframe generation step with Nano Banana Pro");
        
        // 为每个场景生成关键帧
        int sceneIndex = 0;
        for (Scene scene : context.getStoryboard().getScenes()) {
            sceneIndex++;
            logger.info("Generating keyframe for scene {}/{}", 
                    sceneIndex, context.getStoryboard().getSceneCount());
            
            String keyframePath = config.getTempDir() + File.separator + 
                    "keyframe_scene_" + scene.getSceneNumber() + ".jpg";
            
            try {
                // 1. 获取场景涉及的角色
                List<String> characterNames = scene.getCharacterNames();
                List<Character> sceneCharacters = context.getCharactersForScene(characterNames);
                
                logger.info("Scene {} involves {} characters: {}", 
                        scene.getSceneNumber(), 
                        sceneCharacters.size(), 
                        characterNames);
                
                // 2. 收集角色参考图像 URL
                List<String> referenceUrls = new ArrayList<>();
                StringBuilder characterDescriptions = new StringBuilder();
                
                for (Character character : sceneCharacters) {
                    if (character.getImageUrl() != null && !character.getImageUrl().isEmpty()) {
                        referenceUrls.add(character.getImageUrl());
                        characterDescriptions.append(character.getName())
                                .append(": ")
                                .append(character.getDescription())
                                .append("\n");
                    }
                }
                
                // 3. 构建增强的提示词
                String visualPrompt = buildEnhancedPrompt(
                        scene.getVisualDescription(), 
                        characterDescriptions.toString(),
                        characterNames
                );
                
                logger.info("Using {} reference images for scene {}", 
                        referenceUrls.size(), scene.getSceneNumber());
                
                // 4. 使用 Nano Banana Pro 生成图像
                String aspectRatio = config.getNanoBananaProAspectRatio();
                String resolution = config.getNanoBananaProResolution();
                
                String base64Image;
                if (!referenceUrls.isEmpty()) {
                    // 使用参考图像生成
                    base64Image = nanoBananaProService.generateImageWithReferences(
                            visualPrompt,
                            referenceUrls,
                            aspectRatio,
                            resolution
                    );
                } else {
                    // 无参考图像生成
                    logger.warn("No reference images available for scene {}", scene.getSceneNumber());
                    base64Image = nanoBananaProService.generateImage(
                            visualPrompt,
                            aspectRatio,
                            resolution
                    );
                }
                
                // 5. 保存图像到本地
                nanoBananaProService.saveImageToFile(base64Image, keyframePath);
                scene.setKeyframePath(keyframePath);
                
                // 6. 上传到 OSS 并获取 URL
                logger.info("Uploading keyframe to OSS for scene {}", scene.getSceneNumber());
                String keyframeUrl = ossService.uploadFile(keyframePath, null);
                scene.setKeyframeUrl(keyframeUrl);
                
                logger.info("Keyframe generated for scene {}", scene.getSceneNumber());
                logger.info("  Local path: {}", keyframePath);
                logger.info("  OSS URL: {}", keyframeUrl);
                
            } catch (Exception e) {
                logger.error("Failed to generate keyframe for scene {}", scene.getSceneNumber(), e);
                throw new Exception("Keyframe generation failed for scene " + scene.getSceneNumber(), e);
            }
        }
        
        logger.info("Keyframe generation completed for all {} scenes", 
                context.getStoryboard().getSceneCount());
    }

    /**
     * 构建增强的提示词，包含角色一致性要求
     */
    private String buildEnhancedPrompt(String visualDescription, String characterDescriptions, List<String> characterNames) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加场景描述
        prompt.append(visualDescription);
        
        // 添加角色一致性要求
        if (characterDescriptions != null && !characterDescriptions.isEmpty()) {
            prompt.append("\n\nCharacters in this scene:\n");
            prompt.append(characterDescriptions);
            prompt.append("\nIMPORTANT: Maintain the same character designs, appearances, and styles as shown in the reference images.");
            prompt.append("\nEnsure all characters (").append(String.join(", ", characterNames))
                    .append(") are clearly visible and recognizable.");
        }
        
        // 添加质量和风格要求
        prompt.append("\n\nStyle requirements:");
        prompt.append("\n- High-quality, cinematic, detailed");
        prompt.append("\n- Professional animation style");
        prompt.append("\n- Consistent lighting, color palette, and composition");
        prompt.append("\n- Clear focus on the main action and characters");
        prompt.append("\n- Suitable for video generation");
        
        return prompt.toString();
    }

    @Override
    public String getStepName() {
        return "Keyframe Generation (Multi-Character)";
    }
}
