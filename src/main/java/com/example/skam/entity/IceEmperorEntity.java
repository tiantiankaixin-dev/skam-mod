package com.example.skam.entity;

import net.minecraft.entity.mob.HostileEntity;
import com.example.skam.item.ModItems;
import com.example.skam.config.BossConfig;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IceEmperorEntity extends ZombieEntity {

    private final ServerBossBar bossBar;
    private static final UUID FROST_CIRCLE_ARMOR_ID = UUID.fromString("e5b5e5a0-28e2-4c28-9a64-0a37c35593a2");
    private boolean frostMagicCircleApplied = false;
    private int iceShardsCooldown = 0;
    private int iceShardsTick = 0;

    private int glacialPrisonCooldown = 0;
    private final List<BlockPos> prisonBlocks = new ArrayList<>();
    private int prisonClearTick = 0;

    private boolean blizzardActive = false;
    private int blizzardCooldown = 0;
    private int blizzardTick = 0;


    public IceEmperorEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.skam.ice_emperor"),
                BossBar.Color.BLUE,
                BossBar.Style.PROGRESS
        );
        this.experiencePoints = 500;
    }

    public static DefaultAttributeContainer.Builder createIceEmperorAttributes() {
        BossConfig.BossSettings.IceEmperorSettings config = BossConfig.bossSettings.iceEmperor;
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, config.health)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, config.attackDamage)
                .add(EntityAttributes.GENERIC_ARMOR, config.armor)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, config.followRange)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, config.movementSpeed);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        if (this.getWorld().isClient) return;
        if (!frostMagicCircleApplied) {
            applyFrostMagicCirclePassive();
            frostMagicCircleApplied = true;
        }
        handleFrostMagicCircleParticles();
        if (iceShardsCooldown > 0) iceShardsCooldown--;
        if (glacialPrisonCooldown > 0) glacialPrisonCooldown--;
        if (blizzardCooldown > 0) blizzardCooldown--;
        handleGlacialPrisonClearing();
        handleBlizzardEffect();

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        handleIceShards();
        handleGlacialPrison();
        handleBlizzard();
    }

    private void applyFrostMagicCirclePassive() {
        BossConfig.BossSettings.IceEmperorSettings.FrostMagicCircleSettings config = BossConfig.bossSettings.iceEmperor.frostMagicCircle;
        EntityAttributeInstance attribute = this.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
        if (attribute != null) {
            EntityAttributeModifier modifier = new EntityAttributeModifier(
                    FROST_CIRCLE_ARMOR_ID,
                    "Frost Circle Buff",
                    config.additionalArmor,
                    EntityAttributeModifier.Operation.ADDITION
            );
            attribute.removeModifier(FROST_CIRCLE_ARMOR_ID);
            attribute.addPersistentModifier(modifier);
        }
    }

    private void handleFrostMagicCircleParticles() {
        if (this.age % 4 != 0) return;
        BossConfig.BossSettings.IceEmperorSettings.FrostMagicCircleSettings config = BossConfig.bossSettings.iceEmperor.frostMagicCircle;
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            double radius = config.circleRadius;
            for (int i = 0; i < 360; i += 20) {
                double angle = Math.toRadians(i);
                double xOffset = Math.cos(angle) * radius;
                double zOffset = Math.sin(angle) * radius;
                serverWorld.spawnParticles(ParticleTypes.END_ROD,
                        this.getX() + xOffset, this.getY() + 0.2, this.getZ() + zOffset,
                        1, 0, 0, 0, 0.0);
            }
        }
    }

    private void handleIceShards() {
        BossConfig.BossSettings.IceEmperorSettings.IceShardsSettings config = BossConfig.bossSettings.iceEmperor.iceShards;
        if (iceShardsCooldown <= 0 && this.getTarget() != null) {
            iceShardsCooldown = config.cooldown;
            iceShardsTick = config.shardCount * config.fireRate;
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.HOSTILE, 1.5f, 1.2f);
        }

        if (iceShardsTick > 0) {
            iceShardsTick--;
            if (iceShardsTick % config.fireRate == 0 && this.getTarget() != null) {
                LivingEntity target = this.getTarget();
                Vec3d eyePos = this.getEyePos();
                Vec3d targetPos = target.getEyePos();
                Vec3d direction = targetPos.subtract(eyePos).normalize().multiply(0.8);

                for (double i = 1; i < eyePos.distanceTo(targetPos); i += 0.5) {
                    Vec3d particlePos = eyePos.add(direction.multiply(i));
                    ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.SNOWFLAKE, particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0);
                }
                target.damage(this.getDamageSources().magic(), config.damage);
                this.getWorld().playSound(null, target.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 1.0f, 1.0f);
            }
        }
    }

    private void handleGlacialPrison() {
        BossConfig.BossSettings.IceEmperorSettings.GlacialPrisonSettings config = BossConfig.bossSettings.iceEmperor.glacialPrison;
        if (glacialPrisonCooldown <= 0 && this.getTarget() != null) {
            glacialPrisonCooldown = config.cooldown;
            LivingEntity target = this.getTarget();
            BlockPos center = target.getBlockPos();

            this.getWorld().playSound(null, center, SoundEvents.BLOCK_GLASS_PLACE, SoundCategory.HOSTILE, 2.0f, 0.7f);

            for (int x = -config.prisonRadius; x <= config.prisonRadius; x++) {
                for (int y = -1; y <= 3; y++) {
                    for (int z = -config.prisonRadius; z <= config.prisonRadius; z++) {
                        if (y == -1 || y == 3 || Math.abs(x) == config.prisonRadius || Math.abs(z) == config.prisonRadius) {
                            BlockPos blockPos = center.add(x, y, z);
                            if (this.getWorld().getBlockState(blockPos).isAir()) {
                                this.getWorld().setBlockState(blockPos, Blocks.ICE.getDefaultState());
                                prisonBlocks.add(blockPos);
                            }
                        }
                    }
                }
            }
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, config.slownessDuration, config.slownessLevel));
            prisonClearTick = this.age + config.prisonDuration;
        }
    }

    private void handleGlacialPrisonClearing() {
        if (!prisonBlocks.isEmpty() && this.age >= prisonClearTick) {
            clearGlacialPrison();
        }
    }

    private void clearGlacialPrison() {
        for (BlockPos pos : prisonBlocks) {
            if (this.getWorld().getBlockState(pos).isOf(Blocks.ICE)) {
                this.getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
                ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.POOF, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, 10, 0.2, 0.2, 0.2, 0);
            }
        }
        prisonBlocks.clear();
    }

    private void handleBlizzard() {
        BossConfig.BossSettings.IceEmperorSettings.BlizzardSettings config = BossConfig.bossSettings.iceEmperor.blizzard;
        if (!blizzardActive && blizzardCooldown <= 0 && this.getHealth() <= config.triggerHealthThreshold) {
            blizzardActive = true;
            blizzardCooldown = config.cooldown + config.duration;
            blizzardTick = config.duration;
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.HOSTILE, 2.0f, 1.5f);
        }
    }

    private void handleBlizzardEffect() {
        if (blizzardActive) {
            blizzardTick--;
            if (blizzardTick <= 0) {
                blizzardActive = false;
                return;
            }

            BossConfig.BossSettings.IceEmperorSettings.BlizzardSettings config = BossConfig.bossSettings.iceEmperor.blizzard;
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            double radius = config.radius;

            if(this.age % 2 == 0) {
                for (int i = 0; i < 50; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5) * radius * 2;
                    double offsetZ = (this.random.nextDouble() - 0.5) * radius * 2;
                    double offsetY = this.getY() + this.random.nextDouble() * 5;
                    serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE,
                            this.getX() + offsetX, offsetY, this.getZ() + offsetZ,
                            1, 0, 0, 0, 0.05);
                }
            }

            if(blizzardTick % config.damageInterval == 0) {
                Box damageBox = this.getBoundingBox().expand(radius);
                List<LivingEntity> entities = this.getWorld().getNonSpectatingEntities(LivingEntity.class, damageBox);
                for(LivingEntity entity : entities) {
                    if(entity != this) {
                        entity.damage(this.getDamageSources().freeze(), config.damagePerTick);
                        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, config.damageInterval + 20, config.slownessLevel));
                    }
                }
            }
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        ItemEntity coreEntity = this.dropItem(ModItems.LEVEL5_ICE_CORE);
        if (coreEntity != null) {
            coreEntity.setInvulnerable(true);
            coreEntity.setPickupDelay(10);
            coreEntity.setGlowing(true);
        }
        clearGlacialPrison();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isOf(DamageTypes.FREEZE)) {
            return false;
        }
        return super.damage(source, amount);
    }

    @Override
    protected boolean canStartRiding(net.minecraft.entity.Entity entity) { return false; }

    @Override
    protected boolean burnsInDaylight() { return false; }

    @Override
    protected boolean canConvertInWater() { return false; }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) { }
}
