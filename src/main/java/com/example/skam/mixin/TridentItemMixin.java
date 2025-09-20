package com.example.skam.mixin;

import com.example.skam.Skam;
import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.util.TridentRiderData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TridentItem.class)
public class TridentItemMixin {

    @ModifyVariable(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(value = "STORE"),
            ordinal = 0
    )
    private TridentEntity rideTridentOnSneakThrow(TridentEntity tridentEntity, ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient && user instanceof PlayerEntity player && player.isSneaking() && EnchantmentHelper.getLevel(ModEnchantments.TRIDENT_RIDER, stack) > 0) {
            player.startRiding(tridentEntity, true);

            TridentRiderData data = (TridentRiderData) player;
            // 设置10个游戏刻（0.5秒）的豁免期，这足够让客户端和服务器同步
            data.skam_setMountGracePeriod(20);

            // 【调试代码】
            Skam.LOGGER.info("[SKAM DEBUG] Player mounting. Starting sneak immunity grace period for 10 ticks.");
        }
        return tridentEntity;
    }
}
