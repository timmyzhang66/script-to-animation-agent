package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Scene;
import com.agent.animation.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 关键帧生成工作流步骤
 * 为每个场景生成关键帧图像
 */
public class KeyframeGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(KeyframeGenerationStep.class);
    private final GeminiService geminiService;
    private final AppConfig config;

    public KeyframeGenerationStep() {
        this.geminiService = new GeminiService();
        this.config = AppConfig.getInstance();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting keyframe generation step");
        
        String characterImagePath = context.getMainCharacter().getImagePath();
        
        // 为每个场景生成关键帧
        int sceneIndex = 0;
        for (Scene scene : context.getStoryboard().getScenes()) {
            sceneIndex++;
            logger.info("Generating keyframe for scene {}/{}", 
                    sceneIndex, context.getStoryboard().getSceneCount());
            
            String keyframePath = config.getTempDir() + File.separator + 
                    "keyframe_scene_" + scene.getSceneNumber() + ".jpg";
            
            // 使用视觉描述和角色参考生成关键帧
            String visualPrompt = scene.getVisualDescription();
            
            try {
                // 使用带参考图像的生成方法以保持角色一致性
                geminiService.generateImageWithReference(
                        visualPrompt,
                        characterImagePath,
                        keyframePath
                );
                
                scene.setKeyframePath(keyframePath);
                logger.info("Keyframe generated for scene {}: {}", scene.getSceneNumber(), keyframePath);
                
            } catch (Exception e) {
                logger.error("Failed to generate keyframe for scene {}", scene.getSceneNumber(), e);
                throw new Exception("Keyframe generation failed for scene " + scene.getSceneNumber(), e);
            }
        }
        
        logger.info("Keyframe generation completed for all {} scenes", 
                context.getStoryboard().getSceneCount());
    }

    @Override
    public String getStepName() {
        return "Keyframe Generation";
    }
}
