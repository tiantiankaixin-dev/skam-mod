package com.example.skam.item;

import com.example.skam.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class OreDetectorItem extends Item {
    private static final int DETECTION_RADIUS = 50;

    public OreDetectorItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            BlockPos playerPos = user.getBlockPos();
            Map<String, Integer> oreCount = new HashMap<>();

            for (int x = -DETECTION_RADIUS; x <= DETECTION_RADIUS; x++) {
                for (int y = -DETECTION_RADIUS; y <= DETECTION_RADIUS; y++) {
                    for (int z = -DETECTION_RADIUS; z <= DETECTION_RADIUS; z++) {
                        BlockPos scanPos = playerPos.add(x, y, z);
                        BlockState state = world.getBlockState(scanPos);
                        Block block = state.getBlock();

                        // 检测原版矿石
                        if (state.isIn(BlockTags.COAL_ORES)) {
                            oreCount.merge("煤矿石", 1, Integer::sum);
                        } else if (state.isIn(BlockTags.IRON_ORES)) {
                            oreCount.merge("铁矿石", 1, Integer::sum);
                        } else if (state.isIn(BlockTags.GOLD_ORES)) {
                            oreCount.merge("金矿石", 1, Integer::sum);
                        } else if (state.isIn(BlockTags.DIAMOND_ORES)) {
                            oreCount.merge("钻石矿石", 1, Integer::sum);
                        } else if (state.isIn(BlockTags.EMERALD_ORES)) {
                            oreCount.merge("绿宝石矿石", 1, Integer::sum);
                        } else if (state.isIn(BlockTags.LAPIS_ORES)) {
                            oreCount.merge("青金石矿石", 1, Integer::sum);
                        } else if (state.isIn(BlockTags.REDSTONE_ORES)) {
                            oreCount.merge("红石矿石", 1, Integer::sum);
                        } else if (state.isIn(BlockTags.COPPER_ORES)) {
                            oreCount.merge("铜矿石", 1, Integer::sum);
                        }
                       else if (block == ModBlocks.BURNING_GOLD_ORE) {
                            oreCount.merge("燃烧之金矿石", 1, Integer::sum);
                        } else if (block == ModBlocks.FROST_IRON_ORE) {
                            oreCount.merge("寒霜之铁矿石", 1, Integer::sum);
                        } else if (block == ModBlocks.THUNDER_ALLOY_ORE) {
                            oreCount.merge("雷霆合金矿石", 1, Integer::sum);
                        }
                    }
                }
            }

            if (oreCount.isEmpty()) {
                user.sendMessage(Text.translatable("message.skam.no_ores_found").formatted(Formatting.GRAY), false);
            } else {
                user.sendMessage(Text.translatable("message.skam.ore_scan_results").formatted(Formatting.GOLD), false);    for (Map.Entry<String, Integer> entry : oreCount.entrySet()) {
                    user.sendMessage(Text.literal("- " + entry.getKey() + ": " + entry.getValue() + " ")
                            .formatted(Formatting.GREEN), false);
                }
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}