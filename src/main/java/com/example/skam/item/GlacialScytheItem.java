package com.example.skam.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class GlacialScytheItem extends HoeItem {
    public GlacialScytheItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos centerPos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();

        // 首先尝试执行单格耕作，如果失败则不继续
        ActionResult initialResult = super.useOnBlock(context);
        if (!initialResult.isAccepted() || player == null) {
            return initialResult;
        }

        // 如果成功，则耕作周围区域
        if (!world.isClient) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue; // 跳过中心方块，因为它已经被耕过了

                    BlockPos currentPos = centerPos.add(x, 0, z);
                    BlockState blockState = world.getBlockState(currentPos);

                    // 检查是否可以耕作
                    if (blockState.isOf(Blocks.DIRT) || blockState.isOf(Blocks.GRASS_BLOCK) || blockState.isOf(Blocks.DIRT_PATH)) {
                        world.setBlockState(currentPos, Blocks.FARMLAND.getDefaultState(), 11);
                        world.playSound(null, currentPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
            // 对工具造成额外损耗
            context.getStack().damage(8, player, (p) -> p.sendToolBreakStatus(context.getHand()));
        }

        return ActionResult.SUCCESS;
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.hoe_3x3").formatted(Formatting.GRAY));

    }
}
