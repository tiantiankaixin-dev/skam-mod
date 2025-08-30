package com.example.skam.util;

import com.example.skam.item.core.CoreType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.stream.StreamSupport;

public class StrengthCoreUtil {

    /**
     * 计算玩家身上所有装备（主手、副手、盔甲）提供的力量核心总伤害乘数。
     * @param player 要检查的玩家
     * @return 总伤害乘数 (例如, 1.2 代表 +20% 伤害)
     */
    public static float calculateTotalMultiplier(PlayerEntity player) {
        // 1. 收集玩家所有需要检查的物品
        Iterable<ItemStack> itemsToCheck = player.getArmorItems();
        ItemStack mainHandStack = player.getMainHandStack();
        ItemStack offHandStack = player.getOffHandStack();

        // 2. 计算所有物品上力量核心的总等级
        int totalLevel = 0;

        // 检查盔甲
        for (ItemStack armorStack : itemsToCheck) {
            if (!armorStack.isEmpty()) {
                totalLevel += getStrengthCoreLevel(armorStack);
            }
        }
        // 检查主手和副手
        totalLevel += getStrengthCoreLevel(mainHandStack);
        totalLevel += getStrengthCoreLevel(offHandStack);

        // 3. 如果总等级为0，直接返回1.0（无加成）
        if (totalLevel == 0) {
            return 1.0f;
        }

        // 4. 从配置中获取每级加成并计算总乘数
        SkamAttributeConfig config = SkamAttributeConfig.get();
        double bonusPerLevel = config.getAttributesFor(CoreType.STRENGTH).damage_multiplier_per_level;
        return 1.0f + (float)(totalLevel * bonusPerLevel);
    }

    /**
     * 获取单个物品上力量核心的等级。
     * @param stack 物品堆栈
     * @return 力量核心的等级，如果没有则返回 0
     */
    private static int getStrengthCoreLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        Map<CoreType, Integer> cores = CoreNbtApplicator.readAppliedCoresFromStack(stack);
        return cores.getOrDefault(CoreType.STRENGTH, 0);
    }
}
