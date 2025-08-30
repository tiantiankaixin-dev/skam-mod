package com.example.skam.mixin;

import net.minecraft.server.command.EnchantCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantCommand.class)
public class EnchantCommandMixin {

    @Inject(method = "execute", at = @At("RETURN"))
    private static void onEnchantExecute(CallbackInfoReturnable<Integer> cir) {
    }
}
