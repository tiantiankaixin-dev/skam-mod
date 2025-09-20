package com.example.skam.mixin;

import com.example.skam.util.TridentRiderData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    /**
     * 注入到 Entity.isSneaking() 方法，这是所有实体潜行逻辑的根源。
     * 我们在这里检查该实体是否为玩家，并且是否处于我们的豁免期内。
     */
    @Inject(method = "isSneaking()Z", at = @At("HEAD"), cancellable = true)
    private void preventDismountWhileSneaking(CallbackInfoReturnable<Boolean> cir) {
        // 首先，将 "this" (它是一个 Entity) 转换一下，方便使用
        Entity entity = (Entity) (Object) this;

        // 检查这个实体是不是一个玩家
        if (entity instanceof PlayerEntity player) {
            // 如果是玩家，获取我们附加的数据
            TridentRiderData data = (TridentRiderData) player;

            // 如果豁免期 > 0
            if (data.skam_getMountGracePeriod() > 0) {
                // 强制方法返回 false (即“没有潜行”)，并取消后续的原始方法调用
                cir.setReturnValue(false);
            }
        }
    }
}
