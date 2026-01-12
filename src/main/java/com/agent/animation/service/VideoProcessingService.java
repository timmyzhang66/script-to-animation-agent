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
 * 使用 JavaCV (FFmpeg) 进行视频拼接和处理
 */
public class VideoProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(VideoProcessingService.class);
    private final AppConfig config;

    public VideoProcessingService() {
        this.config = AppConfig.getInstance();
        logger.info("VideoProcessingService initialized");
    }

    /**
     * 合并多个视频文件为一个完整视频
     * 
     * @param videoPaths 视频文件路径列表
     * @param outputPath 输出视频路径
     * @return 合并后的视频路径
     * @throws Exception 合并失败时抛出异常
     */
    public String mergeVideos(List<String> videoPaths, String outputPath) throws Exception {
        logger.info("Merging {} videos into: {}", videoPaths.size(), outputPath);
        
        if (videoPaths == null || videoPaths.isEmpty()) {
            throw new IllegalArgumentException("Video paths list is empty");
        }
        
        if (videoPaths.size() == 1) {
            // 只有一个视频，直接复制
            logger.info("Only one video, copying to output");
            copyFile(videoPaths.get(0), outputPath);
            return outputPath;
        }
        
        FFmpegFrameGrabber firstGrabber = null;
        FFmpegFrameRecorder recorder = null;
        
        try {
            // 从第一个视频获取参数
            firstGrabber = new FFmpegFrameGrabber(videoPaths.get(0));
            firstGrabber.start();
            
            int width = firstGrabber.getImageWidth();
            int height = firstGrabber.getImageHeight();
            int audioChannels = firstGrabber.getAudioChannels();
            int sampleRate = firstGrabber.getSampleRate();
            double frameRate = firstGrabber.getFrameRate();
            
            logger.info("Video parameters: {}x{}, {} fps, audio channels: {}, sample rate: {}", 
                    width, height, frameRate, audioChannels, sampleRate);
            
            firstGrabber.stop();
            firstGrabber.release();
            
            // 创建录制器
            recorder = new FFmpegFrameRecorder(outputPath, width, height, audioChannels);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat(config.getVideoOutputFormat());
            recorder.setFrameRate(frameRate);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            
            if (audioChannels > 0) {
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setSampleRate(sampleRate);
            }
            
            recorder.start();
            
            // 逐个处理视频文件
            for (int i = 0; i < videoPaths.size(); i++) {
                String videoPath = videoPaths.get(i);
                logger.info("Processing video {}/{}: {}", i + 1, videoPaths.size(), videoPath);
                
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
            
            logger.info("Video merging completed: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            logger.error("Failed to merge videos", e);
            
            // 清理资源
            if (firstGrabber != null) {
                try { firstGrabber.release(); } catch (Exception ignored) {}
            }
            if (recorder != null) {
                try { recorder.release(); } catch (Exception ignored) {}
            }
            
            throw new Exception("Video merging failed: " + e.getMessage(), e);
        }
    }

    /**
     * 复制文件
     * 
     * @param sourcePath 源文件路径
     * @param destPath 目标文件路径
     * @throws Exception 复制失败时抛出异常
     */
    private void copyFile(String sourcePath, String destPath) throws Exception {
        try {
            File source = new File(sourcePath);
            File dest = new File(destPath);
            
            if (!source.exists()) {
                throw new Exception("Source file does not exist: " + sourcePath);
            }
            
            org.apache.commons.io.FileUtils.copyFile(source, dest);
            logger.info("File copied from {} to {}", sourcePath, destPath);
            
        } catch (Exception e) {
            logger.error("Failed to copy file", e);
            throw new Exception("File copy failed: " + e.getMessage(), e);
        }
    }

    /**
     * 获取视频信息
     * 
     * @param videoPath 视频文件路径
     * @return 视频信息字符串
     * @throws Exception 获取失败时抛出异常
     */
    public String getVideoInfo(String videoPath) throws Exception {
        FFmpegFrameGrabber grabber = null;
        try {
            grabber = new FFmpegFrameGrabber(videoPath);
            grabber.start();
            
            String info = String.format(
                    "Video: %s\nResolution: %dx%d\nFrame Rate: %.2f fps\nDuration: %.2f seconds\nAudio Channels: %d\nSample Rate: %d Hz",
                    videoPath,
                    grabber.getImageWidth(),
                    grabber.getImageHeight(),
                    grabber.getFrameRate(),
                    grabber.getLengthInTime() / 1000000.0,
                    grabber.getAudioChannels(),
                    grabber.getSampleRate()
            );
            
            grabber.stop();
            grabber.release();
            
            return info;
            
        } catch (Exception e) {
            if (grabber != null) {
                try { grabber.release(); } catch (Exception ignored) {}
            }
            throw new Exception("Failed to get video info: " + e.getMessage(), e);
        }
    }
}
