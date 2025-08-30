package com.example.skam.item;

import com.example.skam.config.SkamConfig;
import com.example.skam.enchantment.ModEnchantments;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
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

public class DiamondFireSword extends SwordItem {

    private static final String COOLDOWN_TAG = "DiamondFireCD";

    public DiamondFireSword() {
        super(ToolMaterials.DIAMOND, 7, -2.4F, new Settings().fireproof().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            ItemStack stack = user.getStackInHand(hand);
            if (player.isSneaking()) {
                activateFireExplosion(player, stack);
            } else {
                activateFireBeam(player, stack);
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    // <<< 新增: 允许非玩家实体使用技能
    public void performEntityRightClick(World world, LivingEntity entity) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            // Boss默认使用范围更大的火焰爆破
            createFireExplosion(serverWorld, entity);
        }
    }

    private void activateFireExplosion(ServerPlayerEntity player, ItemStack stack) {
        SkamConfig.DiamondFireSwordConfig.FireExplosionConfig config = SkamConfig.getInstance().weapons.diamond_fire_sword.fire_explosion;
        if (checkCooldown(player, stack, "explosion_burst", config.cooldown_ticks)) {
            createFireExplosion(player.getServerWorld(), player);
        }
    }

    // <<< 优化: 提取出的通用方法，接受 LivingEntity
    private void createFireExplosion(ServerWorld world, LivingEntity caster) {
        SkamConfig.DiamondFireSwordConfig.FireExplosionConfig config = SkamConfig.getInstance().weapons.diamond_fire_sword.fire_explosion;
        Vec3d casterPos = caster.getPos();
        float radius = getExplosionRadius(caster, config.base_radius);

        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                Box.from(casterPos).expand(radius),
                e -> e.isAlive() && e != caster && !(e instanceof PlayerEntity && caster instanceof PlayerEntity)
        );

        // <<< 修复点: 使用统一的 indirectMagic 伤害源
        var magicDamageSource = caster.getDamageSources().indirectMagic(caster, caster);
        targets.forEach(entity -> {
            entity.damage(magicDamageSource, (float) config.damage);
            entity.setOnFireFor(config.fire_duration_ticks / 20);
        });

        // 粒子和声音效果
        world.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.0f, 0.7f);
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, caster.getX(), caster.getY(), caster.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
        world.spawnParticles(ParticleTypes.FLAME, caster.getX(), caster.getY() + 1, caster.getZ(), (int) (radius * 50), radius * 0.7, 0.5, radius * 0.7, 0.2);
        world.spawnParticles(ParticleTypes.LAVA, caster.getX(), caster.getY() + 0.5, caster.getZ(), (int) (radius * 10), radius * 0.5, 0.2, radius * 0.5, 0.1);
    }

    // <<< 优化: 方法现在接受 LivingEntity
    private float getExplosionRadius(LivingEntity caster, int baseRadius) {
        ItemStack stack = caster.getMainHandStack();
        int enchantLevel = EnchantmentHelper.getLevel(ModEnchantments.DOMAIN_EXPANSION, stack);
        return baseRadius + enchantLevel;
    }

    private void activateFireBeam(ServerPlayerEntity player, ItemStack stack) {
        SkamConfig.DiamondFireSwordConfig.ExplosionBeamConfig config = SkamConfig.getInstance().weapons.diamond_fire_sword.explosion_beam;
        if (checkCooldown(player, stack, "explosion_beam", config.cooldown_ticks)) {
            createFireBeam(player.getServerWorld(), player);
        }
    }

    // <<< 优化: 提取出的通用方法，接受 LivingEntity
    private void createFireBeam(ServerWorld world, LivingEntity caster) {
        SkamConfig.DiamondFireSwordConfig.ExplosionBeamConfig config = SkamConfig.getInstance().weapons.diamond_fire_sword.explosion_beam;
        Vec3d startPos = caster.getEyePos();
        Vec3d direction = caster.getRotationVec(1.0F).normalize();
        Vec3d endPos = startPos.add(direction.multiply(config.range));

        EntityHitResult entityHit = ProjectileUtil.raycast(caster, startPos, endPos, new Box(startPos, endPos).expand(1.5), entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.equals(caster), config.range * config.range);
        HitResult blockHit = world.raycast(new RaycastContext(startPos, endPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, caster));

        Vec3d finalHitPos;
        if (entityHit != null && (blockHit.getType() == HitResult.Type.MISS || startPos.squaredDistanceTo(entityHit.getPos()) < startPos.squaredDistanceTo(blockHit.getPos()))) {
            finalHitPos = entityHit.getPos();
        } else if (blockHit.getType() != HitResult.Type.MISS) {
            finalHitPos = blockHit.getPos();
        } else {
            finalHitPos = endPos;
        }

        // 粒子效果
        double distance = startPos.distanceTo(finalHitPos);
        for (double i = 0; i < distance; i += 0.5) {
            world.spawnParticles(ParticleTypes.FLAME, startPos.x + direction.x * i, startPos.y + direction.y * i, startPos.z + direction.z * i, 2, 0.08, 0.08, 0.08, 0.015);
        }

        // 产生爆炸
        createBeamExplosion(world, caster, finalHitPos);
    }

    // <<< 优化: 提取出的通用方法，不再使用物理爆炸实体
    private void createBeamExplosion(ServerWorld world, LivingEntity caster, Vec3d pos) {
        SkamConfig.DiamondFireSwordConfig.ExplosionBeamConfig config = SkamConfig.getInstance().weapons.diamond_fire_sword.explosion_beam;

        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                Box.from(pos).expand(config.aoe_radius),
                e -> e.isAlive() && e != caster && !(e instanceof PlayerEntity && caster instanceof PlayerEntity)
        );

        // <<< 修复点: 使用统一的 indirectMagic 伤害源
        var magicDamageSource = caster.getDamageSources().indirectMagic(caster, caster);
        targets.forEach(entity -> {
            entity.damage(magicDamageSource, (float) config.damage);
            entity.setOnFireFor(config.fire_duration_ticks / 20);
        });

        // 粒子和声音效果
        world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0f, 0.8f);
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
        world.spawnParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, (int)(config.aoe_radius * 40), config.aoe_radius, config.aoe_radius * 0.5, config.aoe_radius, 0.1);
    }

    // 这个冷却方法已经是正确的了，无需修改
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

        // 使用翻译键获取技能名称
        String skillTranslationKey = skill.equals("explosion_burst") ? "message.skam.skill.fire_explosion" : "message.skam.skill.flame_blast";
        Text skillName = Text.translatable(skillTranslationKey);

        // 使用通用的冷却消息翻译键
        player.sendMessage(Text.translatable("message.skam.cooldown", skillName, remainingTicks / 20.0), true);
        return false;
    }


    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        SkamConfig.DiamondFireSwordConfig fireConfig = SkamConfig.getInstance().weapons.diamond_fire_sword;
        int enchantLevel = EnchantmentHelper.getLevel(ModEnchantments.DOMAIN_EXPANSION, stack);
        float currentExplosionRadius = fireConfig.fire_explosion.base_radius + enchantLevel;

        // 技能1: 火焰爆破
        tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.ability.explosion").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.description.explosion_radius", String.format("%.0f", currentExplosionRadius)).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.description.explosion_damage", fireConfig.fire_explosion.damage, fireConfig.fire_explosion.fire_duration_ticks / 20).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.description.explosion_cooldown", fireConfig.fire_explosion.cooldown_ticks / 20).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.description.no_player_damage").formatted(Formatting.GRAY));
        tooltip.add(Text.empty());

        // 技能2: 烈焰轰击
        tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.ability.beam").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.description.beam_range", fireConfig.explosion_beam.range).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.description.beam_damage", fireConfig.explosion_beam.damage, fireConfig.explosion_beam.fire_duration_ticks / 20).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.description.beam_cooldown", fireConfig.explosion_beam.cooldown_ticks / 20).formatted(Formatting.GRAY));

        // 附魔信息
        if (enchantLevel > 0) {
            tooltip.add(Text.empty());
            tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.enchant.header", enchantLevel).formatted(Formatting.LIGHT_PURPLE));
            tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.enchant.description", enchantLevel).formatted(Formatting.GRAY));
        }

        // 升级信息
        tooltip.add(Text.empty());
        tooltip.add(Text.translatable("tooltip.skam.diamond_fire_sword.upgrade").formatted(Formatting.GRAY));
    }

}
