package com.example.skam.item;

import com.example.skam.SkamMod;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
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
import net.minecraft.world.explosion.Explosion;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.config.SkamConfig;
import java.util.ArrayList;
import java.util.List;

public class NetheriteFireSword extends SwordItem {
    private static class FireDomain {
        final ServerWorld world;
        final Vec3d center;
        final ServerPlayerEntity caster;
        int remainingTicks;
        FireDomain(ServerWorld world, Vec3d center, ServerPlayerEntity caster) {
            this.world = world;
            this.center = center;
            this.caster = caster;
            this.remainingTicks = SkamConfig.getInstance().weapons.netherite_fire_sword.fire_domain.duration_ticks;
        }
    }

    private static final List<FireDomain> activeDomains = new ArrayList<>();
    private static final String COOLDOWN_TAG = "NetherFireCD";
    public NetheriteFireSword() {
        super(ToolMaterials.NETHERITE, 8, -2.4F, new Settings().fireproof().maxCount(1));
        ServerTickEvents.END_WORLD_TICK.register(tickingWorld -> {
            if (!tickingWorld.isClient()) {
                activeDomains.removeIf(domain -> {
                    if (domain.world == tickingWorld) {
                        domain.remainingTicks--;
                        return processDomainTick(domain);
                    }
                    return false;
                });
            }
        });
    }

    private static boolean processDomainTick(FireDomain domain) {
        if (domain.remainingTicks <= 0) {
            return true;
        }
        SkamConfig.NetheriteFireSwordConfig.FireDomainConfig config = SkamConfig.getInstance().weapons.netherite_fire_sword.fire_domain;
        Vec3d currentDomainCenter = domain.caster.getPos();
        if (domain.remainingTicks % config.damage_interval_ticks == 0) {
            float radius = getDomainRadius(domain.caster);
            List<LivingEntity> targets = domain.world.getEntitiesByClass(
                    LivingEntity.class,
                    Box.from(currentDomainCenter).expand(radius),
                    e -> currentDomainCenter.distanceTo(e.getPos()) <= radius
                            && e != domain.caster && e.isAlive() && !(e instanceof PlayerEntity)
            );

            // <<< 最终修正：使用“间接魔法伤害”，这是为领域/光环类技能设计的标准伤害类型
            var magicDamageSource = domain.caster.getDamageSources().indirectMagic(domain.caster, domain.caster);

            targets.forEach(entity -> {
                // 使用我们新创建的、正确的魔法伤害源
                entity.damage(magicDamageSource, (float) config.damage);
                entity.setOnFireFor(config.fire_duration_ticks);
            });
        }

        if (domain.remainingTicks % config.particle_interval_ticks == 0) {
            ServerWorld world = domain.world;
            float radius = getDomainRadius(domain.caster);
            world.spawnParticles(ParticleTypes.FLAME,
                    currentDomainCenter.getX(), currentDomainCenter.getY() + 0.5, currentDomainCenter.getZ(),
                    15,
                    radius * 0.7f,
                    radius * 0.2f,
                    radius * 0.7f,
                    0.02f);
            if (domain.remainingTicks % config.damage_interval_ticks == 0) {
                world.spawnParticles(ParticleTypes.LAVA,
                        currentDomainCenter.getX(), currentDomainCenter.getY(), currentDomainCenter.getZ(),
                        5,
                        radius * 0.5f,
                        0.1f,
                        radius * 0.5f,
                        0.0f);
            }
        }

        return false;
    }


