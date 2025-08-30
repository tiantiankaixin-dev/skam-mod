package com.example.skam.entity;

import com.example.skam.config.BowConfig;
import com.example.skam.item.LegendBowItem;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.List;

public class BlackHoleEntity extends Entity {
    private int life;
    private LivingEntity owner;

    public BlackHoleEntity(EntityType<?> type, World world, LivingEntity owner) {
        super(type, world);
        this.owner = owner;
        this.noClip = true;
        this.life = BowConfig.getInstance().black_hole.lifetime_ticks;
    }

    public BlackHoleEntity(EntityType<?> type, World world) {
        this(type, world, null);
    }
    public void setOwner(LivingEntity owner) { this.owner = owner; }
    public LivingEntity getOwner() { return this.owner; }
    @Override protected void initDataTracker() {}


    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) return;

        if (getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY(), this.getZ(), 5, 0.3, 0.3, 0.3, 0.05);
        }

        if (this.life-- <= 0) {
            triggerFinalEffect();
            this.discard();
            return;
        }
        double radius = BowConfig.getInstance().black_hole.attraction_radius;
        double strength = BowConfig.getInstance().black_hole.attraction_strength;
        Box attractionBox = new Box(this.getPos().subtract(radius, radius, radius), this.getPos().add(radius, radius, radius));
        List<LivingEntity> entities = getWorld().getEntitiesByClass(LivingEntity.class, attractionBox, entity -> !(entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) && entity.isAlive());

        for (Entity entity : entities) {
            Vec3d direction = this.getPos().subtract(entity.getPos()).normalize();
            entity.setVelocity(direction.multiply(strength));
            entity.velocityModified = true;
        }
    }

    private void triggerFinalEffect() {
        // 确保在服务端执行，并获取 owner
        if (!(getWorld() instanceof ServerWorld serverWorld) || this.getOwner() == null) {
            return;
        }
        LivingEntity owner = this.getOwner();
        // 1. 创建火焰领域 (这个是好的，保持不变)
        LegendBowItem.createFireDomain(serverWorld, this.getPos(), owner);
        // 2. 雷霆伤害 (修正部分)
        // 不要使用 EntityType.spawn，因为它无法指定所有者
        // 我们需要手动创建一个雷电实体，并设置它的“引导者”(channeler)
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, serverWorld);
        lightning.setPos(this.getX(), this.getY(), this.getZ());

        // 检查 owner 是否为玩家实体，因为 setChanneler 只接受 ServerPlayerEntity
        if (owner instanceof ServerPlayerEntity) {
            // 如果是，我们就可以安全地进行类型转换并设置
            lightning.setChanneler((ServerPlayerEntity) owner);
        }
        // 如果 owner 不是玩家（比如被一个骷髅发射），那么这道闪电就没有归属，这也是正确的行为。
        serverWorld.spawnEntity(lightning);
        // 3. 爆炸伤害 (这个是好的，保持不变)
        getWorld().createExplosion(
                this,
                getWorld().getDamageSources().explosion(this, owner),
                null,
                this.getPos(),
                BowConfig.getInstance().final_explosion.explosion_power,
                false,
                World.ExplosionSourceType.NONE
        );
        // 4. 最终魔法伤害 (修正部分)
        double damageRadius = BowConfig.getInstance().final_explosion.damage_radius;
        float magicDamage = BowConfig.getInstance().final_explosion.magic_damage;
        if (magicDamage > 0) { // 只有在伤害大于0时才进行计算
            Box damageBox = new Box(this.getPos().subtract(damageRadius, damageRadius, damageRadius), this.getPos().add(damageRadius, damageRadius, damageRadius));
            List<LivingEntity> targets = getWorld().getEntitiesByClass(LivingEntity.class, damageBox, e -> e.isAlive() && e != owner);
            // 创建一个包含攻击者信息的魔法伤害源
            var magicDamageSource = getWorld().getDamageSources().indirectMagic(this, owner); // <<< 关键！
            for (LivingEntity target : targets) {
                // 使用我们新创建的、带有攻击者信息的伤害源
                target.damage(magicDamageSource, magicDamage);
            }
        }
    }

    @Override protected void readCustomDataFromNbt(NbtCompound nbt) {}
    @Override protected void writeCustomDataToNbt(NbtCompound nbt) {}
    @Override public void onSpawnPacket(EntitySpawnS2CPacket packet) { super.onSpawnPacket(packet); }
}
