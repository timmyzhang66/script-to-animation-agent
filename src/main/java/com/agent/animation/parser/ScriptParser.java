package com.agent.animation.parser;

import com.agent.animation.dto.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 脚本解析器
 * 解析格式化的脚本文件，提取场景和对话
 * 
 * 脚本格式:
 * Scene X:
 * [场景描述]
 * Dialogue:
 * [说话人]: "[对话内容]"
 * [说话人]: "[对话内容]"
 */
public class ScriptParser {
    private static final Logger logger = LoggerFactory.getLogger(ScriptParser.class);
    
    // 场景标题正则: "Scene 1:", "Scene 2:", etc.
    private static final Pattern SCENE_PATTERN = Pattern.compile("^Scene\\s+(\\d+):\\s*$", Pattern.CASE_INSENSITIVE);
    
    // 对话标记正则: "Dialogue:"
    private static final Pattern DIALOGUE_MARKER_PATTERN = Pattern.compile("^Dialogue:\\s*$", Pattern.CASE_INSENSITIVE);
    
    // 对话行正则: "Speaker: "content"" 或 "Speaker："content""
    private static final Pattern DIALOGUE_LINE_PATTERN = Pattern.compile("^([^:：]+)[：:]\\s*[\"\u201c\u201d]([^\"\u201c\u201d]+)[\"\u201c\u201d]\\s*$");
    
    /**
     * 解析脚本内容
     * 
     * @param scriptContent 脚本内容
     * @return 场景列表
     * @throws Exception 解析失败时抛出异常
     */
    public List<Scene> parse(String scriptContent) throws Exception {
        if (scriptContent == null || scriptContent.trim().isEmpty()) {
            throw new Exception("Script content is empty");
        }
        
        List<Scene> scenes = new ArrayList<>();
        String[] lines = scriptContent.split("\\r?\\n");
        
        Scene currentScene = null;
        StringBuilder sceneDescription = new StringBuilder();
        List<String> dialogues = new ArrayList<>();
        boolean inDialogueSection = false;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            
            // 跳过空行
            if (line.isEmpty()) {
                continue;
            }
            
            // 检查是否是场景标题
            Matcher sceneMatcher = SCENE_PATTERN.matcher(line);
            if (sceneMatcher.matches()) {
                // 保存上一个场景
                if (currentScene != null) {
                    finalizeScene(currentScene, sceneDescription.toString(), dialogues);
                    scenes.add(currentScene);
                }
                
                // 开始新场景
                int sceneNumber = Integer.parseInt(sceneMatcher.group(1));
                currentScene = new Scene();
                currentScene.setSceneNumber(sceneNumber);
                sceneDescription = new StringBuilder();
                dialogues = new ArrayList<>();
                inDialogueSection = false;
                
                logger.debug("Found scene: {}", sceneNumber);
                continue;
            }
            
            // 检查是否是对话标记
            Matcher dialogueMarkerMatcher = DIALOGUE_MARKER_PATTERN.matcher(line);
            if (dialogueMarkerMatcher.matches()) {
                inDialogueSection = true;
                logger.debug("Entering dialogue section for scene {}", currentScene != null ? currentScene.getSceneNumber() : "unknown");
                continue;
            }
            
            // 如果在对话部分，解析对话行
            if (inDialogueSection) {
                Matcher dialogueLineMatcher = DIALOGUE_LINE_PATTERN.matcher(line);
                if (dialogueLineMatcher.matches()) {
                    String speaker = dialogueLineMatcher.group(1).trim();
                    String content = dialogueLineMatcher.group(2).trim();
                    String dialogue = speaker + " says, \"" + content + "\"";
                    dialogues.add(dialogue);
                    logger.debug("Found dialogue: {} - {}", speaker, content);
                } else {
                    logger.warn("Invalid dialogue format at line {}: {}", i + 1, line);
                }
            } else {
                // 否则是场景描述
                if (currentScene != null) {
                    if (sceneDescription.length() > 0) {
                        sceneDescription.append(" ");
                    }
                    sceneDescription.append(line);
                }
            }
        }
        
        // 保存最后一个场景
        if (currentScene != null) {
            finalizeScene(currentScene, sceneDescription.toString(), dialogues);
            scenes.add(currentScene);
        }
        
        if (scenes.isEmpty()) {
            throw new Exception("No scenes found in script. Please check the script format.");
        }
        
        logger.info("Parsed {} scenes from script", scenes.size());
        return scenes;
    }
    
    /**
     * 完成场景的设置
     */
    private void finalizeScene(Scene scene, String description, List<String> dialogues) {
        // 设置场景描述
        scene.setVisualDescription(description.trim());
        scene.setDescription(description.trim());
        
        // 合并所有对话
        if (!dialogues.isEmpty()) {
            String combinedDialogue = String.join("\n", dialogues);
            scene.setDialogue(combinedDialogue);
        }
        
        // 提取场景中提到的角色名称（用于后续匹配）
        List<String> characters = extractCharacterNames(description, dialogues);
        scene.setCharacters(characters);
        
        logger.debug("Finalized scene {}: {} characters, {} dialogue lines", 
                scene.getSceneNumber(), characters.size(), dialogues.size());
    }
    
    /**
     * 从场景描述和对话中提取角色名称
     */
    private List<String> extractCharacterNames(String description, List<String> dialogues) {
        List<String> characters = new ArrayList<>();
        
        // 从对话中提取说话人
        for (String dialogue : dialogues) {
            // dialogue 格式: "Speaker says, "content""
            int saysIndex = dialogue.indexOf(" says,");
            if (saysIndex > 0) {
                String speaker = dialogue.substring(0, saysIndex).trim();
                if (!characters.contains(speaker)) {
                    characters.add(speaker);
                }
            }
        }
        
        // 可以扩展：从场景描述中提取角色名称
        // 例如，识别大写的名字或特定模式
        
        return characters;
    }
}
