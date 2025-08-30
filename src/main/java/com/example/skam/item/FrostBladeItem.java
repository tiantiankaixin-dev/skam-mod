package com.example.skam.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld; // 导入 ServerWorld
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class FrostBladeItem extends SwordItem {
    public FrostBladeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    // 被动技能：冻结之触
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.getWorld().isClient()) {
            // 给予3秒冻结效果
            target.setFrozenTicks(60);
        }
        return super.postHit(stack, target, attacker);
    }

    // 主动技能：冰霜新星
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_PLAYER_HURT_FREEZE, SoundCategory.PLAYERS, 1.0f, 1.0f);
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.SNOWFLAKE,
                    user.getX(), user.getY() + 1, user.getZ(),
                    50,
                    2.0, 0.5, 2.0,
                    0.0);
            Box areaOfEffect = new Box(user.getBlockPos()).expand(5);
            List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, areaOfEffect, e -> e != user && e.isAlive());
            for (LivingEntity entity : entities) {
                // --- 这是关键的修改 ---
                // 使用归因于玩家的间接魔法伤害，这是最适合法术类技能的伤害类型
                entity.damage(user.getDamageSources().indirectMagic(user, user), 2.0f);
                // ---------------------

                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 3)); // 缓慢IV, 5秒
            }
            user.getItemCooldownManager().set(this, 100);
            stack.damage(5, user, p -> p.sendToolBreakStatus(hand));
        }
        return TypedActionResult.success(stack, world.isClient());
    }
}
