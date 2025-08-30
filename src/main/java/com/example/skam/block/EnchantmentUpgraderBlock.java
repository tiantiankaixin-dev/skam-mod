package com.example.skam.block;

import com.example.skam.block.entity.EnchantmentUpgraderBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.screen.Screen; // 导入 Screen 类
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting; // 导入 Formatting 类
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView; // 注意：对于 appendTooltip，使用 BlockView 更通用
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List; // 导入 List 类

public class EnchantmentUpgraderBlock extends BlockWithEntity implements BlockEntityProvider {

    public EnchantmentUpgraderBlock(Settings settings) {
        super(settings);
    }


    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext context) {
        // 检查玩家当前是否按下了 SHIFT 键
        if (Screen.hasShiftDown()) {
            // 如果按下了 SHIFT，显示详细说明
            tooltip.add(Text.literal("将附魔工具和魔能神石放入其中").formatted(Formatting.AQUA));
            tooltip.add(Text.literal("(左侧附魔书，中间附魔装备，右侧魔能神石，按下按钮突破魔咒上限）").formatted(Formatting.AQUA));
        } else {
            // 如果没有按下 SHIFT，显示提示信息
            tooltip.add(Text.literal("按下 [SHIFT] 查看更多信息").formatted(Formatting.YELLOW));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }
    // --- 新增代码结束 ---


    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EnchantmentUpgraderBlockEntity(pos, state);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof EnchantmentUpgraderBlockEntity) {
                ItemScatterer.spawn(world, pos, (EnchantmentUpgraderBlockEntity)blockEntity);
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = ((EnchantmentUpgraderBlockEntity) world.getBlockEntity(pos));
            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }

        return ActionResult.SUCCESS;
    }
}
