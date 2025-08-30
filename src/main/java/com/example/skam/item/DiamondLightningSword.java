package com.example.skam.item;

import com.example.skam.config.SkamConfig;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;

public class DiamondLightningSword extends SwordItem {
    private static final String COOLDOWN_TAG = "DiamondLightningCD";

    public DiamondLightningSword() {
        super(ToolMaterials.DIAMOND, 2, -2.4F, new Settings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            ItemStack stack = player.getStackInHand(hand);
            if (player.isSneaking()) {
                activateLightningExplosion(player, stack);
            } else {
                fireLightningBeam(player, stack);
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    // <<< 新增: 允许非玩家实体使用技能
    public void performEntityRightClick(World world, LivingEntity entity) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            // Boss默认使用伤害更高的雷霆爆破
            createLightningExplosion(serverWorld, entity);
        }
    }

    private void activateLightningExplosion(ServerPlayerEntity player, ItemStack stack) {
        SkamConfig.DiamondLightningSwordConfig.LightningExplosionConfig config = SkamConfig.getInstance().weapons.diamond_lightning_sword.lightning_explosion;
        if (checkCooldown(player, stack, "explosion_burst", config.cooldown_ticks)) {
            // <<< 优化: 逻辑提取到通用方法中
            createLightningExplosion(player.getServerWorld(), player);
        }
    }

    // <<< 优化: 提取出的通用方法，可供玩家和实体调用
    private void createLightningExplosion(ServerWorld world, LivingEntity caster) {
        SkamConfig.DiamondLightningSwordConfig.LightningExplosionConfig config = SkamConfig.getInstance().weapons.diamond_lightning_sword.lightning_explosion;
        Random random = world.getRandom();

        world.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 1.5f, 1.0f);
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, caster.getX(), caster.getY() + 1, caster.getZ(), 200, 0.5, 1.0, 0.5, 0.1);

