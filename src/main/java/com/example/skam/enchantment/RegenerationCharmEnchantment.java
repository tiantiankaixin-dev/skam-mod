package com.example.skam.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class RegenerationCharmEnchantment extends Enchantment {

    public RegenerationCharmEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR, new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }


    @Override
    public int getMinPower(int level) {
        if (level <= 2) {
            return 5 + (level - 1) * 6;
        }

        return Integer.MAX_VALUE;
    }


    @Override
    public int getMaxPower(int level) {

        return this.getMinPower(level) + 20;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }
}
