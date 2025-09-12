package com.example.skam.item;

import com.example.skam.item.core.CoreType;
import com.example.skam.item.core.ICoreItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class Level5IceCore extends Item implements ICoreItem {
    public Level5IceCore(Settings settings) {
        super(settings.fireproof());
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.l5ice").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.upgrade.snow_blade_1").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.upgrade.snow_blade_2").formatted(Formatting.GRAY));
    }
    @Override
    public CoreType getCoreType() {
        return CoreType.ICE;
    }
    @Override
    public int getLevel() {
        return 5;
    }
}

