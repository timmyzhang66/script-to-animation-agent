package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Scene;
import com.agent.animation.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 视频生成工作流步骤
 * 为每个场景的关键帧生成视频片段
 */
public class VideoGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(VideoGenerationStep.class);
    private final GeminiService geminiService;
    private final AppConfig config;

    public VideoGenerationStep() {
        this.geminiService = new GeminiService();
        this.config = AppConfig.getInstance();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting video generation step");
        
        // 为每个场景生成视频
        int sceneIndex = 0;
        for (Scene scene : context.getStoryboard().getScenes()) {
            sceneIndex++;
            logger.info("Generating video for scene {}/{}", 
                    sceneIndex, context.getStoryboard().getSceneCount());
            
            String videoPath = config.getTempDir() + File.separator + 
                    "video_scene_" + scene.getSceneNumber() + ".mp4";
            
            try {
                // 使用关键帧图像和场景描述生成视频
                String videoPrompt = scene.getDescription();
                if (scene.getDialogue() != null && !scene.getDialogue().isEmpty()) {
                    videoPrompt += ". " + scene.getDialogue();
                }
                
                geminiService.generateVideoFromImage(
                        videoPrompt,
                        scene.getKeyframePath(),
                        videoPath
                );
                
                scene.setVideoPath(videoPath);
                logger.info("Video generated for scene {}: {}", scene.getSceneNumber(), videoPath);
                
            } catch (Exception e) {
                logger.error("Failed to generate video for scene {}", scene.getSceneNumber(), e);
                throw new Exception("Video generation failed for scene " + scene.getSceneNumber(), e);
            }
        }
        
        logger.info("Video generation completed for all {} scenes", 
                context.getStoryboard().getSceneCount());
    }

    @Override
    public String getStepName() {
        return "Video Generation";
    }
}
