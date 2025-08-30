package com.example.skam.mixin;

import com.example.skam.util.IAirJumperState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements IAirJumperState {

    @Unique
    private int skam_airJumpsUsed = 0;

    @Unique
    private int skam_ticksInAir = 0;

    @Override
    public int getAirJumpsUsed() {
        return this.skam_airJumpsUsed;
    }

    @Override
    public void setAirJumpsUsed(int jumps) {
        this.skam_airJumpsUsed = jumps;
    }

    @Override
    public void setTicksInAir(int ticks) {
        this.skam_ticksInAir = ticks;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void skam_handleCounters(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        if (player.isOnGround()) {
            this.skam_airJumpsUsed = 0;
            this.skam_ticksInAir = 0;
        } else {
            this.skam_ticksInAir++;
        }
    }
}
