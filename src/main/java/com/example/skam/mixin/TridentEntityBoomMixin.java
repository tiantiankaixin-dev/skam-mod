package com.example.skam.mixin;

import com.example.skam.SkamMod;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntity.class)
public abstract class TridentEntityBoomMixin {

    @Inject(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At("TAIL")
    )
    private void onEntityHitExplosion(EntityHitResult entityHitResult, CallbackInfo ci) {
       TridentEntity trident = (TridentEntity) (Object) this;
  SkamMod.createExplosiveImpact(trident);
    }
}

