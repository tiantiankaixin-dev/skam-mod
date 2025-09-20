package com.example.skam.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.LoyaltyEnchantment;
import net.minecraft.enchantment.RiptideEnchantment;
import net.minecraft.entity.EquipmentSlot;

public class TridentRiderEnchantment extends Enchantment {

    public TridentRiderEnchantment() {

        super(Rarity.VERY_RARE, EnchantmentTarget.TRIDENT, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        // 附魔所需的最低经验等级
        return 25;
    }

    @Override
    public int getMaxLevel() {
        // 最高附魔等级为 1
        return 1;
    }


    @Override
    protected boolean canAccept(Enchantment other) {
        // 与忠诚和激流附魔互斥，因为它们的功能会产生冲突。
        // 忠诚：三叉戟会飞回来，骑行没有意义。
        // 激流：玩家自己飞出去，而不是三叉戟。
        if (other instanceof LoyaltyEnchantment || other instanceof RiptideEnchantment) {
            return false;
        }
        return super.canAccept(other);
    }
}
