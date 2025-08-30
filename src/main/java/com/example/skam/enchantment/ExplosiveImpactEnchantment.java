package com.example.skam.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;

public class ExplosiveImpactEnchantment extends Enchantment {
    public ExplosiveImpactEnchantment(Rarity weight, EquipmentSlot... slotTypes) {
        super(weight, EnchantmentTarget.TRIDENT, slotTypes);
    }


    @Override
    public int getMinPower(int level) {
        return 10 + (level - 1) * 15; // 等级越高，需要的经验等级越高
    }


    @Override
    public int getMaxPower(int level) {
        return super.getMinPower(level) + 50;
    }


    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean isAcceptableItem(ItemStack stack) {
       return stack.getItem() instanceof TridentItem;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }
    @Override
    public boolean canAccept(Enchantment other)
    {
       if
        (other == net.minecraft.enchantment.Enchantments.RIPTIDE) {
            return false
                    ;
        }
        return super.canAccept(other);
    }

}
