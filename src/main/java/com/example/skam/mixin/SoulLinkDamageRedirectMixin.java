package com.example.skam.mixin;

import com.example.skam.util.ISoulLinkDataAccessor;
import com.example.skam.util.ModDamageSources;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class SoulLinkDamageRedirectMixin {

   @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float redirectSoulLinkDamage(float amount, DamageSource source) {
        PlayerEntity player = (PlayerEntity) (Object) this;

       if (source.isOf(ModDamageSources.SOUL_LINK_DAMAGE_TYPE)) {
            return amount;
        }

       if (player.getWorld().isClient()) {
            return amount;
        }

        ServerWorld world = (ServerWorld) player.getWorld();
        UUID playerUuid = player.getUuid();
        float totalRedirectedDamage = 0f;

       for (LivingEntity mob : world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(128), e -> e.isAlive())) {
           NbtCompound nbt = ((ISoulLinkDataAccessor) mob).getPersistentData();

            if (nbt.contains("soul_linker_uuid") && nbt.contains("soul_link_expiry")) {
               if (nbt.getUuid("soul_linker_uuid").equals(playerUuid) && world.getTime() < nbt.getLong("soul_link_expiry")) {
                    float damageToRedirect = amount * 0.05f;

                   mob.damage(ModDamageSources.of(world), damageToRedirect);
                    totalRedirectedDamage += damageToRedirect;
                }
            }
        }

       float maxReduction = amount * 0.5f;
       float actualReduction = Math.min(totalRedirectedDamage, maxReduction);

        return amount - actualReduction;
    }
}
