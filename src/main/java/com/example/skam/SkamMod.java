// src/main/java/com/example/skam/SkamMod.java
package com.example.skam;

import com.example.skam.util.SkamEnchantConfig;
import com.example.skam.loot.ModLootFunctionTypes;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.server.network.ServerPlayerEntity;
import com.example.skam.event.VampireCoreHandler;
import com.example.skam.command.SkamCommands;
import com.example.skam.tooltip.CoreTooltipHandler;
import com.example.skam.util.SkamDamageTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.api.ModInitializer;
import com.example.skam.item.ModItems;
import com.example.skam.util.SkamAttributeConfig;
import net.minecraft.item.ItemStack;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import java.util.UUID;
import com.example.skam.event.AttackEntityHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.Identifier;
import com.example.skam.block.ModBlocks;
import com.example.skam.block.entity.ModBlockEntities;
import com.example.skam.effect.ModEffects;
import com.example.skam.event.WorldTickHandler;
import com.example.skam.item.*;
import com.example.skam.screen.ModScreenHandlers;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.ChunkPos;
import com.example.skam.effect.DemonKingEffectManager;
import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.mixin.TridentEntityAccessor;
import com.example.skam.networking.ModMessages;
import com.example.skam.potion.ModPotions;
import com.example.skam.screen.HandheldDispenserScreenHandler;
import com.example.skam.config.BossConfig;
import com.example.skam.config.BowConfig;
import com.example.skam.config.SkamConfig;
import com.example.skam.effect.ModStatusEffects;
import com.example.skam.entity.ModEntities;
import com.example.skam.mixin.PersistentProjectileEntityAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.joml.Vector3f;
import java.util.*;

