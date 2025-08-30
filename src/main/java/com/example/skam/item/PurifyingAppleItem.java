package com.example.skam.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PurifyingAppleItem extends Item {

    // --- 史诗级强化的食物组件 ---
    // 为了对比，附魔金苹果的效果是：
    // - 饥饿值: 4
    // - 饱和度: 9.6 (1.2f * 2 * 4) -> 实际上是饱和度修正值2.4f
    // - 效果: 生命恢复II(20s), 伤害吸收IV(2m), 抗性提升I(5m), 防火I(5m)

    private static final FoodComponent FOOD_COMPONENT = new FoodComponent.Builder()
            .hunger(6) // 1. 增强: 饥饿值恢复从4点提升到6点
            .saturationModifier(3.0f) // 2. 史诗级增强: 饱和度修正值提升到3.0f (提供 6 * 3.0 * 2 = 36点饱和度), 远超附魔金苹果的9.6点
            .alwaysEdible() // 无论饥饿与否都能吃
            .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 30 * 20, 3), 1.0f) // 3. 增强: 生命恢复 IV (30秒)，等级和时间均超越附魔金苹果的恢复II(20秒)
            .statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 3 * 60 * 20, 4), 1.0f) // 4. 增强: 伤害吸收 V (3分钟)，等级更高，时间更长
            .statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 6 * 60 * 20, 1), 1.0f) // 5. 增强: 抗性提升 II (6分钟)，等级更高，时间更长
            .statusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 6 * 60 * 20, 0), 1.0f) // 6. 增强: 防火 I (6分钟)，时间更长
            .statusEffect(new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 10 * 60 * 20, 4), 1.0f) // 7. 核心增强: 生命提升 V (10分钟), 永久增加10颗心(20点血)的血量上限，这是附魔金苹果完全没有的顶级生存Buff！
            .statusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 3 * 60 * 20, 1), 1.0f) // 8. 新增攻击Buff: 力量 II (3分钟), 大幅提升近战伤害，攻守兼备！
            .build();

    public PurifyingAppleItem(Settings settings) {
        super(settings.food(FOOD_COMPONENT));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true; // 保持附魔光效
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        // --- 核心逻辑：保持不变 ---
        // 在应用任何新的增益效果之前，首先净化所有负面效果。
        if (!world.isClient) {
            List<StatusEffectInstance> activeEffects = new ArrayList<>(user.getStatusEffects());
            for (StatusEffectInstance effectInstance : activeEffects) {
                // 判断效果是否为有害类别 (DEBUFF)
                if (effectInstance.getEffectType().getCategory() == StatusEffectCategory.HARMFUL) {
                    // 移除该有害效果
                    user.removeStatusEffect(effectInstance.getEffectType());
                }
            }
        }

        // 调用父类方法来应用上面定义的FOOD_COMPONENT中的所有效果，并消耗物品
        return super.finishUsing(stack, world, user);
    }
}
