package com.agent.animation.dto;

/**
 * 角色数据对象
 * 包含角色描述、生成的图像路径和 OSS 链接
 */
public class Character {
    private String name;
    private String description;
    private String imagePath;      // 本地文件路径
    private String imageUrl;       // GCS/OSS URL
    private String role;           // 角色类型（主角、配角等）
    private boolean isMainCharacter; // 是否为主角

    public Character() {
    }

    public Character(String name, String description) {
        this.name = name;
        this.description = description;
        this.isMainCharacter = false;
    }
    
    public Character(String name, String description, String role, boolean isMainCharacter) {
        this.name = name;
        this.description = description;
        this.role = role;
        this.isMainCharacter = isMainCharacter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isMainCharacter() {
        return isMainCharacter;
    }
    
    public void setMainCharacter(boolean mainCharacter) {
        isMainCharacter = mainCharacter;
    }

    @Override
    public String toString() {
        return "Character{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", role='" + role + '\'' +
                ", isMainCharacter=" + isMainCharacter +
                ", imagePath='" + imagePath + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
