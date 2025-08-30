package com.example.skam.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;

public class TimeLagThornEnchantment extends Enchantment {
    public TimeLagThornEnchantment() {

        super(Rarity.VERY_RARE, EnchantmentTarget.TRIDENT, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {

        return 5;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {

        return stack.getItem() instanceof TridentItem;
    }

}
