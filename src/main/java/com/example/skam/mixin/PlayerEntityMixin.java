package com.example.skam.mixin;

import com.example.skam.accessor.IAuraAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.property.Properties; // 导入正确的属性类
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements IAuraAccessor {

    @Unique
    private int auraTicks = 0;

    @Unique
    private BlockPos lastLightPos = null;

    @Override
    public void setAuraTicks(int ticks) {
        this.auraTicks = ticks;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        World world = player.getWorld();
        if (!world.isClient) {
           if (this.lastLightPos != null) {
                if (world.getBlockState(this.lastLightPos).isOf(Blocks.LIGHT)) {
                    world.setBlockState(this.lastLightPos, Blocks.AIR.getDefaultState());
                }
                this.lastLightPos = null;
            }
             if (this.auraTicks > 0) {
                BlockPos currentPos = player.getBlockPos().up();
                if (world.getBlockState(currentPos).isAir()) {
                 BlockState lightState = Blocks.LIGHT.getDefaultState().with(Properties.LEVEL_15, 15);
                    world.setBlockState(currentPos, lightState);
                    this.lastLightPos = currentPos;
                }
            }
        }

       if (this.auraTicks > 0) {
            this.auraTicks--;

            if (world.isClient) {
                double radius = 0.7;
                for (int i = 0; i < 4; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double x = player.getX() + radius * Math.cos(angle);
                    double z = player.getZ() + radius * Math.sin(angle);
                    double y = player.getY() + Math.random() * 2.0;

                    world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0, 0.05, 0);
                }
            }
        }
    }
}