    private static float getDomainRadius(ServerPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        int enchantLevel = EnchantmentHelper.getLevel(ModEnchantments.DOMAIN_EXPANSION, stack);
        return SkamConfig.getInstance().weapons.netherite_fire_sword.fire_domain.base_radius + enchantLevel;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            ItemStack stack = user.getStackInHand(hand);

            if (player.isSneaking()) {
                activateFireDomain(player, stack);
            } else {
                fireExplosionBeam(player, stack);
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }
    private void activateFireDomain(ServerPlayerEntity player, ItemStack stack) {
        if (checkCooldown(player, stack, "domain", SkamConfig.getInstance().weapons.netherite_fire_sword.fire_domain.cooldown_ticks)) {
            ServerWorld world = player.getServerWorld();
            activeDomains.add(new FireDomain(world, player.getPos(), player));
            float radius = getDomainRadius(player);
            world.spawnParticles(ParticleTypes.LAVA,
                    player.getX(), player.getY(), player.getZ(),
                    500, radius, 5, radius, 0.5);
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS, 1.0f, 0.5f);
        }
    }
    private void fireExplosionBeam(ServerPlayerEntity player, ItemStack stack) {
        if (checkCooldown(player, stack, "explosion", SkamConfig.getInstance().weapons.netherite_fire_sword.explosion_beam.cooldown_ticks)) {
            ServerWorld world = player.getServerWorld();
            SkamConfig.NetheriteFireSwordConfig.ExplosionBeamConfig config = SkamConfig.getInstance().weapons.netherite_fire_sword.explosion_beam;

            Vec3d startPos = player.getEyePos();
            Vec3d direction = player.getRotationVec(1.0F).normalize();
            Vec3d endPos = startPos.add(direction.multiply(config.range));

            EntityHitResult entityHit = ProjectileUtil.raycast(
                    player, startPos, endPos,
                    new Box(startPos, endPos).expand(1.5),
                    entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.equals(player),
                    config.range * config.range
            );
            HitResult blockHit = world.raycast(new RaycastContext(
                    startPos, endPos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    player
            ));
            Vec3d finalHitPos;
            HitResult.Type hitType = HitResult.Type.MISS;
            if (entityHit != null && blockHit.getType() != HitResult.Type.MISS) {
                double entityDistSq = startPos.squaredDistanceTo(entityHit.getPos());
                double blockDistSq = startPos.squaredDistanceTo(blockHit.getPos());
                if (entityDistSq < blockDistSq) {
                    finalHitPos = entityHit.getPos();
                    hitType = HitResult.Type.ENTITY;
                } else {
                    finalHitPos = blockHit.getPos();
                    hitType = HitResult.Type.BLOCK;
                }
            } else if (entityHit != null) {
                finalHitPos = entityHit.getPos();
                hitType = HitResult.Type.ENTITY;
            } else if (blockHit.getType() != HitResult.Type.MISS) {
                finalHitPos = blockHit.getPos();
                hitType = HitResult.Type.BLOCK;
            } else {
                finalHitPos = endPos;
            }

            double distance = startPos.distanceTo(finalHitPos);
            for (int i = 0; i < distance; i += 2) {
                Vec3d particlePos = startPos.add(direction.multiply(i));
                world.spawnParticles(ParticleTypes.FLAME,
                        particlePos.x, particlePos.y, particlePos.z, 2, 0.1, 0.1, 0.1, 0.02);
            }

            if (hitType != HitResult.Type.MISS) {
                createExplosion(world, player, finalHitPos);
            }
        }
    }

    private void createExplosion(ServerWorld world, ServerPlayerEntity player, Vec3d pos) {
        SkamConfig.NetheriteFireSwordConfig.ExplosionBeamConfig config = SkamConfig.getInstance().weapons.netherite_fire_sword.explosion_beam;

        Explosion explosion = new Explosion(
                world,
                player,
                player.getDamageSources().explosion(player, player),
                null,
                pos.x, pos.y, pos.z,
                (float) config.aoe_radius,
                false,
                Explosion.DestructionType.KEEP
        );
        explosion.affectWorld(true);

        world.getOtherEntities(player, Box.from(pos).expand(config.aoe_radius),
                e -> e instanceof LivingEntity && e.isAlive() && e != player && !(e instanceof PlayerEntity)).forEach(entity -> {
            if (pos.distanceTo(entity.getPos()) <= config.aoe_radius) {
                entity.damage(player.getDamageSources().explosion(player, player), (float) config.damage);
                entity.setOnFireFor(config.fire_duration_ticks);
            }
        });
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
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

        // 使用翻译键获取技能名称
        String skillTranslationKey = skill.equals("domain") ? "message.skam.skill.fire_domain" : "message.skam.skill.flame_blast";
        Text skillName = Text.translatable(skillTranslationKey);

        // 使用通用的冷却消息翻译键
        player.sendMessage(Text.translatable("message.skam.cooldown", skillName, remainingTicks / 20.0), true);
        return false;
    }

