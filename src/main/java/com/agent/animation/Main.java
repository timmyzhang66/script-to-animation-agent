package com.agent.animation;

import com.agent.animation.agent.AnimationAgent;
import com.agent.animation.dto.ScriptInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 主程序入口
 * 提供命令行界面用于脚本到动画的生成
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // 在程序启动时配置 Jackson 限制（必须在任何 Jackson 使用之前）
        configureJacksonLimits();
        
        logger.info("Script to Animation Agent - Starting");
        
        try {
            // 检查环境变量
            checkEnvironmentVariables();
            
            // 获取脚本输入
            ScriptInput scriptInput = getScriptInput(args);
            
            if (scriptInput == null || scriptInput.getScriptContent() == null || 
                    scriptInput.getScriptContent().trim().isEmpty()) {
                logger.error("No script content provided");
                printUsage();
                System.exit(1);
            }
            
            // 创建 Agent 并生成动画
            logger.info("Initializing Animation Agent...");
            AnimationAgent agent = new AnimationAgent();
            
            logger.info("Starting animation generation...");
            String outputVideoPath = agent.generateAnimation(scriptInput);
            
            if (outputVideoPath != null) {
                System.out.println("\n========================================");
                System.out.println("Animation generation completed successfully!");
                System.out.println("Output video: " + outputVideoPath);
                System.out.println("========================================\n");
            } else {
                System.err.println("\n========================================");
                System.err.println("Animation generation failed!");
                System.err.println("Please check the logs for details.");
                System.err.println("========================================\n");
                System.exit(1);
            }
            
        } catch (Exception e) {
            logger.error("Fatal error in main", e);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 配置 Jackson 字符串长度限制
     * 必须在程序启动时调用，在任何 Jackson 使用之前
     */
    private static void configureJacksonLimits() {
        try {
            // 使用 Jackson 2.15.2+ 的静态方法覆盖默认限制
            // 100MB = 100 * 1024 * 1024 = 104857600 字节
            Class<?> constraintsClass = Class.forName("com.fasterxml.jackson.core.StreamReadConstraints");
            Class<?> builderClass = Class.forName("com.fasterxml.jackson.core.StreamReadConstraints$Builder");
            
            // 调用 StreamReadConstraints.builder()
            Object builder = constraintsClass.getMethod("builder").invoke(null);
            
            // 调用 builder.maxStringLength(104857600)
            builder = builderClass.getMethod("maxStringLength", int.class).invoke(builder, 104857600);
            
            // 调用 builder.build()
            Object constraints = builderClass.getMethod("build").invoke(builder);
            
            // 调用 StreamReadConstraints.overrideDefaultStreamReadConstraints(constraints)
            constraintsClass.getMethod("overrideDefaultStreamReadConstraints", constraintsClass)
                    .invoke(null, constraints);
            
            logger.info("Jackson string length limit configured to 100MB using static method");
        } catch (Exception e) {
            logger.warn("Failed to configure Jackson limits using static method: {}", e.getMessage());
            logger.warn("Falling back to system property (may not work with all Jackson versions)");
            System.setProperty("com.fasterxml.jackson.core.StreamReadConstraints.maxStringLength", "104857600");
        }
    }
    
    /**
     * 检查必要的环境变量
     */
    private static void checkEnvironmentVariables() {
        String geminiKey = System.getenv("GEMINI_API_KEY");
        
        if (geminiKey == null || geminiKey.trim().isEmpty()) {
            logger.warn("GEMINI_API_KEY environment variable is not set");
            System.err.println("Warning: GEMINI_API_KEY is not set. All AI operations will fail.");
        }
    }

    /**
     * 从命令行参数或交互式输入获取脚本
     * 
     * @param args 命令行参数
     * @return 脚本输入对象
     */
    private static ScriptInput getScriptInput(String[] args) throws Exception {
        ScriptInput scriptInput = new ScriptInput();
        
        // 从命令行参数读取
        if (args.length > 0) {
            int i = 0;
            while (i < args.length) {
                String arg = args[i];
                
                if (arg.equals("-f") || arg.equals("--file")) {
                    // 从文件读取脚本
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("File path not provided for -f option");
                    }
                    String filePath = args[i + 1];
                    logger.info("Reading script from file: {}", filePath);
                    String content = new String(Files.readAllBytes(Paths.get(filePath)));
                    scriptInput.setScriptContent(content);
                    scriptInput.setTitle(Paths.get(filePath).getFileName().toString());
                    i += 2;
                } else if (arg.equals("-c") || arg.equals("--characters")) {
                    // 从文件读取角色定义
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("Characters file path not provided for -c option");
                    }
                    String charactersFilePath = args[i + 1];
                    logger.info("Using characters file: {}", charactersFilePath);
                    scriptInput.setCharactersFilePath(charactersFilePath);
                    i += 2;
                } else if (arg.equals("-h") || arg.equals("--help")) {
                    printUsage();
                    System.exit(0);
                } else {
                    // 直接作为脚本内容
                    scriptInput.setScriptContent(arg);
                    i++;
                }
            }
            
            // 验证必须有脚本内容
            if (scriptInput.getScriptContent() == null || scriptInput.getScriptContent().trim().isEmpty()) {
                throw new IllegalArgumentException("No script content provided. Use -f to specify a script file.");
            }
        } else {
            // 交互式输入
            scriptInput = readScriptInteractively();
        }
        
        return scriptInput;
    }

    /**
     * 交互式读取脚本
     * 
     * @return 脚本输入对象
     */
    private static ScriptInput readScriptInteractively() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("\n========================================");
        System.out.println("Script to Animation Agent");
        System.out.println("========================================\n");
        
        System.out.print("Enter script title (optional): ");
        String title = reader.readLine();
        
        System.out.print("Enter script description (optional): ");
        String description = reader.readLine();
        
        System.out.println("\nEnter your script content (end with a line containing only 'END'):");
        StringBuilder scriptContent = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().equals("END")) {
                break;
            }
            scriptContent.append(line).append("\n");
        }
        
        ScriptInput scriptInput = new ScriptInput(
                scriptContent.toString().trim(),
                title.trim().isEmpty() ? null : title.trim(),
                description.trim().isEmpty() ? null : description.trim()
        );
        
        return scriptInput;
    }

    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("\nUsage:");
        System.out.println("  java -jar script-to-animation-agent.jar [options]");
        System.out.println("\nOptions:");
        System.out.println("  -f, --file <path>        Read script from file");
        System.out.println("  -c, --characters <path>  Read character definitions from JSON file (optional)");
        System.out.println("  -h, --help               Show this help message");
        System.out.println("  <script>                 Provide script content directly");
        System.out.println("\nIf no arguments provided, interactive mode will start.");
        System.out.println("\nEnvironment Variables:");
        System.out.println("  GEMINI_API_KEY           API key for Google Gemini (required)");
        System.out.println("\nExamples:");
        System.out.println("  # Use AI to analyze characters from script");
        System.out.println("  java -jar script-to-animation-agent.jar -f my_script.txt");
        System.out.println("");
        System.out.println("  # Use predefined characters from JSON file");
        System.out.println("  java -jar script-to-animation-agent.jar -f my_script.txt -c characters.json");
        System.out.println();
    }
}
