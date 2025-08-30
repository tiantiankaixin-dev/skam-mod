package com.example.skam.mixin;

import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TridentEntity.class)
public interface TridentEntityAccessor {

    @Invoker("asItemStack")
    ItemStack invokeAsItemStack();
}
