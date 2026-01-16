package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Scene;
import com.agent.animation.service.GeminiService;
import com.agent.animation.service.OSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

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
        logger.info("Starting industrial video generation step");

        for (Scene scene : context.getStoryboard().getScenes()) {
            logger.info("Generating video for scene {}/{} with Action: {}",
                    scene.getSceneNumber(), context.getStoryboard().getSceneCount(), scene.getActionCode());

            String videoPath = config.getTempDir() + File.separator + "video_scene_" + scene.getSceneNumber() + ".mp4";

            try {
                String videoPrompt = buildVideoPrompt(scene);
                String keyframeUrl = scene.getKeyframeUrl() != null ? scene.getKeyframeUrl() : scene.getKeyframePath();

                geminiService.generateVideoFromImageUrl(videoPrompt, keyframeUrl, videoPath);

                scene.setVideoPath(videoPath);
                String videoUrl = ossService.uploadFile(videoPath, null);
                scene.setVideoUrl(videoUrl);

                logger.info("Scene {} Video Ready: {}", scene.getSceneNumber(), videoUrl);
            } catch (Exception e) {
                logger.error("Failed at scene {}", scene.getSceneNumber(), e);
                throw e;
            }
        }
    }

    private String buildVideoPrompt(Scene scene) {
        StringBuilder prompt = new StringBuilder(scene.getVisualDescription());

        // --- 核心：将 ActionCode 转化为 Veo 的物理表演指令 ---
        String code = scene.getActionCode() != null ? scene.getActionCode() : "CALM_REPLY";
        switch (code) {
            case "LOOK_DOWN_CONTEMPT":
                prompt.append(". Extreme close-up on eyes, sharp gaze looking down with total contempt.");
                break;
            case "SMIRK":
                prompt.append(". A subtle, arrogant smirk, corner of the mouth lifting slightly, anime style.");
                break;
            case "ANGRY_SLAM":
                prompt.append(". Violent motion, character slams table with palm, expressive frustration.");
                break;
            case "SHIFTY_EYES":
                prompt.append(". Shifty eyes moving left and right nervously, beads of sweat on forehead.");
                break;
            case "SHOCK":
                prompt.append(". Eyes wide open, mouth agape, frozen in disbelief, dramatic anime expression.");
                break;
            default:
                prompt.append(". Natural facial animation, subtle lip movements for dialogue.");
        }

        if (scene.getDialogue() != null && !scene.getDialogue().isEmpty()) {
            prompt.append(". Dialogue: ").append(scene.getDialogue());
        }

        prompt.append("\n\nStyle: Smooth 2D animation, maintain IP consistency.");
        return prompt.toString();
    }

    @Override
    public String getStepName() { return "Instructional Video Generation"; }
}