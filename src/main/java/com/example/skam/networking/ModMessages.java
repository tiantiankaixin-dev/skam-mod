package com.example.skam.networking;

import com.example.skam.SkamMod;
import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.networking.packet.RetargetTridentC2SPacket;
import com.example.skam.util.IAirJumperState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/**
 * LOG: 模组的网络消息注册中心。
 * LOG: 本次 Bug 修复不涉及此文件。
 * LOG: 它负责将网络数据包ID与处理逻辑关联起来，其本身的功能是正确的。
 */
public class ModMessages {
    public static final Identifier TRIDENT_MOUNT_SYNC_ID = new Identifier("skam", "trident_mount_sync");
    public static final Identifier RETARGET_TRIDENT_ID = new Identifier(SkamMod.MOD_ID, "retarget_trident");
    public static final Identifier AIR_JUMP_ID = new Identifier(SkamMod.MOD_ID, "air_jump");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(RETARGET_TRIDENT_ID, RetargetTridentC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(AIR_JUMP_ID, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> handleAirJump(player));
        });
    }

    public static void registerS2CPackets() {
        // ...
    }

    private static void handleAirJump(ServerPlayerEntity player) {
        int level = EnchantmentHelper.getLevel(ModEnchantments.AIR_JUMPER, player.getEquippedStack(EquipmentSlot.FEET));
        if (level <= 0) return;
        IAirJumperState playerState = (IAirJumperState) player;
        int airJumpsUsed = playerState.getAirJumpsUsed();

        if ((airJumpsUsed < level || level >= 10) && !player.isOnGround()) {
            final double BASE_PLAYER_SPEED = 0.1D;
            double currentSpeedAttribute = player.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            double speedMultiplier = currentSpeedAttribute / BASE_PLAYER_SPEED;
            speedMultiplier = Math.min(speedMultiplier, 3.0D);

            double baseDashStrength = 0.25D;
            double finalDashStrength = baseDashStrength * speedMultiplier;

            Vec3d lookDirection = player.getRotationVector();
            Vec3d horizontalDashDirection = new Vec3d(lookDirection.x, 0, lookDirection.z).normalize();
            double upwardStrength = 0.5D;
            Vec3d currentVelocity = player.getVelocity();

            player.setVelocity(
                    currentVelocity.x * 0.6 + horizontalDashDirection.x * finalDashStrength,
                    upwardStrength,
                    currentVelocity.z * 0.3 + horizontalDashDirection.z * finalDashStrength
            );

            player.velocityModified = true;
            playerState.setAirJumpsUsed(airJumpsUsed + 1);
            playerState.setTicksInAir(0);
        }
    }
}

