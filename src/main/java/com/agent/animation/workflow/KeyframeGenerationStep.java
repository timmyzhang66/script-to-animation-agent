package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Character;
import com.agent.animation.dto.Scene;
import com.agent.animation.service.NanoBananaProService;
import com.agent.animation.service.OSSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * 工业化关键帧生成步骤
 * 优先使用 IP 角色的视觉锚点图，仅在无匹配角色时才调用 AI 生成逻辑
 */
public class KeyframeGenerationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(KeyframeGenerationStep.class);
    private final NanoBananaProService nanoBananaProService;
    private final OSSService ossService;
    private final AppConfig config;

    public KeyframeGenerationStep() {
        this.nanoBananaProService = new NanoBananaProService();
        this.ossService = new OSSService();
        this.config = AppConfig.getInstance();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting industrial keyframe generation step");

        for (Scene scene : context.getStoryboard().getScenes()) {
            // 1. 获取场景涉及的角色对象
            List<String> characterNames = scene.getCharacterNames();
            List<Character> sceneCharacters = context.getCharactersForScene(characterNames);

            String keyframePath = config.getTempDir() + File.separator +
                    "keyframe_scene_" + scene.getSceneNumber() + ".jpg";

            // --- 核心工业化逻辑：IP 资产优先 ---
            if (!sceneCharacters.isEmpty()) {
                // 只要场景涉及了固定 IP 角色，直接使用其标准 OSS 图片作为关键帧生成的参考参考点
                Character mainChar = sceneCharacters.get(0);
                logger.info("Scene {}: Matched Industrial IP Character '{}'", scene.getSceneNumber(), mainChar.getName());

                // 直接引用 IP 的视觉锚点图，确保视觉绝对统一
                scene.setKeyframeUrl(mainChar.getImageUrl());
                continue;
            }

            // --- 兜底逻辑：调用 AI 生成（已适配修改后的 NanoBananaProService） ---
            try {
                String visualPrompt = scene.getVisualDescription() + ", cinematic 2D anime style";

                // 适配修改：使用桩服务提供的基础 generateImage 方法
                String base64Image = nanoBananaProService.generateImage(
                        visualPrompt,
                        config.getNanoBananaProAspectRatio(),
                        config.getNanoBananaProResolution()
                );

                nanoBananaProService.saveImageToFile(base64Image, keyframePath);
                scene.setKeyframePath(keyframePath);

                // 上传生成的临时关键帧到 OSS
                String keyframeUrl = ossService.uploadFile(keyframePath, null);
                scene.setKeyframeUrl(keyframeUrl);

            } catch (Exception e) {
                logger.warn("Scene {} keyframe generation skipped: {}", scene.getSceneNumber(), e.getMessage());
            }
        }
    }

    @Override
    public String getStepName() { return "Industrial Keyframe Management"; }
}