    public void performEntityRightClick(World world, LivingEntity entity) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            fireEntityExplosionBeam(serverWorld, entity);
        }
    }

    private void fireEntityExplosionBeam(ServerWorld world, LivingEntity entity) {
        SkamConfig.NetheriteFireSwordConfig.ExplosionBeamConfig config = SkamConfig.getInstance().weapons.netherite_fire_sword.explosion_beam;

        Vec3d startPos = entity.getEyePos();
        Vec3d direction = entity.getRotationVec(1.0F).normalize();
        Vec3d endPos = startPos.add(direction.multiply(config.range));

        EntityHitResult entityHit = ProjectileUtil.raycast(
                entity, startPos, endPos,
                new Box(startPos, endPos).expand(1.5),
                target -> target instanceof LivingEntity && target.isAlive() && !target.equals(entity),
                config.range * config.range
        );

        HitResult blockHit = world.raycast(new RaycastContext(
                startPos, endPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                entity
        ));

        Vec3d finalHitPos;
        HitResult.Type hitType = HitResult.Type.MISS;

        if (entityHit != null && blockHit.getType() != HitResult.Type.MISS) {
            double entityDistSq = startPos.squaredDistanceTo(entityHit.getPos());
            double blockDistSq = startPos.squaredDistanceTo(blockHit.getPos());
            if (entityDistSq < blockDistSq) {
                finalHitPos = entityHit.getPos();
                hitType = HitResult.Type.ENTITY;
            } else {
                finalHitPos = blockHit.getPos();
                hitType = HitResult.Type.BLOCK;
            }
        } else if (entityHit != null) {
            finalHitPos = entityHit.getPos();
            hitType = HitResult.Type.ENTITY;
        } else if (blockHit.getType() != HitResult.Type.MISS) {
            finalHitPos = blockHit.getPos();
            hitType = HitResult.Type.BLOCK;
        } else {
            finalHitPos = endPos;
        }

        double distance = startPos.distanceTo(finalHitPos);

        for (int i = 0; i < distance; i += 2) {
            Vec3d particlePos = startPos.add(direction.multiply(i));
            world.spawnParticles(ParticleTypes.FLAME,
                    particlePos.x, particlePos.y, particlePos.z, 2, 0.1, 0.1, 0.1, 0.02);
        }

        if (hitType != HitResult.Type.MISS) {
            createEntityExplosion(world, entity, finalHitPos);
        }
    }

    private void createEntityExplosion(ServerWorld world, LivingEntity caster, Vec3d pos) {
        SkamConfig.NetheriteFireSwordConfig.ExplosionBeamConfig config = SkamConfig.getInstance().weapons.netherite_fire_sword.explosion_beam;

        Explosion explosion = new Explosion(
                world,
                caster,
                caster.getDamageSources().explosion(caster, caster),
                null,
                pos.x, pos.y, pos.z,
                (float) config.aoe_radius,
                false,
                Explosion.DestructionType.KEEP
        );
        explosion.affectWorld(true);

        world.getOtherEntities(caster, Box.from(pos).expand(config.aoe_radius),
                        e -> e instanceof LivingEntity && e.isAlive() && e != caster)
                .forEach(target -> {
                    if (pos.distanceTo(target.getPos()) <= config.aoe_radius) {
                        if (target instanceof PlayerEntity) {
                            SkamMod.LOGGER.info("Boss explosion beam hit player: " + target.getName().getString());
                        }
                        target.damage(caster.getDamageSources().explosion(caster, caster), (float) config.damage);
                        target.setOnFireFor(config.fire_duration_ticks);
                    }
                });

        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER,
                pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
    }


    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        SkamConfig.NetheriteFireSwordConfig fireConfig = SkamConfig.getInstance().weapons.netherite_fire_sword;
        int enchantLevel = EnchantmentHelper.getLevel(ModEnchantments.DOMAIN_EXPANSION, stack);
        float currentDomainRadius = fireConfig.fire_domain.base_radius + enchantLevel;

        // 技能1: 火焰领域
        tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.ability.domain").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.description.domain_radius",
                Text.literal(String.valueOf((int)currentDomainRadius)).formatted(Formatting.AQUA)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.description.domain_damage",
                Text.literal(String.valueOf(fireConfig.fire_domain.damage)).formatted(Formatting.RED),
                Text.literal(String.valueOf(fireConfig.fire_domain.fire_duration_ticks / 20)).formatted(Formatting.GOLD)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.description.domain_duration_cooldown",
                Text.literal(String.valueOf(fireConfig.fire_domain.duration_ticks / 20)).formatted(Formatting.GOLD),
                Text.literal(String.valueOf(fireConfig.fire_domain.cooldown_ticks / 20)).formatted(Formatting.GOLD)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.description.domain_no_player_damage").formatted(Formatting.GRAY));
        tooltip.add(Text.empty());

        // 技能2: 烈焰轰击
        tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.ability.beam").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.description.beam_range",
                Text.literal(String.valueOf(fireConfig.explosion_beam.range)).formatted(Formatting.AQUA)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.description.beam_damage",
                Text.literal(String.valueOf(fireConfig.explosion_beam.damage)).formatted(Formatting.RED),
                Text.literal(String.valueOf(fireConfig.explosion_beam.fire_duration_ticks / 20)).formatted(Formatting.GOLD)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.description.beam_cooldown",
                Text.literal(String.valueOf(fireConfig.explosion_beam.cooldown_ticks / 20)).formatted(Formatting.GOLD)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.description.beam_no_player_damage").formatted(Formatting.GRAY));

        // 附魔信息
        if (enchantLevel > 0) {
            tooltip.add(Text.empty());
            tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.enchant.header",
                    Text.literal(String.valueOf(enchantLevel)).formatted(Formatting.LIGHT_PURPLE)
            ).formatted(Formatting.LIGHT_PURPLE));
            tooltip.add(Text.translatable("tooltip.skam.netherite_fire_sword.enchant.description",
                    Text.literal(String.valueOf(enchantLevel)).formatted(Formatting.AQUA)
            ).formatted(Formatting.GRAY));
        }
    }


}

