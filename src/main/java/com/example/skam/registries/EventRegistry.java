package com.example.skam.registries;

import com.example.skam.PlayerEventHandler;
import com.example.skam.util.ModUtils;
import com.example.skam.WhitePortalBlock;
import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.event.AttackEntityHandler;
import com.example.skam.event.VampireCoreHandler;
import com.example.skam.event.WorldTickHandler;
import com.example.skam.item.EtherealEyeItem;
import com.example.skam.item.LegendBowItem;
import com.example.skam.item.ModItems;
import com.example.skam.mixin.PersistentProjectileEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

import com.example.skam.effect.DemonKingEffectManager;


public class EventRegistry {

    private static final Map<Enchantment, StatusEffect> CHARM_EFFECTS_MAP = new LinkedHashMap<>();
    private static final Set<UUID> PROCESSED_TRIDENTS = new HashSet<>();

    public static void registerEvents() {
        ServerTickEvents.END_WORLD_TICK.register(LegendBowItem::processAllDomains);
        initializeCharmEffects();
        registerCharmTickEvent();
        registerDemonKingTickEvent();
        registerTridentTickEvent();
        WorldTickHandler.register();
        PlayerEventHandler.register();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            EtherealEyeItem.tick(server);
        });
        AttackEntityCallback.EVENT.register(new AttackEntityHandler());
        VampireCoreHandler.register();

        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof PlayerEntity player) {
                Hand hand = getHandWithTotem(player);
                if (hand != null) {
                    player.setHealth(1.0f);
                    player.clearStatusEffects();
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));
                    player.getStackInHand(hand).decrement(1);
                    player.getWorld().sendEntityStatus(player, (byte) 35);
                    teleportPlayerRandomly(player);
                    return false;
                }
            }
            return true;
        });
        ServerTickEvents.END_SERVER_TICK.register(WhitePortalBlock::portalTickHandler);
    }

    private static Hand getHandWithTotem(PlayerEntity player) {
        if (player.getStackInHand(Hand.MAIN_HAND).isOf(ModItems.TELEPORTING_TOTEM)) {
            return Hand.MAIN_HAND;
        }
        if (player.getStackInHand(Hand.OFF_HAND).isOf(ModItems.TELEPORTING_TOTEM)) {
            return Hand.OFF_HAND;
        }
        return null;
    }

    private static void teleportPlayerRandomly(PlayerEntity player) {
        if (!player.getWorld().isClient()) {
            ServerWorld world = (ServerWorld) player.getWorld();
            for (int i = 0; i < 16; ++i) {
                double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 16.0;
                double y = player.getY() + (player.getRandom().nextInt(16) - 8);
                double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 16.0;
                if (player.hasVehicle()) {
                    player.stopRiding();
                }
                BlockPos targetPos = new BlockPos((int) x, (int) y, (int) z);
                if (world.isChunkLoaded(targetPos) && world.getBlockState(targetPos.down()).isSolid()) {
                    Vec3d targetVec = new Vec3d(x, y, z);
                    if (player.teleport(targetVec.x, targetVec.y, targetVec.z, true)) {
                        player.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                        break;
                    }
                }
            }
        }
    }

    private static void registerTridentTickEvent() {
        ServerTickEvents.END_WORLD_TICK.register(EventRegistry::onTridentTick);
    }

    private static void onTridentTick(ServerWorld world) {
        for (TridentEntity trident : world.getEntitiesByType(EntityType.TRIDENT, (entity) -> true)) {
            PersistentProjectileEntityAccessor persistentAccessor = (PersistentProjectileEntityAccessor) trident;
            if (persistentAccessor.getInGround() && !PROCESSED_TRIDENTS.contains(trident.getUuid())) {
                PROCESSED_TRIDENTS.add(trident.getUuid());
                ModUtils.createExplosiveImpact(trident);
            } else if (!persistentAccessor.getInGround() && PROCESSED_TRIDENTS.contains(trident.getUuid())) {
                PROCESSED_TRIDENTS.remove(trident.getUuid());
            }
        }
        PROCESSED_TRIDENTS.removeIf(uuid -> world.getEntity(uuid) == null);
    }

    private static void initializeCharmEffects() {
        CHARM_EFFECTS_MAP.put(ModEnchantments.POWER_CHARM, StatusEffects.STRENGTH);
        CHARM_EFFECTS_MAP.put(ModEnchantments.SPEED_CHARM, StatusEffects.SPEED);
        CHARM_EFFECTS_MAP.put(ModEnchantments.NIGHT_VISION_CHARM, StatusEffects.NIGHT_VISION);
        CHARM_EFFECTS_MAP.put(ModEnchantments.HASTE_CHARM, StatusEffects.HASTE);
        CHARM_EFFECTS_MAP.put(ModEnchantments.WATER_BREATHING_CHARM, StatusEffects.WATER_BREATHING);
        CHARM_EFFECTS_MAP.put(ModEnchantments.DOLPHINS_GRACE_CHARM, StatusEffects.DOLPHINS_GRACE);
        CHARM_EFFECTS_MAP.put(ModEnchantments.REGENERATION_CHARM, StatusEffects.REGENERATION);
        CHARM_EFFECTS_MAP.put(ModEnchantments.HEALTH_BOOST_CHARM, StatusEffects.HEALTH_BOOST);
        CHARM_EFFECTS_MAP.put(ModEnchantments.ABSORPTION_CHARM, StatusEffects.ABSORPTION);
    }

    private static void registerCharmTickEvent() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                applyAllCharmEffects(player);
            }
        });
    }

    private static void applyAllCharmEffects(ServerPlayerEntity player) {
        if (player == null || !player.isAlive()) {
            return;
        }

        Map<StatusEffect, Integer> requiredEffects = new HashMap<>();

        for (Map.Entry<Enchantment, StatusEffect> entry : CHARM_EFFECTS_MAP.entrySet()) {
            Enchantment charmEnchantment = entry.getKey();
            StatusEffect effect = entry.getValue();

            int highestLevel = 0;
            for (ItemStack armorPiece : player.getArmorItems()) {
                highestLevel = Math.max(highestLevel, EnchantmentHelper.getLevel(charmEnchantment, armorPiece));
            }

            if (highestLevel > 0) {
                int effectAmplifier = (effect == StatusEffects.NIGHT_VISION || effect == StatusEffects.WATER_BREATHING || effect == StatusEffects.DOLPHINS_GRACE) ? 0 : highestLevel - 1;
                requiredEffects.merge(effect, effectAmplifier, Math::max);
            }
        }

        for (Map.Entry<StatusEffect, Integer> requiredEntry : requiredEffects.entrySet()) {
            StatusEffect effect = requiredEntry.getKey();
            int requiredAmplifier = requiredEntry.getValue();
            StatusEffectInstance currentInstance = player.getStatusEffect(effect);

            if (currentInstance == null || currentInstance.getAmplifier() < requiredAmplifier || (currentInstance.getAmplifier() == requiredAmplifier && currentInstance.getDuration() < 20000000)) {
                player.addStatusEffect(new StatusEffectInstance(effect, 99999999, requiredAmplifier, true, false, true));
            }
        }

        Set<StatusEffect> activeCharmEffects = new HashSet<>();
        for (StatusEffect effect : player.getActiveStatusEffects().keySet()) {
            if (CHARM_EFFECTS_MAP.containsValue(effect)) {
                activeCharmEffects.add(effect);
            }
        }

        for (StatusEffect activeEffect : activeCharmEffects) {
            if (!requiredEffects.containsKey(activeEffect)) {
                StatusEffectInstance instance = player.getStatusEffect(activeEffect);
                if (instance != null && instance.getDuration() > 20000000) {
                    player.removeStatusEffect(activeEffect);
                }
            }
        }
    }

    private static void registerDemonKingTickEvent() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            for (ServerPlayerEntity player : world.getPlayers()) {
                DemonKingEffectManager.tickPlayerInventory(player);
            }
        });
    }
}
