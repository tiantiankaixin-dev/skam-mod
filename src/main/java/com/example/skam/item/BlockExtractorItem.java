package com.example.skam.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockExtractorItem extends Item {

    public BlockExtractorItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState blockState = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        if (blockState.getHardness(world, pos) < 0 || blockState.getBlock().asItem() == Items.AIR) {
           return ActionResult.FAIL;
        }
        if (!world.isClient && player != null) {
            ItemStack extractedBlockStack = new ItemStack(blockState.getBlock().asItem());
            if (!player.getInventory().insertStack(extractedBlockStack)) {
                player.dropItem(extractedBlockStack, false);
            }
            world.removeBlock(pos, false);
            world.playSound(null, pos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.BLOCKS, 1.0f, 1.2f);
            if (world instanceof ServerWorld) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.4, 0.4, 0.4, 0.0);
            }
            if (!player.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        return ActionResult.SUCCESS;
    }
}
