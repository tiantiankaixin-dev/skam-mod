package com.example.skam.entity;

import com.example.skam.item.ModItems;
import com.example.skam.SkamMod;
import com.example.skam.config.BossConfig;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LightningEmperorEntity extends DrownedEntity {
    private final ServerBossBar bossBar;
    private static final UUID LIGHTNING_CIRCLE_SPEED_ID = UUID.fromString("b4a1c2d0-3c1e-4a5d-8c1f-2a3b9e4a3d1c");
    private boolean lightningMagicCircleApplied = false;
    private boolean hasSummonedGuardians = false;
    private int chainLightningCooldown = 0;
    private int thunderousLeapCooldown = 0;
    private boolean isLeaping = false;
    private boolean isStaticFieldActive = false;
    private int staticFieldTick = 0;


    public LightningEmperorEntity(EntityType<? extends DrownedEntity> entityType, World world) {
        super(entityType, world);
        SkamMod.LOGGER.info("雷霆君主实体已创建");
        this.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, new ItemStack(Items.TRIDENT));
        this.bossBar = new ServerBossBar(this.getDisplayName(), BossBar.Color.YELLOW, BossBar.Style.PROGRESS);
        this.experiencePoints = 500;
    }

    public static DefaultAttributeContainer.Builder createLightningEmperorAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, BossConfig.bossSettings.lightningEmperor.health)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, BossConfig.bossSettings.lightningEmperor.movementSpeed)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, BossConfig.bossSettings.lightningEmperor.attackDamage)
                .add(EntityAttributes.GENERIC_ARMOR, BossConfig.bossSettings.lightningEmperor.armor)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, BossConfig.bossSettings.lightningEmperor.followRange)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS, 0.0D);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
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
    public void setCustomName(Text name) {
        super.setCustomName(name);
        this.bossBar.setName(this.getDisplayName());
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        this.bossBar.clearPlayers();
        this.bossBar.setVisible(false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            return;
        }
        this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

        if (!lightningMagicCircleApplied) {
            applyLightningMagicCirclePassive();
            lightningMagicCircleApplied = true;
        }
        handleLightningMagicCircleParticles();
        handleLeapLanding();
        handleStaticFieldEffect();

        if (chainLightningCooldown > 0) chainLightningCooldown--;
        if (thunderousLeapCooldown > 0) thunderousLeapCooldown--;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || isLeaping) {
            return;
        }
        handleGuardianSummon();
        handleStaticField();
        handleThunderousLeap(target);
        handleChainLightning(target);
    }
    private void applyLightningMagicCirclePassive() {
        BossConfig.BossSettings.LightningEmperorSettings.LightningMagicCircleSettings config = BossConfig.bossSettings.lightningEmperor.lightningMagicCircle;
        EntityAttributeInstance attribute = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (attribute != null) {
            EntityAttributeModifier modifier = new EntityAttributeModifier(
                    LIGHTNING_CIRCLE_SPEED_ID, "Lightning Circle Buff", config.additionalMovementSpeed, EntityAttributeModifier.Operation.ADDITION);
            attribute.removeModifier(LIGHTNING_CIRCLE_SPEED_ID);
            attribute.addPersistentModifier(modifier);
        }
    }

    private void handleLightningMagicCircleParticles() {
        if (this.age % 4 != 0) return;
        BossConfig.BossSettings.LightningEmperorSettings.LightningMagicCircleSettings config = BossConfig.bossSettings.lightningEmperor.lightningMagicCircle;
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            double radius = config.circleRadius;
            for (int i = 0; i < 360; i += 20) {
                double angle = Math.toRadians(i);
                double xOffset = Math.cos(angle) * radius;
                double zOffset = Math.sin(angle) * radius;
                serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        this.getX() + xOffset, this.getY() + 0.2, this.getZ() + zOffset,
                        1, 0, 0, 0, 0.0);
            }
        }
    }
    private void handleGuardianSummon() {
        BossConfig.BossSettings.LightningEmperorSettings.GuardianSummonSettings config = BossConfig.bossSettings.lightningEmperor.guardianSummon;
        if (!hasSummonedGuardians && this.getHealth() <= config.triggerHealthThreshold) {
            hasSummonedGuardians = true;
            summonElderGuardians();
        }
    }

    private void handleChainLightning(LivingEntity target) {
        BossConfig.BossSettings.LightningEmperorSettings.ChainLightningSettings config = BossConfig.bossSettings.lightningEmperor.chainLightning;
        if (chainLightningCooldown <= 0) {
            chainLightningCooldown = config.cooldown;
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 1.5f, 1.8f);

            List<PlayerEntity> hitPlayers = new ArrayList<>();
            LivingEntity currentTarget = target;

            for (int i = 0; i <= config.maxJumps && currentTarget != null; i++) {
                if(currentTarget instanceof PlayerEntity) hitPlayers.add((PlayerEntity) currentTarget);

                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(this.getWorld());
                if (lightning != null) {
                    lightning.refreshPositionAfterTeleport(currentTarget.getPos());
                    lightning.setCosmetic(true);
                    this.getWorld().spawnEntity(lightning);
                }
                currentTarget.damage(this.getDamageSources().lightningBolt(), config.damage);

                LivingEntity finalCurrentTarget = currentTarget;
                List<PlayerEntity> potentialTargets = this.getWorld().getEntitiesByClass(
                        PlayerEntity.class,
                        new Box(finalCurrentTarget.getBlockPos()).expand(config.jumpRange),
                        p -> !hitPlayers.contains(p) && p.isAlive() && !p.isSpectator());

                currentTarget = potentialTargets.stream()
                        .min((p1, p2) -> Float.compare(p1.distanceTo(finalCurrentTarget), p2.distanceTo(finalCurrentTarget)))
                        .orElse(null);
            }
        }
    }

    private void handleThunderousLeap(LivingEntity target) {
        BossConfig.BossSettings.LightningEmperorSettings.ThunderousLeapSettings config = BossConfig.bossSettings.lightningEmperor.thunderousLeap;
        if (thunderousLeapCooldown <= 0 && this.distanceTo(target) > config.leapMinRange) {
            thunderousLeapCooldown = config.cooldown;
            isLeaping = true;
            this.getLookControl().lookAt(target);
            Vec3d velocity = new Vec3d(0, config.leapHeight, 0);
            this.addVelocity(velocity.x, velocity.y, velocity.z);
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.HOSTILE, 2.0f, 1.5f);
        }
    }

    private void handleLeapLanding() {
        if(isLeaping && this.isOnGround()) {
            isLeaping = false;
            BossConfig.BossSettings.LightningEmperorSettings.ThunderousLeapSettings config = BossConfig.bossSettings.lightningEmperor.thunderousLeap;
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.0f, 1.0f);
            ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 5, 2, 0.5, 2, 0);

            List<PlayerEntity> nearbyPlayers = this.getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(config.leapMinRange), p -> p.isAlive() && !p.isSpectator());
            for(PlayerEntity player : nearbyPlayers) {
                player.damage(this.getDamageSources().mobAttack(this), config.damage);
                Vec3d knockbackDir = player.getPos().subtract(this.getPos()).normalize();
                player.takeKnockback(config.knockbackStrength, -knockbackDir.x, -knockbackDir.z);
            }
        }
    }

    private void handleStaticField() {
        BossConfig.BossSettings.LightningEmperorSettings.StaticFieldSettings config = BossConfig.bossSettings.lightningEmperor.staticField;
        if (!isStaticFieldActive && this.getHealth() <= config.triggerHealthThreshold) {
            isStaticFieldActive = true;
            staticFieldTick = config.duration;
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.HOSTILE, 2.0f, 0.8f);
        }
    }

    private void handleStaticFieldEffect() {
        if(isStaticFieldActive) {
            staticFieldTick--;
            if (staticFieldTick <= 0) {
                isStaticFieldActive = false;
                return;
            }
            BossConfig.BossSettings.LightningEmperorSettings.StaticFieldSettings config = BossConfig.bossSettings.lightningEmperor.staticField;
            if (this.age % 2 == 0) {
                for (int i = 0; i < 10; i++) {
                    double x = this.getX() + (this.random.nextDouble() - 0.5) * config.radius * 2;
                    double y = this.getY() + this.random.nextDouble() * 3;
                    double z = this.getZ() + (this.random.nextDouble() - 0.5) * config.radius * 2;
                    ((ServerWorld)this.getWorld()).spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0,0,0,0);
                }
            }
            if(staticFieldTick % config.damageInterval == 0) {
                List<PlayerEntity> nearby = this.getWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(config.radius), p -> p.isAlive() && !p.isSpectator());
                for(PlayerEntity player : nearby) {
                    player.damage(this.getDamageSources().magic(), config.damagePerTick);
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, config.damageInterval + 10, config.weaknessLevel));
                }
            }
        }
    }

    private void summonElderGuardians() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        BossConfig.BossSettings.LightningEmperorSettings.GuardianSummonSettings config = BossConfig.bossSettings.lightningEmperor.guardianSummon;

        for (int i = 0; i < config.guardianCount; i++) {
            ElderGuardianEntity guardian = EntityType.ELDER_GUARDIAN.create(this.getWorld());
            if (guardian != null) {
                double angle = (2 * Math.PI * i) / config.guardianCount;
                double x = this.getX() + Math.cos(angle) * config.summonRadius;
                double z = this.getZ() + Math.sin(angle) * config.summonRadius;

                guardian.refreshPositionAndAngles(x, this.getY(), z, 0, 0);

                EntityAttributeInstance healthAttribute = guardian.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                if (healthAttribute != null) {
                    healthAttribute.setBaseValue(config.guardianHealth);
                }

                EntityAttributeInstance damageAttribute = guardian.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (damageAttribute != null) {
                    damageAttribute.setBaseValue(config.guardianDamage);
                }

                guardian.setHealth(guardian.getMaxHealth());
                serverWorld.spawnEntity(guardian);
            }
        }
    }


    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        super.dropLoot(damageSource, causedByPlayer);
        ItemEntity coreEntity = this.dropItem(ModItems.LEVEL5_LIGHTNING_CORE);
        if (coreEntity != null) {
            coreEntity.setInvulnerable(true);
            coreEntity.setPickupDelay(10);
            coreEntity.setGlowing(true);
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public void checkDespawn() {
    }
}
