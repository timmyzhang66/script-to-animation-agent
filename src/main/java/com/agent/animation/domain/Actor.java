package com.agent.animation.domain;

/**
 * 数字演员领域模型
 * 用于定义具有固定视觉锚点的 IP 形象
 */
public class Actor {
    private String id;              // 唯一标识，如 "Workplace_Zhenhuan"
    private String name;            // 角色名称
    private String visualAnchor;    // 固定的视觉描述词 (Prompt)
    private String fixedOssUrl;     // 存储在 OSS 上的固定参考图链接
    private boolean isMain;         // 是否为主演

    public Actor(String id, String name, String visualAnchor, String fixedOssUrl, boolean isMain) {
        this.id = id;
        this.name = name;
        this.visualAnchor = visualAnchor;
        this.fixedOssUrl = fixedOssUrl;
        this.isMain = isMain;
    }

    // Getter 和 Setter 方法...
    public String getId() { return id; }
    public String getVisualAnchor() { return visualAnchor; }
    public String getFixedOssUrl() { return fixedOssUrl; }
    public String getName() { return name; }
    public boolean isMain() { return isMain; }
}