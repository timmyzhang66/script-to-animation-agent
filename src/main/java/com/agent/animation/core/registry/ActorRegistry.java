package com.agent.animation.core.registry;

import com.agent.animation.domain.Actor;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 数字演员注册中心
 * 实现“角色固定，剧情不固定”的核心调度逻辑
 */
public class ActorRegistry {
    private final Map<String, Actor> actorMap = new HashMap<>();
    private final Gson gson = new Gson();

    public void loadConfig(String path) throws Exception {
        String content = Files.readString(Paths.get(path));
        JsonObject config = gson.fromJson(content, JsonObject.class);
        JsonArray actors = config.getAsJsonArray("actors");

        for (int i = 0; i < actors.size(); i++) {
            JsonObject obj = actors.get(i).getAsJsonObject();
            Actor actor = new Actor(
                    obj.get("id").getAsString(),
                    obj.get("name").getAsString(),
                    obj.get("visual_anchor").getAsString(),
                    obj.get("fixed_oss_url").getAsString(),
                    obj.get("isMainCharacter").getAsBoolean()
            );
            actorMap.put(actor.getName(), actor);
        }
    }

    /**
     * 根据脚本中的角色名匹配库中的演员
     */
    public Actor matchActor(String scriptName) {
        // 这里可以做模糊匹配，比如脚本里叫“甄嬛”，库里叫“职场甄嬛”
        return actorMap.get(scriptName);
    }
}