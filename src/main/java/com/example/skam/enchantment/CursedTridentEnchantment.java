package com.example.skam.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;

public class CursedTridentEnchantment extends Enchantment {
    public CursedTridentEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentTarget.TRIDENT, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        // 等级越高，需要的经验越多
        return 15 + (level - 1) * 10;
    }

    @Override
    public int getMaxPower(int level) {
        return super.getMinPower(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        // --- 改动点 ---
        // 将最大等级从 1 提高到 3，你可以根据需要调整
        return 255;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.isOf(Items.TRIDENT);
    }

    @Override
    public boolean isCursed() {
        // "Cursed" 附魔通常有负面效果，如果你的附魔只是变大，可以考虑返回 false
        // 但保留 true 也无妨，这会让它在附魔台更难出现
        return true;
    }

    @Override
    public boolean isTreasure() {
        // 宝藏附魔，意味着只能通过战利品箱、钓鱼、交易等方式获得，无法在附魔台直接获得
        return true;
    }
}
