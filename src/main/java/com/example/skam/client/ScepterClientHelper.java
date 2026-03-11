package com.example.skam.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

/**
 * 客户端辅助类，用于处理 ScepterOfTheSolarFlareItem 的客户端专属逻辑
 * 这个类只会在客户端被加载
 */
@Environment(EnvType.CLIENT)
public class ScepterClientHelper {
    
    /**
     * 获取客户端世界实例
     * @return 客户端世界，如果不可用则返回 null
     */
    public static World getClientWorld() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null ? client.world : null;
    }
}

