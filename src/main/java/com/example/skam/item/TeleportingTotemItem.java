package com.example.skam.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;

public class TeleportingTotemItem extends Item {

    public TeleportingTotemItem(Settings settings) {
        super(settings.maxCount(1).rarity(Rarity.RARE));
    }
    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
