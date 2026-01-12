package com.agent.animation.workflow;

import com.agent.animation.config.AppConfig;
import com.agent.animation.dto.Scene;
import com.agent.animation.service.VideoProcessingService;
import com.agent.animation.service.GCSService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 视频合并工作流步骤
 * 将所有场景的视频片段合并为最终视频
 */
public class VideoMergingStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(VideoMergingStep.class);
    private final VideoProcessingService videoProcessingService;
    private final GCSService gcsService;
    private final AppConfig config;

    public VideoMergingStep() {
        this.videoProcessingService = new VideoProcessingService();
        this.gcsService = new GCSService();
        this.config = AppConfig.getInstance();
    }

    @Override
    public void execute(WorkflowContext context) throws Exception {
        logger.info("Starting video merging step");
        
        // 收集所有场景的视频路径
        List<String> videoPaths = new ArrayList<>();
        for (Scene scene : context.getStoryboard().getScenes()) {
            String videoPath = scene.getVideoPath();
            if (videoPath == null || videoPath.isEmpty()) {
                throw new Exception("Scene " + scene.getSceneNumber() + " has no video path");
            }
            
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) {
                throw new Exception("Video file does not exist: " + videoPath);
            }
            
            videoPaths.add(videoPath);
            logger.info("Added video for scene {}: {}", scene.getSceneNumber(), videoPath);
        }
        
        // 生成输出文件名
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String outputFileName = "animation_" + timestamp + "." + config.getVideoOutputFormat();
        String outputPath = config.getOutputDir() + File.separator + outputFileName;
        
        // 确保输出目录存在
        new File(config.getOutputDir()).mkdirs();
        
        // 合并视频
        logger.info("Merging {} videos into final output", videoPaths.size());
        videoProcessingService.mergeVideos(videoPaths, outputPath);
        
        // 保存最终视频路径到上下文
        context.setFinalVideoPath(outputPath);
        
        // 上传最终视频到 GCS
        logger.info("Uploading final video to GCS...");
        String finalVideoUrl = gcsService.uploadFile(outputPath, null, false);
        context.setFinalVideoUrl(finalVideoUrl);
        
        logger.info("Video merging completed: {}", outputPath);
        logger.info("Final video URL: {}", finalVideoUrl);
        
        // 输出视频信息
        try {
            String videoInfo = videoProcessingService.getVideoInfo(outputPath);
            logger.info("Final video info:\n{}", videoInfo);
        } catch (Exception e) {
            logger.warn("Failed to get video info", e);
        }
    }

    @Override
    public String getStepName() {
        return "Video Merging";
    }
}
