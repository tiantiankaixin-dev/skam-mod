package com.example.skam.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
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
import net.minecraft.world.explosion.Explosion;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.config.SkamConfig;

import java.util.ArrayList;
import java.util.List;

public class NetheriteIceSword extends SwordItem {
    private static class IceDomain {
        final ServerWorld world;
        final ServerPlayerEntity caster;
        int remainingTicks;

        IceDomain(ServerWorld world, ServerPlayerEntity caster) {
            this.world = world;
            this.caster = caster;
            this.remainingTicks = SkamConfig.getInstance().weapons.netherite_ice_sword.ice_domain.duration_ticks;
        }
    }
    private static final List<IceDomain> activeIceDomains = new ArrayList<>();
    private static final String COOLDOWN_TAG = "NetherIceCD";

    public NetheriteIceSword() {
        super(ToolMaterials.NETHERITE, 8, -2.4F, new Settings().maxCount(1));
        ServerTickEvents.END_WORLD_TICK.register(tickingWorld -> {
            if (!tickingWorld.isClient()) {
                activeIceDomains.removeIf(domain -> {
                    if (domain.world == tickingWorld) {
                        domain.remainingTicks--;
                        return processIceDomainTick(domain);
                    }
                    return false;
                });
            }
        });
    }

    private static boolean processIceDomainTick(IceDomain domain) {
        if (domain.remainingTicks <= 0) {
            return true;
        }
        SkamConfig.NetheriteIceSwordConfig.IceDomainConfig config = SkamConfig.getInstance().weapons.netherite_ice_sword.ice_domain;
        Vec3d currentDomainCenter = domain.caster.getPos();

        if (domain.remainingTicks % config.damage_interval_ticks == 0) {
            float radius = getDomainRadius(domain.caster);
            List<LivingEntity> targets = domain.world.getEntitiesByClass(
                    LivingEntity.class,
                    Box.from(currentDomainCenter).expand(radius),
                    e -> currentDomainCenter.distanceTo(e.getPos()) <= radius
                            && e != domain.caster && e.isAlive() && !(e instanceof PlayerEntity)
            );

            // <<< 核心修正 #1：将领域伤害改为 indirectMagic，以确保伤害加成生效
            var magicDamageSource = domain.caster.getDamageSources().indirectMagic(domain.caster, domain.caster);

            targets.forEach(entity -> {
                entity.damage(magicDamageSource, (float) config.damage);
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, config.slowness_duration_ticks, config.slowness_amplifier));
            });
        }

        if (domain.remainingTicks % config.particle_interval_ticks == 0) {
            ServerWorld world = domain.world;
            float radius = getDomainRadius(domain.caster);
            world.spawnParticles(ParticleTypes.SNOWFLAKE,
                    currentDomainCenter.getX(), currentDomainCenter.getY() + 0.5, currentDomainCenter.getZ(),
                    20, radius * 0.8f, radius * 0.3f, radius * 0.8f, 0.03f);
            if (domain.remainingTicks % config.snowball_particle_interval_ticks == 0) {
                world.spawnParticles(ParticleTypes.ITEM_SNOWBALL,
                        currentDomainCenter.getX(), currentDomainCenter.getY() + 0.2, currentDomainCenter.getZ(),
                        5, radius * 0.3f, 0.1f, radius * 0.3f, 0.01f);
            }
        }
        return false;
    }

    private static float getDomainRadius(ServerPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        int enchantLevel = EnchantmentHelper.getLevel(ModEnchantments.DOMAIN_EXPANSION, stack);
        return SkamConfig.getInstance().weapons.netherite_ice_sword.ice_domain.base_radius + enchantLevel;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            ItemStack stack = user.getStackInHand(hand);
            if (player.isSneaking()) {
                activateIceDomain(player, stack);
            } else {
                fireIceExplosionBeam(player, stack);
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void activateIceDomain(ServerPlayerEntity player, ItemStack stack) {
        if (checkCooldown(player, stack, "domain_ice", SkamConfig.getInstance().weapons.netherite_ice_sword.ice_domain.cooldown_ticks)) {
            ServerWorld world = player.getServerWorld();
            activeIceDomains.add(new IceDomain(world, player)); // 领域中心会跟随玩家，不再需要传递 Vec3d
            float radius = getDomainRadius(player);
            world.spawnParticles(ParticleTypes.SNOWFLAKE,
                    player.getX(), player.getY(), player.getZ(), 700, radius * 0.8f, radius * 0.5f, radius * 0.8f, 0.1f);
            world.spawnParticles(ParticleTypes.ITEM_SNOWBALL,
                    player.getX(), player.getY(), player.getZ(), 100, radius * 0.5f, radius * 0.3f, radius * 0.5f, 0.05f);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.PLAYERS, 1.0f, 0.8f);
        }
    }

    private void fireIceExplosionBeam(ServerPlayerEntity player, ItemStack stack) {
        if (checkCooldown(player, stack, "explosion_ice", SkamConfig.getInstance().weapons.netherite_ice_sword.ice_explosion_beam.cooldown_ticks)) {
            ServerWorld world = player.getServerWorld();
            SkamConfig.NetheriteIceSwordConfig.IceExplosionBeamConfig config = SkamConfig.getInstance().weapons.netherite_ice_sword.ice_explosion_beam;

            Vec3d startPos = player.getEyePos();
            Vec3d direction = player.getRotationVec(1.0F).normalize();
            Vec3d endPos = startPos.add(direction.multiply(config.range));

            // <<< 优化 #1：采用更健壮的光线追踪逻辑，正确处理实体和方块的优先级
            EntityHitResult entityHit = ProjectileUtil.raycast(
                    player, startPos, endPos, new Box(startPos, endPos).expand(1.5),
                    entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.equals(player), config.range * config.range
            );
            HitResult blockHit = world.raycast(new RaycastContext(
                    startPos, endPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player
            ));

            Vec3d finalHitPos;
            if (entityHit != null && blockHit.getType() != HitResult.Type.MISS) {
                finalHitPos = startPos.squaredDistanceTo(entityHit.getPos()) < startPos.squaredDistanceTo(blockHit.getPos()) ? entityHit.getPos() : blockHit.getPos();
            } else if (entityHit != null) {
                finalHitPos = entityHit.getPos();
            } else {
                finalHitPos = blockHit.getType() != HitResult.Type.MISS ? blockHit.getPos() : endPos;
            }

            double distance = startPos.distanceTo(finalHitPos);
            for (double i = 0; i < distance; i += 1) {
                Vec3d particlePos = startPos.add(direction.multiply(i));
                world.spawnParticles(ParticleTypes.SNOWFLAKE,
                        particlePos.x, particlePos.y, particlePos.z, 5, 0.05, 0.05, 0.05, 0.01);
            }
            createIceExplosion(world, player, finalHitPos);
        }
    }

    private void createIceExplosion(ServerWorld world, LivingEntity caster, Vec3d pos) {
        SkamConfig.NetheriteIceSwordConfig.IceExplosionBeamConfig config = SkamConfig.getInstance().weapons.netherite_ice_sword.ice_explosion_beam;

        // 视觉效果，不破坏方块
        world.createExplosion(null, pos.x, pos.y, pos.z, 0.0f, false, World.ExplosionSourceType.NONE);

        // <<< 核心修正 #2：将爆炸伤害也改为 indirectMagic
        var magicDamageSource = caster.getDamageSources().indirectMagic(caster, caster);

        List<LivingEntity> affectedEntities = world.getEntitiesByClass(
                LivingEntity.class,
                Box.from(pos).expand(config.aoe_radius),
                e -> e.isAlive() && e != caster && pos.distanceTo(e.getPos()) <= config.aoe_radius && !(e instanceof PlayerEntity && caster instanceof PlayerEntity)
        );
        affectedEntities.forEach(livingEntity -> {
            livingEntity.damage(magicDamageSource, (float) config.damage);
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, config.slowness_duration_ticks, config.slowness_amplifier));
        });

        // 爆炸粒子效果
        world.spawnParticles(ParticleTypes.SNOWFLAKE,
                pos.x, pos.y + 0.5, pos.z, 200, 2.5, 1.5, 2.5, 0.1);
        world.spawnParticles(ParticleTypes.ITEM_SNOWBALL,
                pos.x, pos.y + 0.5, pos.z, 50, 2.0, 1.0, 2.0, 0.05);
    }

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
        String skillTranslationKey = skillKey.equals("domain_ice") ? "message.skam.skill.ice_domain" : "message.skam.skill.ice_blast";
        Text skillName = Text.translatable(skillTranslationKey);

        // 使用通用的冷却消息翻译键
        player.sendMessage(Text.translatable("message.skam.cooldown", skillName, remainingTicks / 20.0), true);
        return false;
    }


    // <<< 新增功能：允许非玩家实体（如Boss）使用此武器的技能
    public void performEntityRightClick(World world, LivingEntity entity) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            fireEntityIceExplosionBeam(serverWorld, entity);
        }
    }

    private void fireEntityIceExplosionBeam(ServerWorld world, LivingEntity entity) {
        SkamConfig.NetheriteIceSwordConfig.IceExplosionBeamConfig config = SkamConfig.getInstance().weapons.netherite_ice_sword.ice_explosion_beam;

        Vec3d startPos = entity.getEyePos();
        Vec3d direction = entity.getRotationVec(1.0F).normalize();
        Vec3d endPos = startPos.add(direction.multiply(config.range));

        EntityHitResult entityHit = ProjectileUtil.raycast(
                entity, startPos, endPos, new Box(startPos, endPos).expand(1.5),
                target -> target instanceof LivingEntity && target.isAlive() && !target.equals(entity),
                config.range * config.range
        );
        HitResult blockHit = world.raycast(new RaycastContext(
                startPos, endPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity
        ));

        Vec3d finalHitPos;
        if (entityHit != null && blockHit.getType() != HitResult.Type.MISS) {
            finalHitPos = startPos.squaredDistanceTo(entityHit.getPos()) < startPos.squaredDistanceTo(blockHit.getPos()) ? entityHit.getPos() : blockHit.getPos();
        } else if (entityHit != null) {
            finalHitPos = entityHit.getPos();
        } else {
            finalHitPos = blockHit.getType() != HitResult.Type.MISS ? blockHit.getPos() : endPos;
        }

        double distance = startPos.distanceTo(finalHitPos);
        for (double i = 0; i < distance; i += 1) {
            Vec3d particlePos = startPos.add(direction.multiply(i));
            world.spawnParticles(ParticleTypes.SNOWFLAKE,
                    particlePos.x, particlePos.y, particlePos.z, 5, 0.05, 0.05, 0.05, 0.01);
        }

        // 复用 createIceExplosion 逻辑，传入正确的施法者
        createIceExplosion(world, entity, finalHitPos);
    }
    // <<< 新增功能结束

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        SkamConfig.NetheriteIceSwordConfig iceConfig = SkamConfig.getInstance().weapons.netherite_ice_sword;
        int enchantLevel = EnchantmentHelper.getLevel(ModEnchantments.DOMAIN_EXPANSION, stack);
        float currentDomainRadius = iceConfig.ice_domain.base_radius + enchantLevel;

        // --- 修正点在这里 ---
        // 使用正确的状态效果翻译键 'effect.minecraft.slowness'
        Text domainSlownessEffect = Text.translatable("effect.minecraft.slowness")
                .append(" " + (iceConfig.ice_domain.slowness_amplifier + 1))
                .formatted(Formatting.AQUA);
        Text beamSlownessEffect = Text.translatable("effect.minecraft.slowness")
                .append(" " + (iceConfig.ice_explosion_beam.slowness_amplifier + 1))
                .formatted(Formatting.AQUA);
        // --- 修正结束 ---

        // 技能1: 寒冰领域
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.ability.domain").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.description.domain_radius",
                Text.literal(String.valueOf((int)currentDomainRadius)).formatted(Formatting.AQUA)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.description.domain_damage",
                Text.literal(String.valueOf(iceConfig.ice_domain.damage)).formatted(Formatting.RED),
                Text.literal(String.format("%.1f", iceConfig.ice_domain.damage_interval_ticks / 20.0f)).formatted(Formatting.GOLD)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.description.domain_effect",
                domainSlownessEffect,
                Text.literal(String.valueOf(iceConfig.ice_domain.slowness_duration_ticks / 20)).formatted(Formatting.GOLD)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.description.domain_duration_cooldown",
                Text.literal(String.valueOf(iceConfig.ice_domain.duration_ticks / 20)).formatted(Formatting.GOLD),
                Text.literal(String.valueOf(iceConfig.ice_domain.cooldown_ticks / 20)).formatted(Formatting.GOLD)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.description.domain_no_player_damage").formatted(Formatting.GRAY));
        tooltip.add(Text.empty());

        // 技能2: 寒冰爆破
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.ability.beam").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.description.beam_range",
                Text.literal(String.valueOf(iceConfig.ice_explosion_beam.range)).formatted(Formatting.AQUA)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.description.beam_damage",
                Text.literal(String.valueOf(iceConfig.ice_explosion_beam.damage)).formatted(Formatting.RED),
                Text.literal(String.format("%.1f", iceConfig.ice_explosion_beam.aoe_radius)).formatted(Formatting.AQUA)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.description.beam_effect",
                beamSlownessEffect,
                Text.literal(String.valueOf(iceConfig.ice_explosion_beam.slowness_duration_ticks / 20)).formatted(Formatting.GOLD)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.description.beam_cooldown",
                Text.literal(String.valueOf(iceConfig.ice_explosion_beam.cooldown_ticks / 20)).formatted(Formatting.GOLD)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.description.beam_no_player_damage").formatted(Formatting.GRAY));

        // 附魔信息
        if (enchantLevel > 0) {
            tooltip.add(Text.empty());
            tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.enchant.header",
                    Text.literal(String.valueOf(enchantLevel)).formatted(Formatting.LIGHT_PURPLE)
            ).formatted(Formatting.LIGHT_PURPLE));
            tooltip.add(Text.translatable("tooltip.skam.netherite_ice_sword.enchant.description",
                    Text.literal(String.valueOf(enchantLevel)).formatted(Formatting.AQUA)
            ).formatted(Formatting.GRAY));
        }
    }


}
