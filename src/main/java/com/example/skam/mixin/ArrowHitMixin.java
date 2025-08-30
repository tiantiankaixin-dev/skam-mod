// src/main/java/com/example/skam/mixin/ArrowHitMixin.java
package com.example.skam.mixin;

import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.entity.FallingSwordEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public class ArrowHitMixin {

    @Inject(method = "onBlockHit", at = @At("HEAD"))
    private void onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci) {
        handleSkyStrike(blockHitResult);
    }

    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        handleSkyStrike(entityHitResult);
    }

    private void handleSkyStrike(HitResult hitResult) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity)(Object)this;


        if (arrow.getWorld().isClient) {
            return;
        }

        if (!(arrow.getOwner() instanceof LivingEntity living)) {
            return;
        }


        ItemStack weapon = living.getMainHandStack();

        if (!(weapon.getItem() instanceof BowItem)) {
           return;
        }

        int enchantmentLevel = EnchantmentHelper.getLevel(ModEnchantments.SKY_STRIKE, weapon);

        if (enchantmentLevel > 0) {
           double x = hitResult.getPos().x;
            double y = hitResult.getPos().y + 30;
            double z = hitResult.getPos().z;


            FallingSwordEntity sword = new FallingSwordEntity(
                    arrow.getWorld(), x, y, z, enchantmentLevel, living
            );

            boolean spawned = arrow.getWorld().spawnEntity(sword);
       }
    }
}