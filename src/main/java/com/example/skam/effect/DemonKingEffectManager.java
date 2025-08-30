// File Path: src/main/java/com/example/skam/effect/DemonKingEffectManager.java
package com.example.skam.effect;

import com.example.skam.effect.ModStatusEffects;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries; // <- 新增的正确导入
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DemonKingEffectManager {

    private static final String ORIGINAL_ENCHANTS_KEY = "skam.original_enchantments";

    public static void tickPlayerInventory(ServerPlayerEntity player) {
        boolean hasEffect = player.hasStatusEffect(ModStatusEffects.DEMON_KING);
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);

            // 如果玩家没有效果，但物品被修改过，则恢复它
            if (!hasEffect) {
                if (isStackModified(stack)) {
                    restoreEnchantments(stack);
                }
                continue; // 处理下一个物品
            }

            // --- 从这里开始，玩家肯定有效果 ---

            // 如果物品是空的或没有附魔，但有我们的标签，清理它
            if (stack.isEmpty() || !stack.hasEnchantments()) {
                if (isStackModified(stack)) {
                    restoreEnchantments(stack); // restore会移除标签
                }
                continue;
            }

            // 玩家有效果，且物品有附魔，处理增强逻辑
            handleAmplification(stack);
        }
    }

    /**
     * 处理物品附魔的增强逻辑。
     * 这个方法现在更加健壮，能处理玩家通过铁砧等方式修改附魔的情况。
     */
    private static void handleAmplification(ItemStack stack) {
        Map<Enchantment, Integer> currentRealEnchantments = EnchantmentHelper.get(stack);
        Map<Enchantment, Integer> storedBaseEnchantments = getOriginalEnchantments(stack);

        // 从当前附魔反推基础附魔，这是为了检测玩家是否通过铁砧修改了物品
        Map<Enchantment, Integer> derivedBaseEnchantments = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : currentRealEnchantments.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();

            // 检查这个附魔是否在我们已知的备份里
            if (storedBaseEnchantments.containsKey(enchant)) {
                // 如果在，我们假设它的基础等级就是备份里的等级
                derivedBaseEnchantments.put(enchant, storedBaseEnchantments.get(enchant));
            } else {
                // 如果不在，说明这是个新附魔！它的基础等级就是它自己。
                derivedBaseEnchantments.put(enchant, level);
            }
        }

        // 如果推导出的基础附魔和存储的不一样，说明物品被修改了（如添加了新附魔）
        // 我们需要更新备份！
        if (!derivedBaseEnchantments.equals(storedBaseEnchantments)) {
            saveOriginalEnchantments(stack, derivedBaseEnchantments);
            storedBaseEnchantments = derivedBaseEnchantments;
        }

        // 计算出目标（增强后）的附魔状态
        Map<Enchantment, Integer> amplifiedEnchantments = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : storedBaseEnchantments.entrySet()) {
            amplifiedEnchantments.put(entry.getKey(), entry.getValue() * 2);
        }

        // 按需更新：只有在当前附魔不等于目标附魔时才写入，避免闪烁
        if (!currentRealEnchantments.equals(amplifiedEnchantments)) {
            EnchantmentHelper.set(amplifiedEnchantments, stack);
        }
    }

    /**
     * 将物品恢复到其原始附魔状态，并移除标记。
     */
    private static void restoreEnchantments(ItemStack stack) {
        if (!isStackModified(stack)) return;

        Map<Enchantment, Integer> originalEnchantments = getOriginalEnchantments(stack);
        EnchantmentHelper.set(originalEnchantments, stack);

        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            nbt.remove(ORIGINAL_ENCHANTS_KEY);
            if (nbt.isEmpty()) {
                stack.setNbt(null);
            }
        }
    }

    // --- NBT 读写辅助方法 ---

    private static boolean isStackModified(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().contains(ORIGINAL_ENCHANTS_KEY, NbtCompound.LIST_TYPE);
    }

    private static void saveOriginalEnchantments(ItemStack stack, Map<Enchantment, Integer> enchantments) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList nbtList = new NbtList();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            // 使用 Registries.ENCHANTMENT.getId 来安全获取ID
            Identifier id = Registries.ENCHANTMENT.getId(entry.getKey());
            if (id != null) {
                NbtCompound enchantNbt = new NbtCompound();
                enchantNbt.putString("id", id.toString());
                enchantNbt.putShort("lvl", entry.getValue().shortValue());
                nbtList.add(enchantNbt);
            }
        }
        nbt.put(ORIGINAL_ENCHANTS_KEY, nbtList);
    }

    private static Map<Enchantment, Integer> getOriginalEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        if (!isStackModified(stack)) return enchantments;

        NbtList nbtList = stack.getNbt().getList(ORIGINAL_ENCHANTS_KEY, NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound enchantNbt = nbtList.getCompound(i);
            Identifier id = new Identifier(enchantNbt.getString("id"));
            // 使用 Registries.ENCHANTMENT.get 来从ID获取附魔对象
            Enchantment enchantment = Registries.ENCHANTMENT.get(id); // <-- 已修正
            if (enchantment != null) {
                enchantments.put(enchantment, enchantNbt.getShort("lvl") & 0xFFFF);
            }
        }
        return enchantments;
    }
}
