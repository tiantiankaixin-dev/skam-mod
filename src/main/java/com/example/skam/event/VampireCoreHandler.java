package com.example.skam.event;

import com.example.skam.Skam; // <-- 确保导入了你的主类
import com.example.skam.item.core.CoreType;
import com.example.skam.util.SkamAttributeConfig;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.random.Random;

public class VampireCoreHandler {

    private static final int MAX_VAMPIRE_LEVEL = 5;

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient && entity instanceof LivingEntity && !player.isSpectator()) {
                // [调试日志 1] 检查事件是否被触发
                Skam.LOGGER.info("[VampireCore] Attack event triggered on server side.");
                handleVampireEffect(player, (LivingEntity) entity);
            }
            return ActionResult.PASS;
        });
    }

    private static void handleVampireEffect(PlayerEntity player, LivingEntity target) {
        int rawLevel = findHighestVampireCoreLevel(player);

        int highestLevel = Math.min(rawLevel, MAX_VAMPIRE_LEVEL);

        // [调试日志 2] 检查找到的核心等级
        Skam.LOGGER.info("[VampireCore] Found highest vampire core level: " + highestLevel);

        if (highestLevel <= 0) {
            return; // 玩家没有装备任何吸血核心
        }

        SkamAttributeConfig.CoreAttributes config = SkamAttributeConfig.get().getAttributesFor(CoreType.VAMPIRE);
        double chance = config.vampire_chance_per_level * highestLevel;
        double minHeal = config.vampire_min_heal_per_level * highestLevel;
        double maxHeal = config.vampire_max_heal_per_level * highestLevel;

        // [调试日志 3] 检查计算出的几率和治疗量
        Skam.LOGGER.info(String.format("[VampireCore] Calculated values: Chance=%.2f, MinHeal=%.2f, MaxHeal=%.2f", chance, minHeal, maxHeal));

        if (chance <= 0 || maxHeal <= 0) {
            return; // 配置无效
        }

        Random random = player.getRandom();
        if (random.nextFloat() < chance) {
            float healAmount = (float) (minHeal + random.nextDouble() * (Math.max(0, maxHeal - minHeal)));

            // [调试日志 4] 检查是否成功触发
            Skam.LOGGER.info("[VampireCore] SUCCESS! Healing player for " + healAmount);

            if (healAmount > 0) {
                player.heal(healAmount);
                if (player.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.HEART, player.getX(), player.getY() + player.getHeight() / 2.0, player.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
                }
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 2.0f);
            }
        }
    }

    private static int findHighestVampireCoreLevel(PlayerEntity player) {
        int maxLevel = 0;
        maxLevel = Math.max(maxLevel, getVampireCoreLevel(player.getMainHandStack()));
        maxLevel = Math.max(maxLevel, getVampireCoreLevel(player.getOffHandStack()));
        for (ItemStack armorStack : player.getArmorItems()) {
            maxLevel = Math.max(maxLevel, getVampireCoreLevel(armorStack));
        }
        return maxLevel;
    }

    private static int getVampireCoreLevel(ItemStack stack) {
        if (!stack.hasNbt() || !stack.getNbt().contains("skam_mods", NbtElement.COMPOUND_TYPE)) {
            return 0;
        }
        NbtCompound skamNbt = stack.getNbt().getCompound("skam_mods");
        if (!skamNbt.contains("cores", NbtElement.LIST_TYPE)) {
            return 0;
        }

        NbtList coreList = skamNbt.getList("cores", NbtElement.COMPOUND_TYPE);
        for (NbtElement element : coreList) {
            NbtCompound coreTag = (NbtCompound) element;
            if ("VAMPIRE".equalsIgnoreCase(coreTag.getString("type"))) {
                return coreTag.getInt("level");
            }
        }
        return 0;
    }
}
