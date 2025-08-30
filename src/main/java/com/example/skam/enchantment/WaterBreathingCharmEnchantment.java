package com.example.skam.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class WaterBreathingCharmEnchantment extends Enchantment {

    public WaterBreathingCharmEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.ARMOR, new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});
    }
    @Override
    public int getMaxLevel() {
        return 1;
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

