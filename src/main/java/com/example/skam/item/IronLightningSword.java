package com.example.skam.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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
import java.util.List;

public class IronLightningSword extends SwordItem {
    private static final int BEAM_DIRECT_DAMAGE = 20;
    private static final int BEAM_LIGHTNING_DAMAGE = 10;
    private static final int BEAM_RANGE = 30;
    private static final double BEAM_AOE_RADIUS = 1.5;
    private static final int BEAM_COOLDOWN = 60;

    private static final String COOLDOWN_TAG = "IronLightningCD";

    public IronLightningSword() {
        super(ToolMaterials.IRON, 3, -2.4F, new Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            ItemStack stack = player.getStackInHand(hand);
            fireLightningBeam(player, stack);
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void fireLightningBeam(ServerPlayerEntity player, ItemStack stack) {
        if (checkCooldown(player, stack, "beam", BEAM_COOLDOWN)) {
            ServerWorld world = player.getServerWorld();
            Vec3d startPos = player.getEyePos();
            Vec3d direction = player.getRotationVec(1.0F).normalize();
            Vec3d endPos = startPos.add(direction.multiply(BEAM_RANGE));

            EntityHitResult entityHit = ProjectileUtil.raycast(
                    player, startPos, endPos,
                    new Box(startPos, endPos).expand(0.3),
                    entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.equals(player),
                    BEAM_RANGE * BEAM_RANGE
            );
            HitResult blockHit = world.raycast(new RaycastContext(
                    startPos, endPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player
            ));

            Vec3d finalHitPos;
            LivingEntity directHitEntity = null;

            if (entityHit != null && (blockHit.getType() == HitResult.Type.MISS ||
                    startPos.squaredDistanceTo(entityHit.getPos()) < startPos.squaredDistanceTo(blockHit.getPos()))) {
                finalHitPos = entityHit.getPos();
                directHitEntity = (LivingEntity) entityHit.getEntity();
            } else if (blockHit.getType() != HitResult.Type.MISS) {
                finalHitPos = blockHit.getPos();
            } else {
                finalHitPos = endPos;
            }

            double distance = startPos.distanceTo(finalHitPos);
            for (double i = 0; i < distance; i += 0.8) {
                Vec3d particlePos = startPos.add(direction.multiply(i));
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                        particlePos.x, particlePos.y, particlePos.z, 1, 0, 0, 0, 0.0D);
            }
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 0.5f, 1.5f);

            if (directHitEntity != null) {
                directHitEntity.damage(player.getDamageSources().indirectMagic(player, player), BEAM_DIRECT_DAMAGE);
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
                if (lightning != null) {
                    lightning.setCosmetic(true);
                    lightning.setPosition(directHitEntity.getPos());
                    world.spawnEntity(lightning);
                    directHitEntity.damage(player.getDamageSources().lightningBolt(), BEAM_LIGHTNING_DAMAGE);
                }
            } else if (blockHit.getType() != HitResult.Type.MISS && distance > 1) {
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
                if (lightning != null) {
                    lightning.setCosmetic(true);
                    lightning.setPosition(finalHitPos);
                    world.spawnEntity(lightning);
                    Box damageArea = new Box(
                            finalHitPos.getX() - BEAM_AOE_RADIUS,
                            finalHitPos.getY() - BEAM_AOE_RADIUS,
                            finalHitPos.getZ() - BEAM_AOE_RADIUS,
                            finalHitPos.getX() + BEAM_AOE_RADIUS,
                            finalHitPos.getY() + BEAM_AOE_RADIUS,
                            finalHitPos.getZ() + BEAM_AOE_RADIUS
                    );
                    List<LivingEntity> nearbyEntities = world.getEntitiesByClass(
                            LivingEntity.class, damageArea, e -> e.isAlive() && e != player);
                    for (LivingEntity entity : nearbyEntities) {
                        entity.damage(player.getDamageSources().lightningBolt(), BEAM_LIGHTNING_DAMAGE);
                    }
                }
            }
        }
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
        Text skillName = Text.translatable("message.skam.skill.lightning_beam");
        player.sendMessage(Text.translatable("message.skam.cooldown", skillName, remainingTicks / 20.0), true);
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      //  tooltip.add(Text.literal("§5【雷电神剑】").formatted(Formatting.DARK_PURPLE));
      //  tooltip.add(Text.empty());
        tooltip.add(Text.translatable("tooltip.skam.iron_lightning_sword.ability").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.iron_lightning_sword.range").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.iron_lightning_sword.direct_damage").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.iron_lightning_sword.lightning_damage").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.iron_lightning_sword.aoe_damage").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.iron_lightning_sword.cooldown").formatted(Formatting.GRAY));
        tooltip.add(Text.empty());
        tooltip.add(Text.translatable("tooltip.skam.iron_lightning_sword.upgrade_info_1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.iron_lightning_sword.upgrade_info_2").formatted(Formatting.GRAY));
    }

    public static SmithingRecipe createSmithingRecipe(Item baseSword, Item additionItem, Item resultSword) {
        throw new UnsupportedOperationException("Smithing recipe creation must be handled via Recipe API registration.");
    }
}
