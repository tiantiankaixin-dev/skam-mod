// src/main/java/com/example/skam/DimensionHopperItem.java
package com.example.skam.item;

import com.example.skam.SkamMod;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap; // <-- 新增的导入
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DimensionHopperItem extends Item {

    private static final Map<UUID, ReturnLocation> RETURN_LOCATIONS = new HashMap<>();

    public DimensionHopperItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {

            MinecraftServer server = world.getServer();
            if (server == null) {
                return TypedActionResult.fail(user.getStackInHand(hand));
            }

            ServerWorld currentWorld = player.getServerWorld();
            RegistryKey<World> targetDimensionKey = SkamMod.BROKEN_CONTINENT_WORLD_KEY;
            ServerWorld targetWorld = server.getWorld(targetDimensionKey);

            if (targetWorld == null) {
                player.sendMessage(Text.literal("目标维度不存在！").formatted(Formatting.RED), false);
                return TypedActionResult.fail(user.getStackInHand(hand));
            }

            if (currentWorld.getRegistryKey() == targetDimensionKey) {
                teleportBack(player);
            } else {
                teleportToTarget(player, targetWorld);
            }

            player.getItemCooldownManager().set(this, 20);
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);

            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    private void teleportToTarget(ServerPlayerEntity player, ServerWorld targetWorld) {
        // 1. 像以前一样，精确记录玩家的出发位置，以便返回
        RETURN_LOCATIONS.put(player.getUuid(), new ReturnLocation(player.getServerWorld().getRegistryKey(), player.getPos()));

        // 2. 获取玩家当前的X和Z坐标
        double sourceX = player.getX();
        double sourceZ = player.getZ();

        // --- 坐标转换逻辑 ---
        // 我们使用1:1的比例。
        double targetX = sourceX;
        double targetZ = sourceZ;

        // 3. 为目标(X, Z)坐标找到一个安全的Y坐标
        int safeY = findSafeLandingY(targetWorld, (int)targetX, (int)targetZ);

        // 4. 将玩家传送到计算出的新坐标
        player.teleport(targetWorld, targetX, safeY, targetZ, player.getYaw(), player.getPitch());

        // 5. 在新的落脚点创建黑曜石平台
        createObsidianPlatform(targetWorld, player.getBlockPos());

        player.sendMessage(Text.literal("已传送到破碎大陆...").formatted(Formatting.LIGHT_PURPLE), true);
    }

    private void teleportBack(ServerPlayerEntity player) {
        ReturnLocation returnLocation = RETURN_LOCATIONS.get(player.getUuid());

        if (returnLocation != null) {
            ServerWorld returnWorld = player.getServer().getWorld(returnLocation.dimension);
            if (returnWorld != null) {
                Vec3d pos = returnLocation.pos;
                player.teleport(returnWorld, pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());
                RETURN_LOCATIONS.remove(player.getUuid());
                player.sendMessage(Text.literal("已返回原世界！").formatted(Formatting.AQUA), true);
            } else {
                teleportToOverworldSpawn(player);
            }
        } else {
            player.sendMessage(Text.literal("未找到返回点，将您送回主世界。").formatted(Formatting.YELLOW), true);
            teleportToOverworldSpawn(player);
        }
    }

    private void teleportToOverworldSpawn(ServerPlayerEntity player) {
        ServerWorld overworld = player.getServer().getOverworld();
        BlockPos spawnPos = overworld.getSpawnPos();
        player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 0, 0);
        RETURN_LOCATIONS.remove(player.getUuid());
    }

    private void createObsidianPlatform(ServerWorld world, BlockPos playerPos) {
        BlockPos centerOfPlatform = playerPos.down();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos blockToPlace = centerOfPlatform.add(dx, 0, dz);
                world.setBlockState(blockToPlace, Blocks.OBSIDIAN.getDefaultState(), 3);
            }
        }
    }

    /**
     * 在指定世界中为给定的X,Z坐标寻找一个安全的着陆Y坐标。
     * 它会找到最高的固体方块，并返回其上方的坐标。
     * @param world 目标世界
     * @param x     目标X坐标
     * @param z     目标Z坐标
     * @return 一个安全的Y坐标，供玩家传送
     */
    private int findSafeLandingY(ServerWorld world, int x, int z) {
        int groundY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (groundY < world.getBottomY()) {
            return world.getSeaLevel() + 1;
        }
        return groundY + 1;
    }

    private record ReturnLocation(RegistryKey<World> dimension, Vec3d pos) {}
}
