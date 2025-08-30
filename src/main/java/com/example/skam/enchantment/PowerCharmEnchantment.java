package com.example.skam.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class PowerCharmEnchantment extends Enchantment {


    public PowerCharmEnchantment() {

        super(Enchantment.Rarity.VERY_RARE, EnchantmentTarget.ARMOR, new EquipmentSlot[]{
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        });
    }


    @Override
    public int getMaxLevel() {
        return 2;
    }


    @Override
    public int getMinPower(int level) {

        return level * 3;
    }


    @Override
    public int getMaxPower(int level) {
        return this.getMinPower(level) + 2; // 提供一个20级的范围
    }

    @Override
    public boolean isTreasure() {
        return false;
    }
}
