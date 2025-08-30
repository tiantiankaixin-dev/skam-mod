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

public class Level4LightningCore extends Item implements ICoreItem {
    public Level4LightningCore(Settings settings) {
        super(settings.fireproof());
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.l4lightning").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7用于在锻造台将 §f雷霆神剑").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("§7升级为 §b雷霆征伐之刃").formatted(Formatting.GRAY));

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
        return 4;
    }
}