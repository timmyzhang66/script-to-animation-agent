package com.agent.animation.service;

import com.agent.animation.config.AppConfig;
import org.bytedeco.javacv.*;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * 视频处理服务类
 * 使用 JavaCV (FFmpeg) 进行视频拼接，已移除对过时配置项的依赖
 */
public class VideoProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingService.class);
    private final AppConfig config;

    public VideoProcessingService() {
        this.config = AppConfig.getInstance();
        logger.info("VideoProcessingService initialized (Stable Version)");
    }

    public String mergeVideos(List<String> videoPaths, String outputPath) throws Exception {
        logger.info("Merging {} videos into: {}", videoPaths.size(), outputPath);

        if (videoPaths == null || videoPaths.isEmpty()) {
            throw new IllegalArgumentException("Video paths list is empty");
        }

        FFmpegFrameGrabber firstGrabber = null;
        FFmpegFrameRecorder recorder = null;

        try {
            firstGrabber = new FFmpegFrameGrabber(videoPaths.get(0));
            firstGrabber.start();

            int width = firstGrabber.getImageWidth();
            int height = firstGrabber.getImageHeight();
            double frameRate = firstGrabber.getFrameRate();

            // 修正逻辑：不再从 config 读取，直接锁定为 mp4 格式以匹配 Veo 生成物
            recorder = new FFmpegFrameRecorder(outputPath, width, height, firstGrabber.getAudioChannels());
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(frameRate);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

            if (firstGrabber.getAudioChannels() > 0) {
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setSampleRate(firstGrabber.getSampleRate());
            }

            recorder.start();
            firstGrabber.stop();
            firstGrabber.release();

            for (String videoPath : videoPaths) {
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
                grabber.start();
                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    recorder.record(frame);
                }
                grabber.stop();
                grabber.release();
            }

            recorder.stop();
            recorder.release();
            return outputPath;

        } catch (Exception e) {
            logger.error("Video merging failed", e);
            throw e;
        }
    }
}