package com.example.skam.item;

import com.example.skam.entity.FloatingShipEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class FloatingShipItem extends Item {
    public FloatingShipItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient) {
            Vec3d pos = context.getHitPos();
                  FloatingShipEntity shipEntity = new FloatingShipEntity(world, pos.getX(), pos.getY(), pos.getZ());
                  shipEntity.setYaw(context.getPlayer().getYaw());
                  world.spawnEntity(shipEntity);
                  world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, pos);
                  if (!context.getPlayer().getAbilities().creativeMode) {
                context.getStack().decrement(1);
            }

            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }
}
