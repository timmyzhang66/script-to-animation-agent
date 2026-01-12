package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Scene;
import com.agent.animation.service.GeminiService;
import com.agent.animation.service.OSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 视频生成工作流步骤
 * 为每个场景的关键帧生成视频片段
 * 使用 OSS URL 传递关键帧图像，并上传生成的视频到 OSS
 */
public class VideoGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(VideoGenerationStep.class);
    private final GeminiService geminiService;
    private final OSSService ossService;
    private final AppConfig config;

    public VideoGenerationStep() {
        this.geminiService = new GeminiService();
        this.ossService = new OSSService();
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
                // 1. 构建视频生成提示词
                String videoPrompt = buildVideoPrompt(scene, context);
                
                // 2. 使用关键帧 URL 生成视频（而不是本地路径）
                String keyframeUrl = scene.getKeyframeUrl();
                
                if (keyframeUrl == null || keyframeUrl.isEmpty()) {
                    logger.warn("No keyframe URL for scene {}, using local path", scene.getSceneNumber());
                    keyframeUrl = scene.getKeyframePath();
                }
                
                logger.info("Generating video with keyframe URL: {}", keyframeUrl);
                
                // 调用 Veo API 生成视频
                geminiService.generateVideoFromImageUrl(
                        videoPrompt,
                        keyframeUrl,
                        videoPath
                );
                
                scene.setVideoPath(videoPath);
                
                // 3. 上传视频到 GCS
                logger.info("Uploading video to OSS for scene {}", scene.getSceneNumber());
                String videoUrl = ossService.uploadFile(videoPath, null);
                scene.setVideoUrl(videoUrl);
                
                logger.info("Video generated for scene {}", scene.getSceneNumber());
                logger.info("  Local path: {}", videoPath);
                logger.info("  OSS URL: {}", videoUrl);
                
            } catch (Exception e) {
                logger.error("Failed to generate video for scene {}", scene.getSceneNumber(), e);
                throw new Exception("Video generation failed for scene " + scene.getSceneNumber(), e);
            }
        }
        
        logger.info("Video generation completed for all {} scenes", 
                context.getStoryboard().getSceneCount());
    }

    /**
     * 构建视频生成的提示词
     */
    private String buildVideoPrompt(Scene scene, WorkflowContext context) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加场景描述
        prompt.append(scene.getDescription());
        
        // 添加对话
        if (scene.getDialogue() != null && !scene.getDialogue().isEmpty()) {
            prompt.append(". ").append(scene.getDialogue());
        }
        
        // 添加角色信息
        if (!scene.getCharacterNames().isEmpty()) {
            prompt.append("\n\nCharacters: ");
            prompt.append(String.join(", ", scene.getCharacterNames()));
        }
        
        // 添加动画要求
        prompt.append("\n\nAnimation requirements:");
        prompt.append("\n- Smooth and natural motion");
        prompt.append("\n- Maintain character consistency");
        prompt.append("\n- Cinematic quality");
        prompt.append("\n- Clear storytelling");
        
        return prompt.toString();
    }

    @Override
    public String getStepName() {
        return "Video Generation";
    }
}
