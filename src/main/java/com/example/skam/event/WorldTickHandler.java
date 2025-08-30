// src/main/java/com/example/skam/event/WorldTickHandler.java
package com.example.skam.event;

import com.example.skam.effect.MagicCircleEffect;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

public class WorldTickHandler {
    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register((ServerWorld world) -> {
            MagicCircleEffect.tickAll(world);
        });
    }
}