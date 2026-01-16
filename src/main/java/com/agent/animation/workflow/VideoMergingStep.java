package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.service.OSSService;
import com.agent.animation.service.VideoProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class VideoMergingStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(VideoMergingStep.class);
    private final VideoProcessingService videoProcessingService;
    private final OSSService ossService;
    private final AppConfig config;

    public VideoMergingStep() {
        this.videoProcessingService = new VideoProcessingService();
        this.ossService = new OSSService();
        this.config = AppConfig.getInstance();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting video merging step");

        List<String> videoPaths = context.getStoryboard().getScenes().stream()
                .map(s -> s.getVideoPath())
                .filter(path -> path != null)
                .collect(Collectors.toList());

        if (videoPaths.isEmpty()) {
            throw new Exception("No scene videos found to merge");
        }

        String finalVideoPath = config.getTempDir() + File.separator + "final_animation.mp4";

        // 执行 FFmpeg 合并
        videoProcessingService.mergeVideos(videoPaths, finalVideoPath);

        context.setFinalVideoPath(finalVideoPath);

        // 上传最终结果
        String finalUrl = ossService.uploadFile(finalVideoPath, null);
        context.setFinalVideoUrl(finalUrl);

        logger.info("Final merged video ready: {}", finalUrl);
    }

    @Override
    public String getStepName() { return "Final Video Merging"; }
}