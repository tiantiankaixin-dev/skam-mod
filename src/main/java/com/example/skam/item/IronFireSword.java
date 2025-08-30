package com.example.skam.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
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
import java.util.List;

public class IronFireSword extends SwordItem {

    private static final int EXPLOSION_BEAM_DAMAGE = 20;
    private static final int EXPLOSION_BEAM_RANGE = 30;
    private static final float EXPLOSION_BEAM_AOE_RADIUS = 3.0f;
    private static final int EXPLOSION_BEAM_COOLDOWN = 60;
    private static final int EXPLOSION_BEAM_FIRE_DURATION = 80;

    private static final String COOLDOWN_TAG = "IronFireCD";

    public IronFireSword() {
        super(ToolMaterials.IRON, 4, -2.4F, new Settings().fireproof().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            ItemStack stack = user.getStackInHand(hand);
            fireIronFireExplosionBeam(player, stack);
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void fireIronFireExplosionBeam(ServerPlayerEntity player, ItemStack stack) {
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
                    startPos, endPos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player
            ));
            Vec3d finalHitPos;
            HitResult.Type hitType = HitResult.Type.MISS;

            if (entityHit != null && (blockHit.getType() == HitResult.Type.MISS || startPos.squaredDistanceTo(entityHit.getPos()) < startPos.squaredDistanceTo(blockHit.getPos()))) {
                finalHitPos = entityHit.getPos();
                hitType = HitResult.Type.ENTITY;
            } else if (blockHit.getType() != HitResult.Type.MISS) {
                finalHitPos = blockHit.getPos();
                hitType = HitResult.Type.BLOCK;
            } else {
                finalHitPos = endPos;
            }

            double distance = startPos.distanceTo(finalHitPos);
            for (int i = 0; i < distance; i += 3) {
                Vec3d particlePos = startPos.add(direction.multiply(i));
                world.spawnParticles(ParticleTypes.FLAME,
                        particlePos.x, particlePos.y, particlePos.z, 1, 0.08, 0.08, 0.08, 0.015);
            }

            if (hitType != HitResult.Type.MISS) {
                createIronFireExplosion(world, player, finalHitPos);
            }
        }
    }

    private void createIronFireExplosion(ServerWorld world, ServerPlayerEntity player, Vec3d pos) {
        Explosion explosion = new Explosion(
                world,
                player,
                player.getDamageSources().explosion(player, player),
                null,
                pos.x, pos.y, pos.z,
                EXPLOSION_BEAM_AOE_RADIUS,
                false,
                Explosion.DestructionType.KEEP
        );
        explosion.collectBlocksAndDamageEntities();
        explosion.affectWorld(true);

        world.getOtherEntities(player, Box.from(pos).expand(EXPLOSION_BEAM_AOE_RADIUS),
                e -> e instanceof LivingEntity && e.isAlive() && e != player && !(e instanceof PlayerEntity)).forEach(entity -> {
            if (pos.distanceTo(entity.getPos()) <= EXPLOSION_BEAM_AOE_RADIUS) {
                entity.damage(player.getDamageSources().onFire(), EXPLOSION_BEAM_DAMAGE);
                entity.setOnFireFor(EXPLOSION_BEAM_FIRE_DURATION);
            }
        });
        world.spawnParticles(ParticleTypes.EXPLOSION,
                pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
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
        String skillName = "烈焰轰击";
        player.sendMessage(Text.translatable("chat.skam.cooldown", Text.translatable("ability.skam.explosion_beam"),
                String.format("%.1f", remainingTicks / 20.0)), true);
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
     //   tooltip.add(Text.literal("§6【烈焰神剑】").formatted(Formatting.GOLD));
      //  tooltip.add(Text.empty());
        tooltip.add(Text.translatable("item.skam.iron_fire_sword.tooltip.ability").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("item.skam.iron_fire_sword.tooltip.description1", EXPLOSION_BEAM_RANGE).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.skam.iron_fire_sword.tooltip.description2", EXPLOSION_BEAM_DAMAGE, (EXPLOSION_BEAM_FIRE_DURATION / 20)).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.skam.iron_fire_sword.tooltip.cooldown", (EXPLOSION_BEAM_COOLDOWN / 20)).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.skam.iron_fire_sword.tooltip.note").formatted(Formatting.GRAY));
        tooltip.add(Text.empty());
        tooltip.add(Text.translatable("item.skam.iron_fire_sword.tooltip.upgrade1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.skam.iron_fire_sword.tooltip.upgrade2").formatted(Formatting.GRAY));
    }

    public static SmithingRecipe createSmithingRecipe(Item baseSword, Item additionItem, Item resultSword) {
        throw new UnsupportedOperationException("Smithing recipe creation must be handled via Recipe API registration.");
    }
}