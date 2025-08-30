package com.example.skam.networking.packet;

import com.example.skam.client.CursedTridentAccess;
import com.example.skam.interfaces.IStasisEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.function.Predicate;

public class RetargetTridentC2SPacket {

    private static final int BASE_FLIGHT_DURATION = 5;
    private static final int DURATION_PER_LEVEL = 5;
    private static final double MIN_DISTANCE_SQUARED = 1.0;

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        Vec3d targetPos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());

        server.execute(() -> {
            ServerWorld world = player.getServerWorld();

            // --- 【功能修改】 ---
            // 日志：修改了过滤器（Predicate），使其不仅能识别拥有时停附魔的三叉戟，
            // 日志：也能识别拥有 CursedTrident 附魔的三叉戟。
            // 日志：这样，玩家就可以操控这两种三叉戟了。
            Predicate<TridentEntity> filter = trident -> {
                if (trident.getOwner() != player) return false;

                boolean hasStasis = trident instanceof IStasisEntity stasis && stasis.getStasisLevel() > 0;
                boolean isCursed = trident instanceof CursedTridentAccess cursed && cursed.skam$isCursed();

                return hasStasis || isCursed;
            };

            Optional<? extends TridentEntity> stasisTridentOptional = world.getEntitiesByType(EntityType.TRIDENT, filter)
                    .stream()
                    .findFirst();

            if (stasisTridentOptional.isPresent()) {
                TridentEntity tridentToRedirect = stasisTridentOptional.get();
                // 日志：因为 Mixin 注入了两个接口，所以这里的类型转换是安全的。
                IStasisEntity stasisInterface = (IStasisEntity) tridentToRedirect;
                CursedTridentAccess cursedAccess = (CursedTridentAccess) tridentToRedirect;

                if (tridentToRedirect.getPos().squaredDistanceTo(targetPos) < MIN_DISTANCE_SQUARED) {
                    return;
                }

                tridentToRedirect.setNoGravity(true);
                stasisInterface.setAirborne();
                stasisInterface.resetDealtDamage();
                stasisInterface.setReleasedForLoyalty(false);

                Vec3d velocity = targetPos.subtract(tridentToRedirect.getPos()).normalize();
                tridentToRedirect.setVelocity(velocity.multiply(4.0));

                // --- 【功能修改】 ---
                // 日志：计算飞行时间时，综合考虑时停等级和 Cursed 等级。
                // 日志：取两者中的最大值来决定飞行时间，使其表现更符合预期。
                int stasisLevel = stasisInterface.getStasisLevel();
                int cursedLevel = cursedAccess.skam$getCursedLevel();
                int effectiveLevel = Math.max(stasisLevel, cursedLevel);

                int newFlightDuration = BASE_FLIGHT_DURATION + (effectiveLevel * DURATION_PER_LEVEL);
                stasisInterface.setStasisTicks(newFlightDuration);

                world.playSound(null, tridentToRedirect.getBlockPos(), SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        });
    }
}

