package com.example.skam.entity;

import com.example.skam.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class FloatingShipEntity extends Entity {


    private boolean playerControlled = false;

    public FloatingShipEntity(EntityType<?> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public FloatingShipEntity(World world, double x, double y, double z) {
        this(ModEntities.FLOATING_SHIP, world);
        this.setPosition(x, y, z);
    }


    public void setPlayerControlled(boolean controlled) {
        this.playerControlled = controlled;
        if (controlled) {
            this.setNoGravity(true);
        }
    }

    @Override
    public void tick() {
        if (this.playerControlled) {
             super.baseTick();
            return;
        }

        super.tick();
    }

    @Override
    protected void initDataTracker() {

    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {

        if (this.playerControlled) {
            return false;
        }

        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.isRemoved() && !this.getWorld().isClient) {
            this.kill();
            this.dropStack(new ItemStack(ModItems.FLOATING_SHIP_ITEM));
        }
        return true;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {

        if (this.playerControlled) {
            return ActionResult.PASS;
        }
        if (!this.hasPassengers()) {
            if (!this.getWorld().isClient) {
                player.startRiding(this);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }
}
