package com.example.skam;

import com.example.skam.item.TreasureSummonerItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity; // 确保导入这个类
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TimedTreasureMobRule {

    private static int timer = 0;
    // 将间隔改长一点，比如5秒，避免刷屏和过于频繁的检查
    private static final int TRIGGER_INTERVAL_TICKS = 3600;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(TimedTreasureMobRule::onServerTick);
        SkamMod.LOGGER.info("定时宝藏怪物规则已注册。");
    }

    private static void onServerTick(MinecraftServer server) {
        timer++;
        if (timer >= TRIGGER_INTERVAL_TICKS) {
            timer = 0;
            applyRule(server);
        }
    }

    private static void applyRule(MinecraftServer server) {
        List<ServerPlayerEntity> playerList = server.getPlayerManager().getPlayerList();
        if (playerList.isEmpty()) {
            return;
        }
        Collections.shuffle(playerList);
        for (ServerPlayerEntity player : playerList) {
            ServerWorld world = player.getServerWorld();
            Box searchArea = new Box(player.getBlockPos()).expand(64.0);
            List<HostileEntity> nearbyHostileMobs = world.getEntitiesByClass(HostileEntity.class, searchArea, (mob) -> true);

            // ==================== 【关键修改点】 ====================
            // 使用 mob.getTarget() 来判断怪物是否正在以玩家为目标
            List<HostileEntity> eligibleMobs = nearbyHostileMobs.stream()
                    .filter(mob -> {
                        // 条件1: 不能已经是宝藏怪物
                        boolean isNotTreasureMob = mob.getScoreboardTeam() == null || !mob.getScoreboardTeam().getName().startsWith(TreasureSummonerItem.TEAM_NAME_PREFIX);

                        // 条件2 (最终正确逻辑): 怪物的当前目标不能是玩家
                        // mob.getTarget() 返回怪物正在追逐和攻击的实体。
                        // 这是判断其是否“正在与玩家战斗”的最可靠方法。
                        LivingEntity target = mob.getTarget();
                        boolean notTargetingPlayer = !(target instanceof PlayerEntity);

                        return isNotTreasureMob && notTargetingPlayer;
                    })
                    .collect(Collectors.toList());
            // =======================================================


            if (eligibleMobs.isEmpty()) {
                continue;
            }

            HostileEntity targetMob = eligibleMobs.get(world.getRandom().nextInt(eligibleMobs.size()));
            Random random = world.getRandom();

            EntityAttributeInstance maxHealthAttr = targetMob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
            if (maxHealthAttr == null) {
                continue;
            }
            double originalHealth = maxHealthAttr.getBaseValue();

            int initialTier;
            int chance = random.nextInt(100);
            if (chance < 80) {
                initialTier = 1;
            } else if (chance < 95) {
                initialTier = 2;
            } else {
                initialTier = 3;
            }

            int minTierForThisMob = MobConfig.getMinTierForMob(targetMob);
            int finalTier = Math.max(initialTier, minTierForThisMob);

            double healthBonus;
            Identifier lootTableId;
            Formatting glowColor;

            switch (finalTier) {
                case 2:
                    glowColor = Formatting.BLUE;
                    lootTableId = new Identifier(SkamMod.MOD_ID, "entities/treasure_tier_2");
                    healthBonus = 100.0 + random.nextInt(101);
                    targetMob.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 100000 * 20, 4, false, false));
                    break;
                case 3:
                    glowColor = Formatting.RED;
                    lootTableId = new Identifier(SkamMod.MOD_ID, "entities/treasure_tier_3");
                    healthBonus = 200.0 + random.nextInt(301);
                    targetMob.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 1000000 * 20, 14, false, false));
                    break;
                default: // case 1
                    glowColor = Formatting.GREEN;
                    lootTableId = new Identifier(SkamMod.MOD_ID, "entities/treasure_tier_1");
                    healthBonus = 50.0 + random.nextInt(51);
                    break;
            }

            double newTotalHealth = originalHealth + healthBonus;
            maxHealthAttr.setBaseValue(newTotalHealth);
            targetMob.heal((float) newTotalHealth);

            TreasureSummonerItem.equipMobAndSetDrops(world, targetMob, lootTableId);
            targetMob.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, -1, 0, false, false));
            TreasureSummonerItem.setupTreasureTeam(world, targetMob, glowColor);

            Text message = Text.translatable("message.skam.enemy_appeared").formatted(Formatting.RED, Formatting.BOLD);
            world.getPlayers().forEach(p -> {
                if (p.squaredDistanceTo(targetMob) < 64 * 64) {
                    p.sendMessage(message, false);
                }
            });
            world.playSound(null, targetMob.getX(), targetMob.getY(), targetMob.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.HOSTILE, 1.0f, 0.8f);

            SkamMod.LOGGER.info(String.format("成功强化 %s (等级 %d, 配置最低等级 %d -> 最终等级 %d)",
                    targetMob.getType().getUntranslatedName(),
                    initialTier,
                    minTierForThisMob,
                    finalTier
            ));
            return;
        }
    }
}
