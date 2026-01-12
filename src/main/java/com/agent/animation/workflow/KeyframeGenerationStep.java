package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Scene;
import com.agent.animation.service.NanoBananaProService;
import com.agent.animation.service.GCSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 关键帧生成工作流步骤
 * 使用 Nano Banana Pro 为每个场景生成关键帧图像
 * 支持参考图像以保持角色一致性
 */
public class KeyframeGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(KeyframeGenerationStep.class);
    private final NanoBananaProService nanoBananaProService;
    private final GCSService gcsService;
    private final AppConfig config;

    public KeyframeGenerationStep() throws Exception {
        this.nanoBananaProService = new NanoBananaProService();
        this.gcsService = new GCSService();
        this.config = AppConfig.getInstance();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting keyframe generation step with Nano Banana Pro");
        
        // 获取主角色图像的 URL
        String characterImageUrl = context.getMainCharacter().getImageUrl();
        
        if (characterImageUrl == null || characterImageUrl.isEmpty()) {
            logger.warn("No character image URL found, generating keyframes without reference");
        } else {
            logger.info("Using character reference image: {}", characterImageUrl);
        }
        
        // 为每个场景生成关键帧
        int sceneIndex = 0;
        for (Scene scene : context.getStoryboard().getScenes()) {
            sceneIndex++;
            logger.info("Generating keyframe for scene {}/{}", 
                    sceneIndex, context.getStoryboard().getSceneCount());
            
            String keyframePath = config.getTempDir() + File.separator + 
                    "keyframe_scene_" + scene.getSceneNumber() + ".jpg";
            
            try {
                // 构建增强的提示词，包含角色一致性要求
                String visualPrompt = buildEnhancedPrompt(scene.getVisualDescription(), 
                        context.getMainCharacter().getDescription());
                
                // 使用 Nano Banana Pro 生成图像
                String aspectRatio = config.getNanoBananaProAspectRatio();
                String resolution = config.getNanoBananaProResolution();
                
                String base64Image;
                if (characterImageUrl != null && !characterImageUrl.isEmpty()) {
                    // 使用参考图像生成
                    List<String> referenceUrls = Arrays.asList(characterImageUrl);
                    base64Image = nanoBananaProService.generateImageWithReferences(
                            visualPrompt,
                            referenceUrls,
                            aspectRatio,
                            resolution
                    );
                } else {
                    // 无参考图像生成
                    base64Image = nanoBananaProService.generateImage(
                            visualPrompt,
                            aspectRatio,
                            resolution
                    );
                }
                
                // 保存图像到本地
                nanoBananaProService.saveImageToFile(base64Image, keyframePath);
                scene.setKeyframePath(keyframePath);
                
                // 上传到 GCS 并获取 URL（用于后续视频生成）
                String keyframeUrl = gcsService.uploadFile(keyframePath, null, false);
                scene.setKeyframeUrl(keyframeUrl);
                
                logger.info("Keyframe generated for scene {}: {}", scene.getSceneNumber(), keyframePath);
                logger.info("Keyframe URL: {}", keyframeUrl);
                
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
    private String buildEnhancedPrompt(String visualDescription, String characterDescription) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加场景描述
        prompt.append(visualDescription);
        
        // 添加角色一致性要求
        if (characterDescription != null && !characterDescription.isEmpty()) {
            prompt.append("\n\nCharacter consistency requirements: ");
            prompt.append(characterDescription);
            prompt.append("\nMaintain the same character design, appearance, and style as shown in the reference image.");
        }
        
        // 添加质量和风格要求
        prompt.append("\n\nStyle: High-quality, cinematic, detailed, professional animation style.");
        prompt.append(" Consistent lighting, color palette, and composition.");
        
        return prompt.toString();
    }

    @Override
    public String getStepName() {
        return "Keyframe Generation (Nano Banana Pro)";
    }
}
