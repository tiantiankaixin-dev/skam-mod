package com.example.skam.item;

import com.example.skam.config.SkamConfig;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
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
// Removed unused Explosion import

import java.util.List;

public class DiamondIceSword extends SwordItem {
    private static final String COOLDOWN_TAG = "DiamondIceCD";

    public DiamondIceSword() {
        super(ToolMaterials.DIAMOND, 7, -2.4F, new Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            ItemStack stack = player.getStackInHand(hand);
            if (player.isSneaking()) {
                activateIceExplosion(player, stack);
            } else {
                fireIceExplosionBeam(player, stack);
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    // <<< 新增: 允许非玩家实体使用技能
    public void performEntityRightClick(World world, LivingEntity entity) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            // Boss默认使用伤害更高的冰霜爆破
            createIceExplosion(serverWorld, entity);
        }
    }

    private void activateIceExplosion(ServerPlayerEntity player, ItemStack stack) {
        SkamConfig.DiamondIceSwordConfig.IceExplosionConfig config = SkamConfig.getInstance().weapons.diamond_ice_sword.ice_explosion;
        if (checkCooldown(player, stack, "explosion_burst", config.cooldown_ticks)) {
            createIceExplosion(player.getServerWorld(), player);
        }
    }

    // <<< 优化: 提取出的通用方法
    private void createIceExplosion(ServerWorld world, LivingEntity caster) {
        SkamConfig.DiamondIceSwordConfig.IceExplosionConfig config = SkamConfig.getInstance().weapons.diamond_ice_sword.ice_explosion;
        Vec3d casterPos = caster.getPos();

        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                Box.from(casterPos).expand(config.radius),
                e -> e.isAlive() && e != caster && !(e instanceof PlayerEntity && caster instanceof PlayerEntity)
        );

        // <<< 修复点: 使用统一的 indirectMagic 伤害源
        var magicDamageSource = caster.getDamageSources().indirectMagic(caster, caster);
        targets.forEach(entity -> {
            entity.damage(magicDamageSource, (float) config.damage);
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, config.slowness_duration_ticks, config.slowness_amplifier));
        });

        // 粒子和声音效果
        world.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0f, 0.8f);
        world.spawnParticles(ParticleTypes.SNOWFLAKE, caster.getX(), caster.getY() + 1, caster.getZ(), (int) (config.radius * 60), config.radius * 0.8, 0.6, config.radius * 0.8, 0.1);
        world.spawnParticles(ParticleTypes.ITEM_SNOWBALL, caster.getX(), caster.getY() + 1, caster.getZ(), (int) (config.radius * 15), config.radius * 0.6, 0.4, config.radius * 0.6, 0.05);
    }


    private void fireIceExplosionBeam(ServerPlayerEntity player, ItemStack stack) {
        SkamConfig.DiamondIceSwordConfig.IceExplosionBeamConfig config = SkamConfig.getInstance().weapons.diamond_ice_sword.ice_explosion_beam;
        if (checkCooldown(player, stack, "explosion_beam", config.cooldown_ticks)) {
            createIceExplosionBeam(player.getServerWorld(), player);
        }
    }

    // <<< 优化: 提取出的通用方法
    private void createIceExplosionBeam(ServerWorld world, LivingEntity caster) {
        SkamConfig.DiamondIceSwordConfig.IceExplosionBeamConfig config = SkamConfig.getInstance().weapons.diamond_ice_sword.ice_explosion_beam;
        Vec3d startPos = caster.getEyePos();
        Vec3d direction = caster.getRotationVec(1.0F).normalize();
        Vec3d endPos = startPos.add(direction.multiply(config.range));

        EntityHitResult entityHit = ProjectileUtil.raycast(caster, startPos, endPos, new Box(startPos, endPos).expand(1.0), entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.equals(caster), (long) config.range * config.range);
        HitResult blockHit = world.raycast(new RaycastContext(startPos, endPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, caster));

        Vec3d finalHitPos;
        if (entityHit != null && (blockHit.getType() == HitResult.Type.MISS || startPos.squaredDistanceTo(entityHit.getPos()) < startPos.squaredDistanceTo(blockHit.getPos()))) {
            finalHitPos = entityHit.getPos();
        } else if (blockHit.getType() != HitResult.Type.MISS) {
            finalHitPos = blockHit.getPos();
        } else {
            finalHitPos = endPos;
        }

        // 射线粒子效果
        for (double i = 0; i < startPos.distanceTo(finalHitPos); i += 0.5) { // 增加粒子密度
            Vec3d particlePos = startPos.add(direction.multiply(i));
            world.spawnParticles(ParticleTypes.SNOWFLAKE, particlePos.x, particlePos.y, particlePos.z, 5, 0.04, 0.04, 0.04, 0.01);
        }

        // 爆炸效果
        createIceBeamExplosion(world, caster, finalHitPos, config);
    }

    // <<< 优化: 方法现在接受 LivingEntity
    private void createIceBeamExplosion(ServerWorld world, LivingEntity caster, Vec3d pos, SkamConfig.DiamondIceSwordConfig.IceExplosionBeamConfig config) {
        // 不再创建物理爆炸，只造成伤害和效果
        List<LivingEntity> affectedEntities = world.getEntitiesByClass(
                LivingEntity.class,
                Box.from(pos).expand(config.aoe_radius),
                e -> e.isAlive() && e != caster && !(e instanceof PlayerEntity && caster instanceof PlayerEntity)
        );

        // <<< 修复点: 使用统一的 indirectMagic 伤害源
        var magicDamageSource = caster.getDamageSources().indirectMagic(caster, caster);
        affectedEntities.forEach(livingEntity -> {
            livingEntity.damage(magicDamageSource, (float)config.damage);
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, config.slowness_duration_ticks, config.slowness_amplifier));
        });

        // 粒子和声音
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.NEUTRAL, 1.0f, 1.0f);
        world.spawnParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y + 0.5, pos.z, (int)(config.aoe_radius * 50), config.aoe_radius, config.aoe_radius * 0.5, config.aoe_radius, 0.1);
        world.spawnParticles(ParticleTypes.ITEM_SNOWBALL, pos.x, pos.y + 0.5, pos.z, (int)(config.aoe_radius * 20), config.aoe_radius * 0.8, config.aoe_radius * 0.4, config.aoe_radius * 0.8, 0.08);
    }


    // <<< 修复点: 使用和其他剑统一的、更健壮的冷却系统
    private boolean checkCooldown(ServerPlayerEntity player, ItemStack stack, String skillKey, int cd) {
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
        if (cooldowns.getLong(skillKey + "_Time") <= currentTime) {
            cooldowns.putLong(skillKey + "_Time", currentTime + cd);
            return true;
        }

        long remainingTicks = cooldowns.getLong(skillKey + "_Time") - currentTime;

        // 使用翻译键获取技能名称
        String skillTranslationKey = skillKey.equals("explosion_burst") ? "message.skam.skill.frost_nova" : "message.skam.skill.ice_blast";
        Text skillName = Text.translatable(skillTranslationKey);

        // 使用通用的冷却消息翻译键
        // 注意：这里统一使用了 message.skam.cooldown 的格式
        player.sendMessage(Text.translatable("message.skam.cooldown", skillName, remainingTicks / 20.0), true);
        return false;
    }


    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        SkamConfig.DiamondIceSwordConfig iceConfig = SkamConfig.getInstance().weapons.diamond_ice_sword;

        // 技能1: 冰霜爆破
        tooltip.add(Text.translatable("tooltip.skam.diamond_ice_sword.ability.explosion").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.diamond_ice_sword.description.explosion_damage", iceConfig.ice_explosion.radius, iceConfig.ice_explosion.damage).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_ice_sword.description.explosion_effect", (iceConfig.ice_explosion.slowness_amplifier + 1), (iceConfig.ice_explosion.slowness_duration_ticks / 20)).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_ice_sword.description.explosion_cooldown", (iceConfig.ice_explosion.cooldown_ticks / 20)).formatted(Formatting.GRAY));
        tooltip.add(Text.empty());

        // 技能2: 寒冰轰击
        tooltip.add(Text.translatable("tooltip.skam.diamond_ice_sword.ability.beam").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.diamond_ice_sword.description.beam_aoe", iceConfig.ice_explosion_beam.aoe_radius).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_ice_sword.description.beam_damage_effect", iceConfig.ice_explosion_beam.damage, (iceConfig.ice_explosion_beam.slowness_amplifier + 1)).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_ice_sword.description.beam_cooldown", (iceConfig.ice_explosion_beam.cooldown_ticks / 20)).formatted(Formatting.GRAY));
    }

}
