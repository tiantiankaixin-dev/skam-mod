package com.example.skam.item.core;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 一个通用的核心物品类，用于所有类型的核心。
 */
public class CoreItem extends Item implements ICoreItem {

    private final CoreType coreType;
    private final int level;

    public CoreItem(Settings settings, CoreType coreType, int level) {
        super(settings);
        this.coreType = coreType;
        this.level = level;
    }

    @Override
    public CoreType getCoreType() {
        return this.coreType;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // 在物品提示框中显示核心类型和等级
        tooltip.add(Text.translatable("tooltip.skam.core.type", Text.translatable(coreType.getNameTranslationKey()).formatted(coreType.getColor())).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.core.level", this.level).formatted(Formatting.GRAY));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
