package com.example.skam.item;

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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.config.SkamConfig;

import java.util.ArrayList;
import java.util.List;

public class NetheriteLightningSword extends SwordItem {
    private static class LightningStormData {
        final ServerWorld world;
        final ServerPlayerEntity caster;
        int remainingTicks;
        int nextStrikeCooldown;

        LightningStormData(ServerWorld world, ServerPlayerEntity caster) {
            this.world = world;
            this.caster = caster;
            this.remainingTicks = SkamConfig.getInstance().weapons.netherite_lightning_sword.lightning_storm.duration_ticks;
            this.nextStrikeCooldown = SkamConfig.getInstance().weapons.netherite_lightning_sword.lightning_storm.strike_interval_ticks;
        }
    }
    private static final List<LightningStormData> activeLightningStorms = new ArrayList<>();
    private static final String COOLDOWN_TAG = "NetherLightningCD";

    public NetheriteLightningSword() {
        super(ToolMaterials.NETHERITE, 3, -2.4F, new Settings().maxCount(1));
        ServerTickEvents.END_WORLD_TICK.register(tickingWorld -> {
            if (!tickingWorld.isClient()) {
                activeLightningStorms.removeIf(storm -> {
                    if (storm.world == tickingWorld) {
                        storm.remainingTicks--;
                        return processLightningStormTick(storm);
                    }
                    return false;
                });
            }
        });
    }

