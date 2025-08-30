package com.example.skam.mixin;

import com.example.skam.effect.ModEffects;
import com.example.skam.item.ModItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof PlayerEntity player) {

           if (source == player.getDamageSources().lightningBolt() && hasFullThunderGodArmor(player)) {

               player.addStatusEffect(new StatusEffectInstance(ModEffects.THUNDER_CHARGE, 600, 0));
            }
        }
    }

    private boolean hasFullThunderGodArmor(PlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack leggings = player.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack boots = player.getEquippedStack(EquipmentSlot.FEET);
        return helmet.getItem() == ModItems.THUNDER_GOD_HELMET &&
                chestplate.getItem() == ModItems.THUNDER_GOD_CHESTPLATE &&
                leggings.getItem() == ModItems.THUNDER_GOD_LEGGINGS &&
                boots.getItem() == ModItems.THUNDER_GOD_BOOTS;
    }
}

