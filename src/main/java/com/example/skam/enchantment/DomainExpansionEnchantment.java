package com.example.skam.enchantment;

import com.example.skam.item.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class DomainExpansionEnchantment extends Enchantment {

    public DomainExpansionEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinPower(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxPower(int level) {
        return getMinPower(level) + 15;
    }

    @Override
    public int getMaxLevel() {

        return 5;
    }

    @Override
    public boolean isTreasure() {
        return false; }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return true;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return true;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {

        return stack.getItem() instanceof NetheriteFireSword ||
                stack.getItem() instanceof NetheriteIceSword ||
                stack.getItem() instanceof NetheriteLightningSword ||
                stack.getItem() instanceof DiamondFireSword ||
                stack.getItem() instanceof DiamondIceSword ||
                stack.getItem() instanceof DiamondLightningSword;
    }

   @Override
    public int getProtectionAmount(int level, net.minecraft.entity.damage.DamageSource source) {
       return 0;
    }


    @Override
    public boolean canAccept(Enchantment other) {
       return super.canAccept(other);
    }
}