package com.example.skam.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LightningEntity; // 明确导入，避免歧义
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld; // 使用 ServerWorld 来生成闪电
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult; // 明确导入
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class StaffOfThunderItem extends Item {
    public StaffOfThunderItem(Settings settings) {
        super(settings.maxDamage(250));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // 仅在服务器端执行逻辑
        if (!world.isClient) {
            // 寻找玩家视线内的第一个目标 (20格远)
            HitResult hitResult = user.raycast(20, 1, false);
            Entity initialTarget = null;
            Vec3d startPos = hitResult.getPos(); // 默认起始点为射线末端

            if (hitResult.getType() == HitResult.Type.ENTITY) {
                initialTarget = ((EntityHitResult) hitResult).getEntity();
                // --- 修改点 3：如果击中实体，将起始点设为该实体的位置 ---
                startPos = initialTarget.getPos();
            }

            // 启动连锁闪电
            chainLightning((ServerWorld) world, user, startPos, initialTarget, 4, 50.0f, new ArrayList<>());

            user.getItemCooldownManager().set(this, 50); // 2.5秒冷却
            stack.damage(1, user, p -> p.sendToolBreakStatus(hand));
        }

        return TypedActionResult.success(stack, world.isClient());
    }

    private void chainLightning(ServerWorld world, PlayerEntity caster, Vec3d lastPos, Entity currentTarget, int jumpsLeft, float damage, List<Entity> alreadyHit) {
        if (jumpsLeft <= 0) return;

        // 产生装饰性闪电和音效
        world.playSound(null, lastPos.getX(), lastPos.getY(), lastPos.getZ(), SoundEvents.ITEM_TRIDENT_THUNDER, SoundCategory.WEATHER, 1.0F, 1.0F);
        spawnCosmeticLightning(world, lastPos);

        // 对当前目标造成伤害和麻痹效果
        if (currentTarget instanceof LivingEntity livingTarget && !alreadyHit.contains(currentTarget)) {
            // --- 修改点 2：使用归因于玩家的魔法伤害 ---
            livingTarget.damage(world.getDamageSources().indirectMagic(caster, caster), damage);
            livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30, 4)); // 1.5s 缓慢V
            livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 30, 4)); // 1.5s 挖掘疲劳V
            alreadyHit.add(currentTarget);
        }

        // --- 修改点 1：寻找最近的下一个目标，而不是列表中的第一个 ---
        Box searchBox = new Box(lastPos.getX() - 8, lastPos.getY() - 8, lastPos.getZ() - 8, lastPos.getX() + 8, lastPos.getY() + 8, lastPos.getZ() + 8);
        List<LivingEntity> nearbyEntities = world.getEntitiesByClass(LivingEntity.class, searchBox,
                e -> e.isAlive() && e != caster && !alreadyHit.contains(e));

        LivingEntity nextTarget = null;
        double minDistanceSq = Double.MAX_VALUE;

        for (LivingEntity potentialTarget : nearbyEntities) {
            double distanceSq = potentialTarget.getPos().squaredDistanceTo(lastPos);
            if (distanceSq < minDistanceSq) {
                minDistanceSq = distanceSq;
                nextTarget = potentialTarget;
            }
        }
        // ----------------------------------------------------

        // 如果找到最近的目标，则递归调用
        if (nextTarget != null) {
            // 下一跳的伤害会衰减
            chainLightning(world, caster, nextTarget.getPos(), nextTarget, jumpsLeft - 1, damage * 0.75f, alreadyHit);
        }
    }

    private void spawnCosmeticLightning(World world, Vec3d pos) {
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
        if (lightning != null) {
            lightning.setCosmetic(true); // 不会造成伤害或火焰
            lightning.setPosition(pos);
            world.spawnEntity(lightning);
        }
    }
}
