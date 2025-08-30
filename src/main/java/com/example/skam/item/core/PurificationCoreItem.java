// 文件: com/example/skam/item/core/PurificationCoreItem.java
package com.example.skam.item.core;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PurificationCoreItem extends Item implements ICoreItem {

    private final int level;

    public PurificationCoreItem(int level, Settings settings) {
        super(settings);
        this.level = level;
    }

    @Override
    public CoreType getCoreType() {
        return CoreType.PURIFICATION;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        // 添加描述性提示
        tooltip.add(Text.translatable("tooltip.skam.purification_core.description", this.level).formatted(Formatting.GRAY));
        super.appendTooltip(stack, world, tooltip, context);
    }
}
