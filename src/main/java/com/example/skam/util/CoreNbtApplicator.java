// 文件: com.example.skam.util/CoreNbtApplicator.java
package com.example.skam.util;

import com.example.skam.item.core.CoreType;
import com.example.skam.item.core.ICoreItem;
// ... (其他 imports)
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import com.example.skam.potion.nbt.CoreBonusData;
import com.example.skam.potion.nbt.NbtInjector;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.LinkedHashMap;

/**
 * 主API类，用于将核心效果应用到物品上。
 * 它协调 NbtInjector 和 CoreTooltipSystem。
 */
public class CoreNbtApplicator {

    /**
     * 尝试将一个核心物品应用到目标物品上。
     * 这个方法包含了所有验证逻辑（容量、最大等级等）。
     * 你应该在所有核心应用场景中调用此方法。
     *
     * @param targetStack 目标物品 (如武器、盔甲)
     * @param coreStack   要应用的核心物品
     * @return 如果应用成功，返回 true；否则返回 false。
     */
    public static boolean tryApplyCore(ItemStack targetStack, ItemStack coreStack) {
        // 1. 验证输入
        if (targetStack.isEmpty() || coreStack.isEmpty() || !(coreStack.getItem() instanceof ICoreItem coreItem)) {
            return false;
        }

        CoreType typeToApply = coreItem.getCoreType();
        int levelToAdd = coreItem.getLevel();

        // 2. 获取当前状态
        Map<CoreType, Integer> existingCores = readAppliedCoresFromStack(targetStack);
        int currentLevel = existingCores.getOrDefault(typeToApply, 0);
        int currentTotalLevel = existingCores.values().stream().mapToInt(Integer::intValue).sum();
        int maxCapacity = getMaxCoreCapacity(targetStack);

        // 3. 执行验证
        // 3a. 检查核心容量
        if (currentTotalLevel + levelToAdd > maxCapacity) {
            // 可在此处给玩家发送消息，如 "核心容量不足！"
            return false;
        }

        // 3b. 检查该类型的核心是否已达到最大等级
        if (currentLevel + levelToAdd > typeToApply.getMaxLevel()) {
            // 可在此处给玩家发送消息，如 "吸血核心已达到最大等级！"
            return false;
        }

        // 4. 执行修改
        writeCoreToNbt(targetStack, typeToApply, levelToAdd);

        // 5. 更新所有属性和Lore
        applyCoreModifications(targetStack);

        return true;
    }

    public static boolean tryPurify(ItemStack targetStack, ItemStack coreStack) {
        // 1. 验证
        if (targetStack.isEmpty() || coreStack.isEmpty() || !(coreStack.getItem() instanceof ICoreItem coreItem) || coreItem.getCoreType() != CoreType.PURIFICATION) {
            return false;
        }
        Map<CoreType, Integer> appliedCores = readAppliedCoresFromStack(targetStack);
        if (appliedCores.isEmpty()) {
            return false; // 目标物品上没有核心可移除
        }
        int purificationPower = coreItem.getLevel();
        int initialTotalLevel = appliedCores.values().stream().mapToInt(Integer::intValue).sum();
        // 2. 执行净化
        // 使用 LinkedHashMap 的迭代器来保证移除顺序（从最先镶嵌的开始）
        Iterator<Map.Entry<CoreType, Integer>> iterator = appliedCores.entrySet().iterator();
        while (iterator.hasNext() && purificationPower > 0) {
            Map.Entry<CoreType, Integer> entry = iterator.next();
            int currentLevel = entry.getValue();
            int levelsToRemove = Math.min(purificationPower, currentLevel);
            int newLevel = currentLevel - levelsToRemove;
            purificationPower -= levelsToRemove;
            if (newLevel <= 0) {
                iterator.remove(); // 完全移除
            } else {
                entry.setValue(newLevel); // 更新等级
            }
        }
        // 3. 检查是否有变化
        int finalTotalLevel = appliedCores.values().stream().mapToInt(Integer::intValue).sum();
        if (finalTotalLevel >= initialTotalLevel) {
            return false; // 没有移除任何等级
        }

        // 4. 应用修改
        writeAllCoresToNbt(targetStack, appliedCores);
        applyCoreModifications(targetStack);

        return true;
    }
    // --- 创建一个新的辅助方法来一次性写入所有核心 ---
    // 这对于净化操作（需要重写整个列表）非常有用
    private static void writeAllCoresToNbt(ItemStack stack, Map<CoreType, Integer> cores) {
        NbtCompound skamNbt = stack.getOrCreateSubNbt("skam_mods");

        if (cores == null || cores.isEmpty()) {
            skamNbt.remove("cores");
            if (skamNbt.isEmpty()) {
                stack.removeSubNbt("skam_mods");
            }
            return;
        }
        NbtList coreList = new NbtList();
        for (Map.Entry<CoreType, Integer> entry : cores.entrySet()) {
            NbtCompound coreTag = new NbtCompound();
            coreTag.putString("type", entry.getKey().name());
            coreTag.putInt("level", entry.getValue());
            coreList.add(coreTag);
        }
        skamNbt.put("cores", coreList);
    }


