package com.example.skam.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ControlledTridentManager {
   private static final ConcurrentHashMap<UUID, TridentEntity> controlledTridents = new ConcurrentHashMap<>();
    public static void setControlledTrident(PlayerEntity player, TridentEntity trident) {
        controlledTridents.put(player.getUuid(), trident);
    }
    public static TridentEntity getControlledTrident(PlayerEntity player) {
        return controlledTridents.get(player.getUuid());
    }
    public static void removeControlledTrident(PlayerEntity player) {
        controlledTridents.remove(player.getUuid());
    }
    public static void removeControlledTrident(TridentEntity trident) {
       controlledTridents.entrySet().removeIf(entry -> entry.getValue().equals(trident));
    }
}
