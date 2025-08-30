package com.example.skam.mixin;

import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.networking.ModMessages;
import com.example.skam.util.ControlledTridentManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow public ClientPlayerEntity player;
    @Shadow public HitResult crosshairTarget;

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void onLeftClick(CallbackInfo ci) {
        if (this.player == null) {
            return;
        }

        TridentEntity controlledTrident = ControlledTridentManager.getControlledTrident(this.player);

        if (controlledTrident != null && !controlledTrident.isRemoved()) {
            Vec3d targetPos;

            if (this.crosshairTarget != null && this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                targetPos = this.crosshairTarget.getPos();
            } else {
               targetPos = this.player.getEyePos().add(this.player.getRotationVec(1.0F).multiply(50.0D));
            }

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeDouble(targetPos.x);
            buf.writeDouble(targetPos.y);
            buf.writeDouble(targetPos.z);

            ClientPlayNetworking.send(ModMessages.RETARGET_TRIDENT_ID, buf);

            this.player.swingHand(this.player.getActiveHand());

            ci.cancel();
        }
    }
    @Inject(method = "tick", at = @At("HEAD"))
    private void skam_onClientTickForAirJump(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        ClientPlayerEntity player = client.player;
         if (player != null && client.options.jumpKey.wasPressed()) {
            if (!player.isOnGround() && EnchantmentHelper.getLevel(ModEnchantments.AIR_JUMPER, player.getEquippedStack(EquipmentSlot.FEET)) > 0) {
 ClientPlayNetworking.send(ModMessages.AIR_JUMP_ID, PacketByteBufs.create());
            }
        }
    }

}
