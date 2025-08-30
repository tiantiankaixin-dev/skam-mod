package com.example.skam.mixin;

import com.example.skam.SkamMod;
import com.example.skam.item.SwordSheathItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntity.class)
public abstract class TridentEntityChunkLoaderMixin {

    @Unique
    private boolean skam_chunkForced = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickChunkLoad(CallbackInfo ci) {
        TridentEntity trident = (TridentEntity) (Object) this;

        if (trident.getWorld().isClient()) {
            return;
        }

        ItemStack tridentStack = ((TridentEntityAccessor) trident).invokeAsItemStack();
        NbtCompound nbt = tridentStack.getNbt();
        if (nbt == null) {
            return;
        }

        PlayerEntity owner = trident.getOwner() instanceof PlayerEntity ? (PlayerEntity) trident.getOwner() : null;
        if (owner != null) {
            String ownerKey = SwordSheathItem.getOwnerNbtKey(owner);
           if (nbt.contains(ownerKey)) {
                if (((PersistentProjectileEntityAccessor) trident).isInGround() && !skam_chunkForced) {
                    SkamMod.setChunkForced((ServerWorld) trident.getWorld(), trident.getChunkPos(), true);
                    skam_chunkForced = true;
                }
            }
        }
    }

    @Inject(method = "onPlayerCollision", at = @At("HEAD"))
    private void onPlayerCollisionUnloadChunk(PlayerEntity player, CallbackInfo ci) {
        TridentEntity trident = (TridentEntity) (Object) this;
        if (((PersistentProjectileEntityAccessor) trident).isInGround() && skam_chunkForced) {
            SkamMod.setChunkForced((ServerWorld) trident.getWorld(), trident.getChunkPos(), false);
            skam_chunkForced = false;
        }
    }
}