public class SkamMod implements ModInitializer {
    public static final String MOD_ID = "skam";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Map<Enchantment, StatusEffect> CHARM_EFFECTS_MAP = new LinkedHashMap<>();
    public static final ScreenHandlerType<HandheldDispenserScreenHandler> HANDHELD_DISPENSER_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(HandheldDispenserScreenHandler::new);
    public static final Identifier REDIRECT_STASIS_TRIDENT_PACKET_ID = new Identifier(MOD_ID, "redirect_stasis_trident");
    private static final Set<UUID> PROCESSED_TRIDENTS = new HashSet<>();
    public static final Item CURSED_TRIDENT_ENTITY_ITEM = new Item(new Item.Settings());
    public static final Item SWORD_SHEATH = registerItem("sword_sheath", new SwordSheathItem(new FabricItemSettings()));
    public static final ScreenHandlerType<SwordSheathScreenHandler> SWORD_SHEATH_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, new Identifier(MOD_ID, "sword_sheath"), new ExtendedScreenHandlerType<>(SwordSheathScreenHandler::new));
    public static final RegistryKey<World> BROKEN_CONTINENT_WORLD_KEY =
            RegistryKey.of(RegistryKeys.WORLD, new Identifier(MOD_ID, "broken_continent"));
    public static final EntityModelLayer FIRE_GOD_ARMOR_LAYER = new EntityModelLayer(new Identifier(MOD_ID, "fire_god_armor"), "main");


    public static final Block WHITE_PORTAL_BLOCK = new WhitePortalBlock(
            FabricBlockSettings.create()
                    .mapColor(MapColor.WHITE)
                    .strength(-1.0F)          // 坚不可摧
                    .luminance(15)            // 发光
                    .noCollision()            // 无碰撞
                    .nonOpaque()              // 非不透明
                    .sounds(BlockSoundGroup.GLASS)
            // .replaceable()         // <--- 删除或注释掉这一行！
    );
    public static final Item WHITE_PORTAL_BLOCK_ITEM = new BlockItem(
            WHITE_PORTAL_BLOCK,
            new Item.Settings()
    );



    @Override
    public void onInitialize() {
        MobConfig.loadConfig();
        BossConfig.loadConfig();
        SkamConfig.loadConfig();
        BowConfig.load();
        ModStatusEffects.registerEffects();
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModItemGroups.registerItemGroups();
        ModEntities.registerEntities();
        ModPotions.registerPotions();
        ModPotions.registerPotionRecipes();
        registerBrewingRecipes();
        ModEnchantments.registerEnchantments();
        ModEntities.registerModEntities();
        ModEntities.registerEntityAttributes();
        ModMessages.registerC2SPackets();
        ServerTickEvents.END_WORLD_TICK.register(LegendBowItem::processAllDomains);
        initializeCharmEffects();
        registerCharmTickEvent();
        registerDemonKingTickEvent();
        registerTridentTickEvent();
        WorldTickHandler.register();
        PlayerEventHandler.register();
        ModEffects.registerEffects();
        ModMessages.registerC2SPackets();
        ModBlockEntities.registerBlockEntities();
        ModScreenHandlers.registerScreenHandlers();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            EtherealEyeItem.tick(server);});
        AttackEntityCallback.EVENT.register(new AttackEntityHandler());
        SkamAttributeConfig.load();
        CommandRegistrationCallback.EVENT.register(SkamCommands::register);
        VampireCoreHandler.register();
        SkamDamageTypes.register();
        CoreTooltipHandler.register();
        ModLootFunctionTypes.register();
        TimedTreasureMobRule.register();




        Registry.register(Registries.SCREEN_HANDLER, new Identifier(MOD_ID, "handheld_dispenser"), HANDHELD_DISPENSER_SCREEN_HANDLER);
        Registry.register(Registries.ITEM, new Identifier("skam", "cursed_trident_entity"), CURSED_TRIDENT_ENTITY_ITEM);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "white_portal_block"), WHITE_PORTAL_BLOCK);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "white_portal_block"), WHITE_PORTAL_BLOCK_ITEM);
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            // 检查实体是否是玩家
            if (entity instanceof PlayerEntity player) {
                // 检查玩家是否持有我们的位移图腾
                Hand hand = getHandWithTotem(player);
                if (hand != null) {
                    // 阻止死亡
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
    private Hand getHandWithTotem(PlayerEntity player) {
        if (player.getStackInHand(Hand.MAIN_HAND).isOf(ModItems.TELEPORTING_TOTEM)) {
            return Hand.MAIN_HAND;
        }
        if (player.getStackInHand(Hand.OFF_HAND).isOf(ModItems.TELEPORTING_TOTEM)) {
            return Hand.OFF_HAND;
        }
        return null;
    }
    private void teleportPlayerRandomly(PlayerEntity player) {
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






    public static boolean isWearingFullIceGodArmor(PlayerEntity player) {
        ItemStack head = player.getInventory().getArmorStack(3);
        ItemStack chest = player.getInventory().getArmorStack(2);
        ItemStack legs = player.getInventory().getArmorStack(1);
        ItemStack feet = player.getInventory().getArmorStack(0);
        return !head.isEmpty() && head.isOf(ModItems.ICE_GOD_HELMET) &&
                !chest.isEmpty() && chest.isOf(ModItems.ICE_GOD_CHESTPLATE) &&
                !legs.isEmpty() && legs.isOf(ModItems.ICE_GOD_LEGGINGS) &&
                !feet.isEmpty() && feet.isOf(ModItems.ICE_GOD_BOOTS);
    }
    public static boolean isWearingFullThunderGodArmor(PlayerEntity player) {
        ItemStack head = player.getInventory().getArmorStack(3);
        ItemStack chest = player.getInventory().getArmorStack(2);
        ItemStack legs = player.getInventory().getArmorStack(1);
        ItemStack feet = player.getInventory().getArmorStack(0);
        return !head.isEmpty() && head.isOf(ModItems.THUNDER_GOD_HELMET) &&
                !chest.isEmpty() && chest.isOf(ModItems.THUNDER_GOD_CHESTPLATE) &&
                !legs.isEmpty() && legs.isOf(ModItems.THUNDER_GOD_LEGGINGS) &&
                !feet.isEmpty() && feet.isOf(ModItems.THUNDER_GOD_BOOTS);
    }


    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(MOD_ID, name), item);
    }

    public static void setChunkForced(ServerWorld world, ChunkPos pos, boolean forced) {
        world.setChunkForced(pos.x, pos.z, forced);
        if (forced) {
            LOGGER.info("Forcing chunk for recallable trident: " + pos);
        } else {
            LOGGER.info("Un-forcing chunk for recalled trident: " + pos);
        }
    }

    public static void createThunderStrike(TridentEntity trident, Vec3d position) {
        World world = trident.getWorld();
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            TridentEntityAccessor tridentAccessor = (TridentEntityAccessor) trident;
            ItemStack tridentStack = tridentAccessor.invokeAsItemStack();
            if (EnchantmentHelper.getLevel(ModEnchantments.THUNDER_CALLER, tridentStack) > 0) {
                SkamEnchantConfig.ThunderCallerEnchant config = SkamEnchantConfig.get().thunder_caller;
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(serverWorld);
                if (lightning != null) {
                    lightning.refreshPositionAfterTeleport(position);
                    lightning.setCosmetic(true);
                    float damage = config.damage;
                    float radius = config.damage_radius;
                    Box damageBox = new Box(position.subtract(radius, radius, radius), position.add(radius, radius, radius));
                    Entity owner = trident.getOwner();
                    List<Entity> entitiesToDamage = world.getOtherEntities(owner, damageBox);
                    for (Entity entity : entitiesToDamage) {
                        if (entity instanceof LivingEntity) {
                            entity.damage(world.getDamageSources().lightningBolt(), damage);
                        }
                    }
                    if (owner instanceof ServerPlayerEntity) {
                        lightning.setChanneler((ServerPlayerEntity) owner);
                    }
                    serverWorld.spawnEntity(lightning);
                }
            }
        }
    }

    public static void createExplosiveImpact(TridentEntity trident) {
        if (trident.getWorld().isClient()) {
            return;
        }

        TridentEntityAccessor tridentAccessor = (TridentEntityAccessor) trident;
        ItemStack tridentStack = tridentAccessor.invokeAsItemStack();
        int level = EnchantmentHelper.getLevel(ModEnchantments.EXPLOSIVE_IMPACT, tridentStack);

        if (level > 0) {
            SkamEnchantConfig.ExplosiveImpactEnchant config = SkamEnchantConfig.get().explosive_impact;
            float damage = level * config.damage_per_level;
            float radius = config.base_radius + level * config.radius_per_level;
            trident.getWorld().createExplosion(
                    trident.getOwner(),
                    trident.getX(),
                    trident.getY(),
                    trident.getZ(),
                    0.0f,
                    false,
                    World.ExplosionSourceType.NONE
            );
            Box explosionBox = trident.getBoundingBox().expand(radius);
            List<LivingEntity> entities = trident.getWorld().getNonSpectatingEntities(LivingEntity.class, explosionBox);
            for (LivingEntity livingEntity : entities) {
                if (livingEntity.isAlive() && livingEntity != trident.getOwner()) {
                    livingEntity.damage(trident.getDamageSources().thrown(trident, trident.getOwner()), damage);
                }
            }
            if (trident.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.FLASH,
                        trident.getX(),
                        trident.getY() + 0.7,
                        trident.getZ(),
                        1, 0, 0, 0, 0
                );
                DustParticleEffect dustOptions = new DustParticleEffect(new Vector3f(0.1f, 0.8f, 0.9f), 1.0f);
                int shockwaveParticles = config.base_shockwave_particles + level * config.shockwave_particles_per_level;
                {
                    for (int i = 0; i < shockwaveParticles; i++) {
                        double angle = 2 * Math.PI * i / shockwaveParticles;
                        double dx = Math.cos(angle) * radius;
                        double dz = Math.sin(angle) * radius;
                        serverWorld.spawnParticles(dustOptions, trident.getX() + dx, trident.getY() + 0.5, trident.getZ() + dz, 1, 0, 0, 0, 0);
                    }
                    int swirlingParticles = config.base_swirling_particles + level * config.swirling_particles_per_level;
                    Random random = serverWorld.getRandom();
                    for (int i = 0; i < swirlingParticles; i++) {
                        double offsetX = (random.nextDouble() - 0.5) * radius * 1.5;
                        double offsetY = random.nextDouble() * 1.5;
                        double offsetZ = (random.nextDouble() - 0.5) * radius * 1.5;
                        serverWorld.spawnParticles(
                                ParticleTypes.WITCH,
                                trident.getX() + offsetX,
                                trident.getY() + offsetY,
                                trident.getZ() + offsetZ,
                                1, 0, 0, 0, 0.05
                        );
                    }
                }
            }
        }
    }

    private void registerTridentTickEvent() {
        ServerTickEvents.END_WORLD_TICK.register(this::onTridentTick);
    }

    private void onTridentTick(ServerWorld world) {
        for (TridentEntity trident : world.getEntitiesByType(EntityType.TRIDENT, (entity) -> true)) {
            PersistentProjectileEntityAccessor persistentAccessor = (PersistentProjectileEntityAccessor) trident;
            if (persistentAccessor.getInGround() && !PROCESSED_TRIDENTS.contains(trident.getUuid())) {
                PROCESSED_TRIDENTS.add(trident.getUuid());
                createExplosiveImpact(trident);
            } else if (!persistentAccessor.getInGround() && PROCESSED_TRIDENTS.contains(trident.getUuid())) {
                PROCESSED_TRIDENTS.remove(trident.getUuid());
            }
        }
        PROCESSED_TRIDENTS.removeIf(uuid -> world.getEntity(uuid) == null);
    }

    private void initializeCharmEffects() {
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

    private void registerCharmTickEvent() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                applyAllCharmEffects(player);
            }
        });
    }


    private void applyAllCharmEffects(ServerPlayerEntity player) {
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

        // 2. 应用或更新魔咒效果
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


    private void registerBrewingRecipes() {
        Potion strongDemonKingPotion = Registry.register(Registries.POTION, new Identifier(MOD_ID, "strong_demon_king_potion"),
                new Potion("demon_king", new StatusEffectInstance(ModStatusEffects.DEMON_KING, 1800, 1)));
        LOGGER.info("Registered brewing recipes for Skam Mod.");
    }

    private void registerDemonKingTickEvent() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {

            for (ServerPlayerEntity player : world.getPlayers()) {
                DemonKingEffectManager.tickPlayerInventory(player);
            }
        });
    }
}
