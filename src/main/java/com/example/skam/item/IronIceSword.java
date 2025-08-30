package com.example.skam.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import java.util.List;

public class IronIceSword extends SwordItem {
   private static final int EXPLOSION_BEAM_DAMAGE = 20;
    private static final int EXPLOSION_BEAM_RANGE = 30;
    private static final float EXPLOSION_BEAM_AOE_RADIUS = 3.0f;
    private static final int EXPLOSION_BEAM_COOLDOWN = 60;
    private static final int EXPLOSION_BEAM_SLOWNESS_AMPLIFIER = 1;
    private static final int EXPLOSION_BEAM_SLOWNESS_DURATION = 60;

    private static final String COOLDOWN_TAG = "IronIceCD";

    public IronIceSword() {
        super(ToolMaterials.IRON, 4, -2.4F, new Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            ItemStack stack = player.getStackInHand(hand);
            fireIceExplosionBeam(player, stack);
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void fireIceExplosionBeam(ServerPlayerEntity player, ItemStack stack) {
        if (checkCooldown(player, stack, "beam", EXPLOSION_BEAM_COOLDOWN)) {
            ServerWorld world = player.getServerWorld();
            Vec3d startPos = player.getEyePos();
            Vec3d direction = player.getRotationVec(1.0F).normalize();
            Vec3d endPos = startPos.add(direction.multiply(EXPLOSION_BEAM_RANGE));

            EntityHitResult entityHit = ProjectileUtil.raycast(
                    player, startPos, endPos,
                    new Box(startPos, endPos).expand(1.0),
                    entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.equals(player),
                    EXPLOSION_BEAM_RANGE * EXPLOSION_BEAM_RANGE
            );
            HitResult blockHit = world.raycast(new RaycastContext(
                    startPos, endPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player
            ));

            Vec3d finalHitPos;
            HitResult.Type hitType = HitResult.Type.MISS;

            if (entityHit != null && (blockHit.getType() == HitResult.Type.MISS ||
                    startPos.squaredDistanceTo(entityHit.getPos()) < startPos.squaredDistanceTo(blockHit.getPos()))) {
                finalHitPos = entityHit.getPos();
                hitType = HitResult.Type.ENTITY;
            } else if (blockHit.getType() != HitResult.Type.MISS) {
                finalHitPos = blockHit.getPos();
                hitType = HitResult.Type.BLOCK;
            } else {
                finalHitPos = endPos;
            }

            double distance = startPos.distanceTo(finalHitPos);
            for (int i = 0; i < distance; i += 1) {
                Vec3d particlePos = startPos.add(direction.multiply(i));
                world.spawnParticles(ParticleTypes.SNOWFLAKE,
                        particlePos.x, particlePos.y, particlePos.z, 2, 0.03, 0.03, 0.03, 0.008);
            }

            if (hitType != HitResult.Type.MISS) {
                createIceExplosion(world, player, finalHitPos);
            }
        }
    }

    private void createIceExplosion(ServerWorld world, ServerPlayerEntity player, Vec3d pos) {
        Explosion explosion = new Explosion(
                world, player, player.getDamageSources().explosion(player, player), null,
                pos.x, pos.y, pos.z, EXPLOSION_BEAM_AOE_RADIUS,
                false, Explosion.DestructionType.KEEP
        );
        explosion.collectBlocksAndDamageEntities();
        explosion.affectWorld(true);

        world.getOtherEntities(player, Box.from(pos).expand(EXPLOSION_BEAM_AOE_RADIUS),
                        e -> e instanceof LivingEntity && e.isAlive() && e != player && !(e instanceof PlayerEntity))
                .forEach(entity -> {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (pos.distanceTo(livingEntity.getPos()) <= EXPLOSION_BEAM_AOE_RADIUS) {
                        livingEntity.damage(player.getDamageSources().freeze(), EXPLOSION_BEAM_DAMAGE);
                        livingEntity.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.SLOWNESS,
                                EXPLOSION_BEAM_SLOWNESS_DURATION,
                                EXPLOSION_BEAM_SLOWNESS_AMPLIFIER
                        ));
                    }
                });

        world.spawnParticles(ParticleTypes.SNOWFLAKE,
                pos.x, pos.y + 0.5, pos.z, 80, 1.5, 0.8, 1.5, 0.05);
    }

    private boolean checkCooldown(ServerPlayerEntity player, ItemStack stack, String skill, int cd) {
        if (stack.getItem() != this) return false;
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound cooldowns;
        if (!nbt.contains(COOLDOWN_TAG, NbtCompound.COMPOUND_TYPE)) {
            cooldowns = new NbtCompound();
            nbt.put(COOLDOWN_TAG, cooldowns);
        } else {
            cooldowns = nbt.getCompound(COOLDOWN_TAG);
        }
        long currentTime = player.getWorld().getTime();
        if (cooldowns.getLong(skill + "_Time") <= currentTime) {
            cooldowns.putLong(skill + "_Time", currentTime + cd);
            return true;
        }
        long remainingTicks = cooldowns.getLong(skill + "_Time") - currentTime;
        Text skillName = Text.translatable("message.skam.skill.ice_blast");

        player.sendMessage(Text.translatable("message.skam.cooldown", skillName, remainingTicks / 20.0), true);
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
     //   tooltip.add(Text.literal("§b【寒冰神剑】").formatted(Formatting.AQUA));
     //   tooltip.add(Text.empty());
        tooltip.add(Text.translatable("tooltip.skam.iron_ice_sword.ability").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.iron_ice_sword.range").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.iron_ice_sword.aoe").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.iron_ice_sword.damage").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.iron_ice_sword.effect").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.iron_ice_sword.cooldown").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.iron_ice_sword.no_player_damage").formatted(Formatting.GRAY));
        tooltip.add(Text.empty());
        tooltip.add(Text.translatable("tooltip.skam.iron_ice_sword.upgrade_info_1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.iron_ice_sword.upgrade_info_2").formatted(Formatting.GRAY));
    }

    public static SmithingRecipe createSmithingRecipe(Item baseSword, Item additionItem, Item resultSword) {
        throw new UnsupportedOperationException("Smithing recipe creation must be handled via Recipe API registration.");
    }
}