        for (int i = 0; i < config.strike_count; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double radiusOffset = 1.0 + random.nextDouble() * (config.radius - 1.0);
            double strikeX = caster.getX() + Math.cos(angle) * radiusOffset;
            double strikeZ = caster.getZ() + Math.sin(angle) * radiusOffset;
            double strikeY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int)strikeX, (int)strikeZ);
            Vec3d strikePos = new Vec3d(strikeX, strikeY, strikeZ);

            // 1. 召唤纯视觉的闪电
            LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
            lightning.setPosition(strikePos);
            world.spawnEntity(lightning);

            // <<< 修复点: 2. 手动创建范围伤害，确保伤害来源正确
            var magicDamageSource = caster.getDamageSources().indirectMagic(caster, caster);
            List<LivingEntity> targets = world.getEntitiesByClass(
                    LivingEntity.class,
                    Box.from(strikePos).expand(3.0), // 半径3.0的伤害范围
                    e -> e.isAlive() && e != caster && !(e instanceof PlayerEntity && caster instanceof PlayerEntity)
            );

            for(LivingEntity target : targets){
                target.damage(magicDamageSource, (float)config.damage_per_strike);
            }
        }
    }

    private void fireLightningBeam(ServerPlayerEntity player, ItemStack stack) {
        SkamConfig.DiamondLightningSwordConfig.LightningBeamConfig config = SkamConfig.getInstance().weapons.diamond_lightning_sword.lightning_beam;
        if (checkCooldown(player, stack, "beam", config.cooldown_ticks)) {
            // <<< 优化: 逻辑提取到通用方法中
            createLightningBeam(player.getServerWorld(), player);
        }
    }

    // <<< 优化: 提取出的通用方法，可供玩家和实体调用
    private void createLightningBeam(ServerWorld world, LivingEntity caster) {
        SkamConfig.DiamondLightningSwordConfig.LightningBeamConfig config = SkamConfig.getInstance().weapons.diamond_lightning_sword.lightning_beam;
        Vec3d startPos = caster.getEyePos();
        Vec3d direction = caster.getRotationVec(1.0F).normalize();
        Vec3d endPos = startPos.add(direction.multiply(config.range));

        EntityHitResult entityHit = ProjectileUtil.raycast(caster, startPos, endPos, new Box(startPos, endPos).expand(0.4), entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.equals(caster), (long) config.range * config.range);
        HitResult blockHit = world.raycast(new RaycastContext(startPos, endPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, caster));
        Vec3d finalHitPos;
        LivingEntity directHitEntity = null;

        if (entityHit != null && (blockHit.getType() == HitResult.Type.MISS || startPos.squaredDistanceTo(entityHit.getPos()) < startPos.squaredDistanceTo(blockHit.getPos()))) {
            finalHitPos = entityHit.getPos();
            directHitEntity = (LivingEntity) entityHit.getEntity();
        } else if (blockHit.getType() != HitResult.Type.MISS) {
            finalHitPos = blockHit.getPos();
        } else {
            finalHitPos = endPos;
        }

        // 粒子效果
        for (double i = 0; i < startPos.distanceTo(finalHitPos); i += 0.6) {
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, startPos.x + direction.x * i, startPos.y + direction.y * i, startPos.z + direction.z * i, 1, 0, 0, 0, 0.0D);
        }
        world.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 0.6f, 1.4f);

        // <<< 修复点: 统一伤害逻辑
        // 1. 如果直接命中，造成额外的魔法伤害
        if (directHitEntity != null) {
            directHitEntity.damage(caster.getDamageSources().indirectMagic(caster, caster), (float)config.potion_damage);
        }

        // 2. 在命中点召唤视觉闪电
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.setPosition(finalHitPos);
        world.spawnEntity(lightning);

        // 3. 在命中点造成范围雷电伤害
        var magicDamageSource = caster.getDamageSources().indirectMagic(caster, caster);
        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                Box.from(finalHitPos).expand(3.0), // 半径3.0的伤害范围
                e -> e.isAlive() && e != caster && !(e instanceof PlayerEntity && caster instanceof PlayerEntity)
        );
        for(LivingEntity target : targets){
            // 使用 config.lightning_damage, 而不是 potion_damage
            target.damage(magicDamageSource, (float)config.lightning_damage);
        }
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
        String skillTranslationKey = skillKey.equals("explosion_burst") ? "message.skam.skill.thunder_burst" : "message.skam.skill.lightning_beam";
        Text skillName = Text.translatable(skillTranslationKey);

        // 使用通用的冷却消息翻译键
        player.sendMessage(Text.translatable("message.skam.cooldown", skillName, remainingTicks / 20.0), true);
        return false;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        SkamConfig.DiamondLightningSwordConfig lightningConfig = SkamConfig.getInstance().weapons.diamond_lightning_sword;

        // 技能1: 雷霆爆破
        tooltip.add(Text.translatable("tooltip.skam.diamond_lightning_sword.ability.explosion").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.diamond_lightning_sword.description.explosion_strikes", lightningConfig.lightning_explosion.radius, lightningConfig.lightning_explosion.strike_count).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_lightning_sword.description.explosion_damage", lightningConfig.lightning_explosion.damage_per_strike).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_lightning_sword.description.explosion_cooldown", (lightningConfig.lightning_explosion.cooldown_ticks / 20)).formatted(Formatting.GRAY));
        tooltip.add(Text.empty());

        // 技能2: 雷电射线
        tooltip.add(Text.translatable("tooltip.skam.diamond_lightning_sword.ability.beam").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.diamond_lightning_sword.description.beam_lightning_damage", lightningConfig.lightning_beam.lightning_damage).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_lightning_sword.description.beam_direct_damage", lightningConfig.lightning_beam.potion_damage).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.diamond_lightning_sword.description.beam_cooldown", (lightningConfig.lightning_beam.cooldown_ticks / 20)).formatted(Formatting.GRAY));
    }

}

