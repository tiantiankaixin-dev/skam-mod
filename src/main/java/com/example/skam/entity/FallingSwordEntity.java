// src/main/java/com/example/skam/entity/FallingSwordEntity.java
package com.example.skam.entity;

import com.example.skam.util.SkamEnchantConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FallingSwordEntity extends Entity {
    private static final TrackedData<Integer> ENCHANTMENT_LEVEL =
            DataTracker.registerData(FallingSwordEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private LivingEntity owner;
    private int fallTime = 0;

    public FallingSwordEntity(EntityType<?> type, World world) {
        super(type, world);

    }

    public FallingSwordEntity(World world, double x, double y, double z, int enchantmentLevel, LivingEntity owner) {
        this(ModEntities.FALLING_SWORD, world);
        this.setPosition(x, y, z);
        this.setEnchantmentLevel(enchantmentLevel);
        this.owner = owner;
        this.setVelocity(0, -0.1, 0);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ENCHANTMENT_LEVEL, 1);
    }

    public void setEnchantmentLevel(int level) {
        this.dataTracker.set(ENCHANTMENT_LEVEL, level);
    }

    public int getEnchantmentLevel() {
        return this.dataTracker.get(ENCHANTMENT_LEVEL);
    }

    @Override
    public void tick() {
        super.tick();
        fallTime++;

        if (fallTime % 20 == 0) {
        }

        Vec3d velocity = this.getVelocity();
        this.setVelocity(velocity.x, Math.max(velocity.y - 0.006, -2.0), velocity.z);
        this.move(MovementType.SELF, this.getVelocity());
        if (this.isOnGround() || this.horizontalCollision) {
            explode();
            this.discard();
        }
        if (fallTime > SkamEnchantConfig.get().falling_sword.despawn_ticks) {
            this.discard();
        }
    }

    private void explode() {
        if (!this.getWorld().isClient) {
            int level = getEnchantmentLevel();
            SkamEnchantConfig.FallingSwordEnchant config = SkamEnchantConfig.get().falling_sword; // <-- 获取配置部分以简化代码

            // --- 修改: 从配置读取伤害和爆炸威力 ---
            float damage = config.damage_per_level * level;
            float explosionPower = config.explosion_power;
            double damageRadius = config.damage_radius;

            this.getWorld().createExplosion(
                    this.owner, this.getX(), this.getY(), this.getZ(),
                    explosionPower, // <-- 使用配置值
                    false, World.ExplosionSourceType.NONE
            );

            // --- 修改: 从配置读取伤害范围 ---
            this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(damageRadius))
                    .forEach(entity -> {
                        if (entity instanceof LivingEntity && entity != this.owner) {
                            double distance = entity.distanceTo(this);
                            if (distance <= damageRadius) {
                                float actualDamage = damage * (float) (1.0 - distance / damageRadius);
                                entity.damage(this.getDamageSources().explosion(this, this.owner), actualDamage);
                            }
                        }
                    });
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        setEnchantmentLevel(nbt.getInt("EnchantmentLevel"));
        fallTime = nbt.getInt("FallTime");
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("EnchantmentLevel", getEnchantmentLevel());
        nbt.putInt("FallTime", fallTime);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}