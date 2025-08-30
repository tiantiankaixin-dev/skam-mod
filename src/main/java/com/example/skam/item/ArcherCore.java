// 文件: com/example/skam/item/ArcherCore.java

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

public class ArcherCore extends Item implements ICoreItem {

    private final int level;

    public ArcherCore(Settings settings, int level) {
        super(settings);
        this.level = level;
    }

    @Override
    public CoreType getCoreType() {
        return CoreType.ARCHER;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    // 你可以留空 appendTooltip，因为 CoreNbtApplicator 会处理 Lore
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("tooltip.skam.archercore").formatted(Formatting.GRAY));

    }
}
