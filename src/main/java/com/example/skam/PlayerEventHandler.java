// src/main/java/com/example/skam/PlayerEventHandler.java
package com.example.skam;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEventHandler {

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            if (player != null) {
                WhitePortalBlock.removePlayerFromPortalLogic(player.getUuid());
            }
        });
    }
}
