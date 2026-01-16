package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Scene;
import com.agent.animation.service.GeminiService;
import com.agent.animation.service.OSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 工业化视频生成步骤
 * 将 AI 导演生成的表演指令（ActionCode）转化为具体的视觉生成指令
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
        logger.info("Starting instructional video generation");

        for (Scene scene : context.getStoryboard().getScenes()) {
            logger.info("Processing Scene {} with Action: {}",
                    scene.getSceneNumber(), scene.getActionCode());

            String videoPath = config.getTempDir() + File.separator + "scene_video_" + scene.getSceneNumber() + ".mp4";

            try {
                // 1. 构建增强型表演提示词
                String videoPrompt = buildVideoPrompt(scene);

                // 2. 调用修正后的 GeminiService
                String keyframeUrl = scene.getKeyframeUrl();
                geminiService.generateVideoFromImageUrl(videoPrompt, keyframeUrl, videoPath);

                // 3. 上传结果
                scene.setVideoPath(videoPath);
                String videoUrl = ossService.uploadFile(videoPath, null);
                scene.setVideoUrl(videoUrl);

                logger.info("Scene {} generation successful: {}", scene.getSceneNumber(), videoUrl);
            } catch (Exception e) {
                logger.error("Failed to generate video for scene {}", scene.getSceneNumber(), e);
                throw e;
            }
        }
    }

    /**
     * 将 ActionCode 翻译为 Veo 能够理解的物理动效描述
     */
    private String buildVideoPrompt(Scene scene) {
        StringBuilder prompt = new StringBuilder(scene.getVisualDescription());
        String code = scene.getActionCode() != null ? scene.getActionCode() : "CALM_REPLY";

        switch (code) {
            case "LOOK_DOWN_CONTEMPT":
                prompt.append(". Extreme close-up on eyes, sharp gaze looking down with total contempt, sharp animation style.");
                break;
            case "SMIRK":
                prompt.append(". A subtle, arrogant smirk on the lips, corner of the mouth lifting, high quality 2D anime.");
                break;
            case "ANGRY_SLAM":
                prompt.append(". Dynamic movement, the character slams the table forcefully, expressive anger.");
                break;
            case "SHIFTY_EYES":
                prompt.append(". Eyes darting around suspiciously, shifty gaze, nervous sweat drops.");
                break;
            case "SHOCK":
                prompt.append(". Dramatic zoom on face, wide eyes, open mouth, expression of complete disbelief.");
                break;
            default:
                prompt.append(". Natural character movement with subtle facial expressions.");
        }

        if (scene.getDialogue() != null && !scene.getDialogue().isEmpty()) {
            prompt.append(". Character says: ").append(scene.getDialogue());
        }

        return prompt.toString();
    }

    @Override
    public String getStepName() { return "Instructional Video Production"; }
}