package com.example.skam.util;

import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.item.ModItems;
import com.example.skam.mixin.TridentEntityAccessor;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ModUtils {

    public static final Logger LOGGER = LoggerFactory.getLogger("skam-utils");

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
}
