package com.example.skam.mixin;

import com.example.skam.util.ModUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin {
  @Inject(method = "onBlockHit", at = @At("TAIL"))
    private void onBlockHitMixin(BlockHitResult blockHitResult, CallbackInfo ci) {
       PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;
   if (projectile.getType() == EntityType.TRIDENT) {
            TridentEntity trident = (TridentEntity) projectile;
            ModUtils.createThunderStrike(trident, blockHitResult.getPos());
        }
    }
}