    private static boolean processLightningStormTick(LightningStormData storm) {
        if (storm.remainingTicks <= 0) {
            return true;
        }
        SkamConfig.NetheriteLightningSwordConfig.LightningStormConfig config = SkamConfig.getInstance().weapons.netherite_lightning_sword.lightning_storm;

        storm.nextStrikeCooldown--;
        if (storm.nextStrikeCooldown <= 0) {
            storm.nextStrikeCooldown = config.strike_interval_ticks;
            // <<< 修正点 #1：调用新的、修复后的闪电伤害方法
            summonAndDamageWithLightning(storm.world, storm.caster, getStormRadius(storm.caster));
        }

        if (storm.remainingTicks % config.particle_interval_ticks == 0) {
            storm.world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    storm.caster.getX(), storm.caster.getY() + storm.caster.getHeight() / 2.0, storm.caster.getZ(),
                    5, storm.caster.getWidth() * 0.7, storm.caster.getHeight() * 0.4, storm.caster.getWidth() * 0.7, 0.05);
        }
        return false;
    }

    // <<< 修正点 #2：重构雷暴风暴的闪电逻辑
    // 现在这个方法只负责召唤闪电并手动造成伤害，不再依赖 setChanneler
    private static void summonAndDamageWithLightning(ServerWorld world, LivingEntity caster, float radius) {
        SkamConfig.NetheriteLightningSwordConfig config = SkamConfig.getInstance().weapons.netherite_lightning_sword;
        Random random = world.getRandom();
        double angle = random.nextDouble() * 2 * Math.PI;
        double radiusOffset = random.nextDouble() * radius;
        double strikeX = caster.getX() + Math.cos(angle) * radiusOffset;
        double strikeZ = caster.getZ() + Math.sin(angle) * radiusOffset;
        double strikeY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int)Math.floor(strikeX), (int)Math.floor(strikeZ));
        Vec3d strikePos = new Vec3d(strikeX, strikeY, strikeZ);

        // 召唤一道纯视觉的闪电
        LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
        lightning.setPosition(strikePos);
        world.spawnEntity(lightning);

        // 手动创建范围伤害，伤害来源可以被正确追踪
        List<LivingEntity> targets = world.getEntitiesByClass(
                LivingEntity.class,
                Box.from(strikePos).expand(config.lightning_beam.aoe_on_block_radius),
                e -> e.isAlive() && e != caster && !(e instanceof PlayerEntity && caster instanceof PlayerEntity)
        );

        var magicDamageSource = caster.getDamageSources().indirectMagic(caster, caster);
        targets.forEach(target -> {
            target.damage(magicDamageSource, (float) config.lightning_storm.damage_per_strike);
        });
    }

    private static float getStormRadius(ServerPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        int enchantLevel = EnchantmentHelper.getLevel(ModEnchantments.DOMAIN_EXPANSION, stack);
        return SkamConfig.getInstance().weapons.netherite_lightning_sword.lightning_storm.base_radius + enchantLevel;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            ItemStack stack = user.getStackInHand(hand);
            if (player.isSneaking()) {
                activateLightningStorm(player, stack);
            } else {
                if (checkCooldown(player, stack, "beam_lightning", SkamConfig.getInstance().weapons.netherite_lightning_sword.lightning_beam.cooldown_ticks)) {
                    createLightningBeam(player.getServerWorld(), player);
                }
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void activateLightningStorm(ServerPlayerEntity player, ItemStack stack) {
        if (checkCooldown(player, stack, "storm_lightning", SkamConfig.getInstance().weapons.netherite_lightning_sword.lightning_storm.cooldown_ticks)) {
            ServerWorld world = player.getServerWorld();
            activeLightningStorms.add(new LightningStormData(world, player));
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.PLAYERS, 1.5f, 0.7f);
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    player.getX(), player.getY() + player.getHeight()/2.0, player.getZ(),
                    150, player.getWidth() * 0.5f, player.getHeight() * 0.5f, player.getWidth() * 0.5f, 0.25f);
        }
    }

    public void performEntityRightClick(World world, LivingEntity entity) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            createLightningBeam(serverWorld, entity);
        }
    }

    private void createLightningBeam(ServerWorld world, LivingEntity caster) {
        SkamConfig.NetheriteLightningSwordConfig.LightningBeamConfig config = SkamConfig.getInstance().weapons.netherite_lightning_sword.lightning_beam;

        Vec3d startPos = caster.getEyePos();
        Vec3d direction = caster.getRotationVec(1.0F).normalize();
        Vec3d endPos = startPos.add(direction.multiply(config.range));

        EntityHitResult entityHit = ProjectileUtil.raycast(
                caster, startPos, endPos, new Box(startPos, endPos).expand(0.5),
                entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.equals(caster), config.range * config.range
        );
        HitResult blockHit = world.raycast(new RaycastContext(
                startPos, endPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, caster
        ));

        Vec3d finalHitPos;
        LivingEntity directHitEntity = null;
        if (entityHit != null && blockHit.getType() != HitResult.Type.MISS) {
            if (startPos.squaredDistanceTo(entityHit.getPos()) < startPos.squaredDistanceTo(blockHit.getPos())) {
                finalHitPos = entityHit.getPos();
                directHitEntity = (LivingEntity) entityHit.getEntity();
            } else {
                finalHitPos = blockHit.getPos();
            }
        } else if (entityHit != null) {
            finalHitPos = entityHit.getPos();
            directHitEntity = (LivingEntity) entityHit.getEntity();
        } else {
            finalHitPos = blockHit.getType() != HitResult.Type.MISS ? blockHit.getPos() : endPos;
        }

        double distance = startPos.distanceTo(finalHitPos);
        for (double i = 0; i < distance; i += 0.4) {
            world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    startPos.x + direction.x * i, startPos.y + direction.y * i, startPos.z + direction.z * i,
                    1, 0, 0, 0, 0.0D);
        }

        world.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL, SoundCategory.PLAYERS, 0.7f, 1.3f);

        if (directHitEntity != null && !(directHitEntity instanceof PlayerEntity && caster instanceof PlayerEntity)) {
            directHitEntity.damage(caster.getDamageSources().indirectMagic(caster, caster), (float) config.potion_damage);
        }

        // <<< 修正点 #3：移除 setChanneler，替换为手动伤害
        if (distance > 1) {
            // 召唤视觉闪电
            LightningEntity lightning = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
            lightning.setPosition(finalHitPos);
            world.spawnEntity(lightning);

            // 手动造成范围伤害
            List<LivingEntity> targets = world.getEntitiesByClass(
                    LivingEntity.class,
                    Box.from(finalHitPos).expand(config.aoe_on_block_radius),
                    e -> e.isAlive() && e != caster && !(e instanceof PlayerEntity && caster instanceof PlayerEntity)
            );
            var magicDamageSource = caster.getDamageSources().indirectMagic(caster, caster);
            targets.forEach(target -> {
                target.damage(magicDamageSource, (float) config.lightning_damage);
            });
        }
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
        String skillTranslationKey = skillKey.equals("storm_lightning") ? "message.skam.skill.lightning_storm" : "message.skam.skill.lightning_beam";
        Text skillName = Text.translatable(skillTranslationKey);

        // 使用通用的冷却消息翻译键
        player.sendMessage(Text.translatable("message.skam.cooldown", skillName, remainingTicks / 20.0), true);
        return false;
    }


    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        SkamConfig.NetheriteLightningSwordConfig lightningConfig = SkamConfig.getInstance().weapons.netherite_lightning_sword;
        int enchantLevel = EnchantmentHelper.getLevel(ModEnchantments.DOMAIN_EXPANSION, stack);
        float currentStormRadius = lightningConfig.lightning_storm.base_radius + enchantLevel;

        // 技能1: 闪电射线
        tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.ability.beam").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.description.beam_range",
                Text.literal(String.valueOf(lightningConfig.lightning_beam.range)).formatted(Formatting.AQUA)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.description.beam_lightning_damage",
                Text.literal(String.valueOf(lightningConfig.lightning_beam.lightning_damage)).formatted(Formatting.RED),
                Text.literal(String.valueOf(lightningConfig.lightning_beam.aoe_on_block_radius)).formatted(Formatting.AQUA)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.description.beam_direct_damage",
                Text.literal(String.valueOf(lightningConfig.lightning_beam.potion_damage)).formatted(Formatting.RED)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.description.beam_cooldown",
                Text.literal(String.valueOf(lightningConfig.lightning_beam.cooldown_ticks / 20)).formatted(Formatting.GOLD)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.description.beam_no_player_damage").formatted(Formatting.GRAY));
        tooltip.add(Text.empty());

        // 技能2: 雷暴风暴
        tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.ability.storm").formatted(Formatting.YELLOW));
        tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.description.storm_radius",
                Text.literal(String.valueOf((int)currentStormRadius)).formatted(Formatting.AQUA)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.description.storm_damage",
                Text.literal(String.valueOf(lightningConfig.lightning_storm.damage_per_strike)).formatted(Formatting.RED)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.description.storm_duration_cooldown",
                Text.literal(String.valueOf(lightningConfig.lightning_storm.duration_ticks / 20)).formatted(Formatting.GOLD),
                Text.literal(String.valueOf(lightningConfig.lightning_storm.cooldown_ticks / 20)).formatted(Formatting.GOLD)
        ).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.description.storm_no_player_damage").formatted(Formatting.GRAY));

        // 附魔信息
        if (enchantLevel > 0) {
            tooltip.add(Text.empty());
            tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.enchant.header",
                    Text.literal(String.valueOf(enchantLevel)).formatted(Formatting.LIGHT_PURPLE)
            ).formatted(Formatting.LIGHT_PURPLE));
            tooltip.add(Text.translatable("tooltip.skam.netherite_lightning_sword.enchant.description",
                    Text.literal(String.valueOf(enchantLevel)).formatted(Formatting.AQUA)
            ).formatted(Formatting.GRAY));
        }
    }

}
