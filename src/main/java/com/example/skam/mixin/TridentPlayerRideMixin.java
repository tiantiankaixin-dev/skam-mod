package com.example.skam.mixin;

import com.example.skam.Skam;
import com.example.skam.util.TridentRiderData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class TridentPlayerRideMixin implements TridentRiderData {

    @Unique
    private int mountGracePeriod = 0;

    // 实现接口方法
    @Override
    public int skam_getMountGracePeriod() {
        return this.mountGracePeriod;
    }

    @Override
    public void skam_setMountGracePeriod(int ticks) {
        this.mountGracePeriod = ticks;
    }

    /**
     * 这个方法只负责倒计时。
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void tickGracePeriod(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        // 只在服务器端倒计时
        if (!player.getWorld().isClient && this.mountGracePeriod > 0) {
            this.mountGracePeriod--;
            if (this.mountGracePeriod == 0) {
                Skam.LOGGER.info("[SKAM DEBUG] Mount grace period ended. Sneaking status is now normal.");
            }
        }
    }
}


