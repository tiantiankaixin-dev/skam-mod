package com.example.skam.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class SpeedCharmEnchantment extends Enchantment {

    public SpeedCharmEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR, new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }
    @Override
    public int getMaxLevel() {
        return 2;
    }
    @Override
    public int getMinPower(int level) {
        return 5 + (level - 1) * 6;
    }

    @Override
    public int getMaxPower(int level) {
        return this.getMinPower(level) + 20;
    }
}
