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

public class Level2LightningCore extends Item implements ICoreItem {
    public Level2LightningCore(Settings settings) {
        super(settings.fireproof());
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.l2lightning").formatted(Formatting.GRAY));

    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
    @Override
    public CoreType getCoreType() {
        return CoreType.LIGHTNING;
    }
    @Override
    public int getLevel() {
        return 2;
    }
}
