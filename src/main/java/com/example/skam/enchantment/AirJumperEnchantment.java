package com.example.skam.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class AirJumperEnchantment extends Enchantment {
    public AirJumperEnchantment() {
        super(Enchantment.Rarity.VERY_RARE, EnchantmentTarget.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
    }

    @Override
    public int getMaxLevel() {
        return 8;
    }

    @Override
    public int getMinPower(int level) {

        return 15 + (level - 1) * 10;
    }

    @Override
    public int getMaxPower(int level) {

        return this.getMinPower(level) + 50;
    }
}
