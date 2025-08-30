package com.example.skam.entity;

import com.example.skam.item.ModItems;
import com.example.skam.SkamMod;
import com.example.skam.config.BossConfig;
import com.example.skam.item.NetheriteFireSword;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class FlameEmperorEntity extends ZombieEntity {
    private final ServerBossBar bossBar;
    private boolean flameAuraActive = false;
    private int flameAuraTick = 0;
    private int rightClickTick = 0;
    private boolean vexSummoned = false;
    private boolean buffApplied = false;

   public FlameEmperorEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        this.bossBar = new ServerBossBar(
                Text.translatable("entity.skam.flame_emperor"),
                BossBar.Color.RED,
                BossBar.Style.PROGRESS
        );
        this.experiencePoints = 500;


        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.NETHERITE_FIRE_SWORD));
        this.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0f); // 不掉落武器
        if (!world.isClient && BossConfig.bossSettings != null) {
           this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                    .setBaseValue(BossConfig.bossSettings.flameEmperor.health);
            this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                    .setBaseValue(BossConfig.bossSettings.flameEmperor.attackDamage);
            this.setHealth(this.getMaxHealth());
        }
    }

    public static DefaultAttributeContainer.Builder createFlameEmperorAttributes() {
        return ZombieEntity.createZombieAttributes();
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
        if (!flameAuraActive && this.getHealth() <= BossConfig.bossSettings.flameEmperor.flameAura.triggerHealthThreshold) {
            activateFlameAura();
        }
        if (!vexSummoned && this.getHealth() <= BossConfig.bossSettings.flameEmperor.vexSummon.triggerHealthThreshold) {
            summonVexes();
            applyBossBuff();
        }
        if (flameAuraActive) {
            handleFlameAura();
        }
        handleWeaponRightClick();
        if (buffApplied) {
            maintainBossBuff();
        }
    }

    private void summonVexes() {
        if (!this.getWorld().isClient && this.getWorld() instanceof ServerWorld serverWorld) {
            vexSummoned = true;

            BossConfig.BossSettings.FlameEmperorSettings.VexSummonSettings config =
                    BossConfig.bossSettings.flameEmperor.vexSummon;
            serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_EVOKER_CAST_SPELL, this.getSoundCategory(), 2.0f, 0.5f);
            for (int i = 0; i < 50; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * config.summonRadius * 2;
                double offsetZ = (this.random.nextDouble() - 0.5) * config.summonRadius * 2;
                double offsetY = this.random.nextDouble() * 3;

                serverWorld.spawnParticles(ParticleTypes.WITCH,
                        this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                        1, 0, 0, 0, 0.02);
            }
            for (int i = 0; i < config.vexCount; i++) {
                VexEntity vex = EntityType.VEX.create(serverWorld);
                if (vex != null) {
                    double angle = this.random.nextDouble() * 2 * Math.PI;
                    double radius = this.random.nextDouble() * config.summonRadius;
                    double x = this.getX() + Math.cos(angle) * radius;
                    double z = this.getZ() + Math.sin(angle) * radius;
                    double y = this.getY() + this.random.nextDouble() * 2;
                    vex.setPosition(x, y, z);
                    vex.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(config.vexHealth);
                    vex.setHealth((float) config.vexHealth);
                    vex.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(config.vexAttackDamage);
                    vex.setOwner(this);
                    if (this.getTarget() instanceof PlayerEntity) {
                        vex.setTarget(this.getTarget());
                    }
                    serverWorld.spawnEntity(vex);
                    serverWorld.spawnParticles(ParticleTypes.POOF,
                            x, y, z, 10, 0.5, 0.5, 0.5, 0.1);
                }
            }

            SkamMod.LOGGER.info("Flame Emperor summoned " + config.vexCount + " vexes!");
        }
    }

    private void applyBossBuff() {
        if (!buffApplied) {
            buffApplied = true;
            BossConfig.BossSettings.FlameEmperorSettings.VexSummonSettings config =
                    BossConfig.bossSettings.flameEmperor.vexSummon;
            this.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.STRENGTH,
                    Integer.MAX_VALUE,
                    config.bossStrengthLevel,
                    false,
                    true,
                    true
            ));
            this.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED,
                    Integer.MAX_VALUE,
                    config.bossSpeedLevel,
                    false,
                    true,
                    true
            ));
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_ENDER_DRAGON_GROWL, this.getSoundCategory(), 2.0f, 1.2f);
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(ParticleTypes.ANGRY_VILLAGER,
                        this.getX(), this.getY() + this.getHeight() / 2, this.getZ(),
                        20, 0.5, 0.5, 0.5, 0.1);
            }

            SkamMod.LOGGER.info("Flame Emperor gained Strength " + (config.bossStrengthLevel + 1) +
                    " and Speed " + (config.bossSpeedLevel + 1) + "!");
        }
    }

    private void maintainBossBuff() {
        BossConfig.BossSettings.FlameEmperorSettings.VexSummonSettings config =
                BossConfig.bossSettings.flameEmperor.vexSummon;

        if (!this.hasStatusEffect(StatusEffects.STRENGTH)) {
            this.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.STRENGTH,
                    Integer.MAX_VALUE,
                    config.bossStrengthLevel,
                    false, true, true
            ));
        }

        if (!this.hasStatusEffect(StatusEffects.SPEED)) {
            this.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED,
                    Integer.MAX_VALUE,
                    config.bossSpeedLevel,
                    false, true, true
            ));
        }
    }

    private void handleWeaponRightClick() {
        rightClickTick++;
        if (rightClickTick >= 200) {
            rightClickTick = 0;
            performWeaponRightClick();
        }
    }

    private void performWeaponRightClick() {
        if (!this.getWorld().isClient) {
            ItemStack mainHandStack = this.getMainHandStack();
            if (!mainHandStack.isEmpty() && mainHandStack.getItem() instanceof NetheriteFireSword) {
                NetheriteFireSword fireSword = (NetheriteFireSword) mainHandStack.getItem();
                fireSword.performEntityRightClick(this.getWorld(), this);
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.HOSTILE, 1.0f, 1.0f);
                SkamMod.LOGGER.info("Flame Emperor used NetheriteFireSword explosion beam!");
            }
        }
    }

    private void activateFlameAura() {
        flameAuraActive = true;
        flameAuraTick = 0;
        if (!this.getWorld().isClient) {
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_ENDER_DRAGON_GROWL, this.getSoundCategory(), 2.0f, 0.5f);
        }

        SkamMod.LOGGER.info("Flame Emperor activated flame aura!");
    }
    private void handleFlameAura() {
        flameAuraTick++;
        if (!this.getWorld().isClient) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            double radius = BossConfig.bossSettings.flameEmperor.flameAura.auraRadius;
            if (flameAuraTick % 5 == 0) {
                for (int i = 0; i < 20; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5) * radius * 2;
                    double offsetZ = (this.random.nextDouble() - 0.5) * radius * 2;
                    double offsetY = this.random.nextDouble() * 3;
                    serverWorld.spawnParticles(ParticleTypes.FLAME,
                            this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                            1, 0, 0, 0, 0.02);

                    serverWorld.spawnParticles(ParticleTypes.DRAGON_BREATH,
                            this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                            1, 0, 0, 0, 0.02);
                }
            }
            if (flameAuraTick % BossConfig.bossSettings.flameEmperor.flameAura.damageInterval == 0) {
                dealFlameAuraDamage(radius);
            }
        }
    }

    private void dealFlameAuraDamage(double radius) {
        Box damageBox = new Box(
                this.getX() - radius, this.getY() - 2, this.getZ() - radius,
                this.getX() + radius, this.getY() + 4, this.getZ() + radius
        );

        List<LivingEntity> entities = this.getWorld().getEntitiesByClass(
                LivingEntity.class, damageBox,
                entity -> entity != this && entity.isAlive() && !entity.isSpectator()
        );

        for (LivingEntity entity : entities) {
            double distance = this.distanceTo(entity);
            if (distance <= radius) {
                entity.damage(this.getDamageSources().create(DamageTypes.DRAGON_BREATH, this),
                        BossConfig.bossSettings.flameEmperor.flameAura.dragonBreathDamage);
                entity.damage(this.getDamageSources().onFire(),
                        BossConfig.bossSettings.flameEmperor.flameAura.fireDamage);
                entity.setOnFireFor(3);
            }
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        ItemEntity coreEntity = this.dropItem(ModItems.LEVEL5_FIRE_CORE);
        if (coreEntity != null) {
            coreEntity.setInvulnerable(true);
            coreEntity.setPickupDelay(10);
            coreEntity.setGlowing(true);
        }
        flameAuraActive = false;
    }


    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    protected boolean canStartRiding(net.minecraft.entity.Entity entity) {
        return false;
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
    }
}