    /**
     * 对物品应用所有核心修改，包括NBT数据和Lore面板。
     * 这是你应该从外部调用的主要方法。
     * @param stack The ItemStack to modify.
     */
    public static void applyCoreModifications(ItemStack stack) {
        // 1. 从NBT读取已应用的核心
        Map<CoreType, Integer> appliedCores = readAppliedCoresFromStack(stack);
        // 2. 计算总属性加成
        CoreBonusData bonusData = NbtInjector.calculateTotalBonuses(appliedCores);
        // 3. 应用NBT数据 (AttributeModifiers, 自定义标签等)
        NbtInjector.apply(stack, bonusData);
        // 4. 【关键改动】移除旧的 updateLore 调用
        // CoreTooltipSystem.updateLore(stack, appliedCores, bonusData); // <-- 删除这一行
        // 5. 【可选但推荐】清理旧的、由我们自己写入的Lore，以防万一
        if (stack.hasNbt() && stack.getNbt().contains("display", NbtElement.COMPOUND_TYPE)) {
            NbtCompound display = stack.getNbt().getCompound("display");
            // 检查Lore是否由我们的旧系统生成（可以找一个独特的标识，比如"核心容量"）
            // 简单起见，这里先不实现复杂的检查，直接移除所有Lore。
            // 注意：这可能会移除其他mod或原版添加的lore，更安全的做法是只移除我们自己的行。
            // 但对于一个新系统，这是最简单的迁移方法。
            // display.remove("Lore"); // 如果你确定要强制清除所有lore，可以取消这行注释
        }
    }

    /**
     * 将指定的核心和等级写入到物品的NBT中。
     * 这个方法会正确地合并等级。
     * @param stack The ItemStack to modify.
     * @param type  The CoreType to add/update.
     * @param levelToAdd The level to add to the existing core level.
     */
    private static void writeCoreToNbt(ItemStack stack, CoreType type, int levelToAdd) {
        NbtCompound skamNbt = stack.getOrCreateSubNbt("skam_mods");
        NbtList coreList = skamNbt.getList("cores", NbtElement.COMPOUND_TYPE);
        if (!skamNbt.contains("cores", NbtElement.LIST_TYPE)) {
            skamNbt.put("cores", coreList);
        }

        boolean found = false;
        for (NbtElement element : coreList) {
            if (element instanceof NbtCompound coreTag) {
                String typeStr = coreTag.getString("type");
                if (Objects.equals(typeStr, type.name())) {
                    int currentLevel = coreTag.getInt("level");
                    coreTag.putInt("level", currentLevel + levelToAdd);
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            NbtCompound newCoreTag = new NbtCompound();
            newCoreTag.putString("type", type.name());
            newCoreTag.putInt("level", levelToAdd);
            coreList.add(newCoreTag);
        }
    }


    // --- 以下方法保持不变 ---

    public static Map<CoreType, Integer> readAppliedCoresFromStack(ItemStack stack) {
        Map<CoreType, Integer> cores = new LinkedHashMap<>();
        if (!stack.hasNbt() || !stack.getNbt().contains("skam_mods", NbtElement.COMPOUND_TYPE)) {
            return cores;
        }
        NbtCompound skamNbt = stack.getNbt().getCompound("skam_mods");
        if (skamNbt.contains("cores", NbtElement.LIST_TYPE)) {
            NbtList coreList = skamNbt.getList("cores", NbtElement.COMPOUND_TYPE);
            for (NbtElement element : coreList) {
                NbtCompound coreTag = (NbtCompound) element;
                try {
                    CoreType type = CoreType.valueOf(coreTag.getString("type").toUpperCase());
                    int level = coreTag.getInt("level");
                    cores.merge(type, level, Integer::sum);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        cores.remove(CoreType.PURIFICATION); // 纯化核心不应显示
        return cores;
    }

    public static int getMaxCoreCapacity(ItemStack stack) {
        if (stack.hasNbt()) {
            NbtCompound rootNbt = stack.getNbt();
            if (rootNbt != null && rootNbt.contains("skam_mods", NbtElement.COMPOUND_TYPE)) {
                NbtCompound skamNbt = rootNbt.getCompound("skam_mods");
                if (skamNbt.contains("max_capacity", NbtElement.INT_TYPE)) {
                    return skamNbt.getInt("max_capacity");
                }
            }
        }
        return 0;
    }

}
