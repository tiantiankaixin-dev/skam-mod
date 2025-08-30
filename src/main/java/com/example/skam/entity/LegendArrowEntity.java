package com.example.skam.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class LegendArrowEntity extends PersistentProjectileEntity {
    public LegendArrowEntity(EntityType<? extends LegendArrowEntity> entityType, World world) {
        super(entityType, world);
    }

    public LegendArrowEntity(World world, LivingEntity owner) {
        super(ModEntities.LEGEND_ARROW, owner, world);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        spawnBlackHole();
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        spawnBlackHole();
        this.discard();
    }

    private void spawnBlackHole() {
        if (!getWorld().isClient) {
            LivingEntity owner = (this.getOwner() instanceof LivingEntity) ? (LivingEntity)this.getOwner() : null;
            BlackHoleEntity blackHole = new BlackHoleEntity(ModEntities.BLACK_HOLE, getWorld(), owner);
            blackHole.setPosition(this.getPos());
            getWorld().spawnEntity(blackHole);
            if (getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY(), this.getZ(), 50, 0.5, 0.5, 0.5, 0.0);
            }
            AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(getWorld(), this.getX(), this.getY() + 50, this.getZ());
            if (owner != null) {
                cloud.setOwner(owner);
            }
            cloud.setParticleType(ParticleTypes.SQUID_INK);
            cloud.setRadius(15.0f);
            cloud.setDuration(400);
            cloud.setWaitTime(0);
            cloud.setRadiusGrowth(-cloud.getRadius() / (float)cloud.getDuration());
            getWorld().spawnEntity(cloud);
        }
    }

    @Override
    protected ItemStack asItemStack() {

        return new ItemStack(Items.ARROW);
    }
}
