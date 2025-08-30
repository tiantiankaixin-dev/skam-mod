package com.example.skam.mixin;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DispenserBlock.class)
public interface DispenserBlockAccessor {
    @Invoker("getBehaviorForItem")
    DispenserBehavior callGetBehaviorForItem(ItemStack stack); // <<< 移除了 'static' 关键字
}
