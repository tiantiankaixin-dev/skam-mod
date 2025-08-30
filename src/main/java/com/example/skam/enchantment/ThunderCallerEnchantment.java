package com.example.skam.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;

public class ThunderCallerEnchantment extends Enchantment {

    public ThunderCallerEnchantment() {

        super(Rarity.VERY_RARE, EnchantmentTarget.TRIDENT, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }


    @Override
    public int getMaxLevel() {
        return 1;
    }


    @Override
    public int getMinPower(int level) {
        return 25;
    }


    @Override
    public int getMaxPower(int level) {
        return 50;
    }


    @Override
    public boolean isTreasure() {
        return true;
    }


    @Override
    public boolean canAccept(Enchantment other) {

        return super.canAccept(other) && other != Enchantments.CHANNELING && other != Enchantments.RIPTIDE;
    }


    @Override
    public boolean isAcceptableItem(ItemStack stack) {
        return stack.getItem() instanceof TridentItem;
    }
}
