package com.example.skam.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class WindCallerHornItem extends Item {

    private static final int COOLDOWN_TICKS = 20;
    private static final double ENEMY_KNOCKBACK_STRENGTH = 2.0;
    private static final double SELF_BOOST_STRENGTH = 1.2;
    private static final int DETECTION_RANGE = 8;
    private static final int DETECTION_WIDTH = 4;

    public WindCallerHornItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        boolean isLookingDown = user.getPitch() > 65.0f;
        if (isLookingDown) {
            user.addVelocity(0, SELF_BOOST_STRENGTH, 0);
            user.fallDistance = 0.0f;
        }

        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;

            if (isLookingDown) {
               user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 60, 0, false, false, true));
                serverWorld.spawnParticles(ParticleTypes.POOF, user.getX(), user.getY(), user.getZ(), 20, 0.5, 0.1, 0.5, 0.1);
            } else {
                Vec3d lookVec = user.getRotationVector();
                Vec3d detectionCenter = user.getEyePos().add(lookVec.multiply(DETECTION_RANGE / 2.0));
                Box detectionBox = new Box(detectionCenter, detectionCenter).expand(DETECTION_WIDTH, DETECTION_WIDTH, DETECTION_WIDTH).stretch(lookVec.multiply(DETECTION_RANGE));
                List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, detectionBox, (entity) -> entity != user && entity.isAlive());

                for (LivingEntity target : entities) {
                    Vec3d knockbackDir = target.getPos().subtract(user.getPos()).normalize();
                    target.addVelocity(
                            knockbackDir.x * ENEMY_KNOCKBACK_STRENGTH,
                            0.4,
                            knockbackDir.z * ENEMY_KNOCKBACK_STRENGTH
                    );
                }

                Vec3d particlePos = user.getEyePos().add(lookVec.multiply(1.5));
                serverWorld.spawnParticles(ParticleTypes.CLOUD, particlePos.x, particlePos.y, particlePos.z, 30, 0.5, 0.5, 0.5, 0.1);
            }

            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);

            RegistryEntry<SoundEvent> raidHornSound = SoundEvents.EVENT_RAID_HORN;
            world.playSound(null, user.getBlockPos(), raidHornSound.value(), SoundCategory.PLAYERS, 1.5f, 1.0f);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }
}
