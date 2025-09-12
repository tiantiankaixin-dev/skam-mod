package com.example.skam.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeartfireFurnacePickItem extends PickaxeItem {
    // 建立一个矿石到产物的映射表
    private static final Map<Block, Item> SMELT_MAP = new HashMap<>();
    static {
        SMELT_MAP.put(Blocks.IRON_ORE, Items.IRON_INGOT);
        SMELT_MAP.put(Blocks.DEEPSLATE_IRON_ORE, Items.IRON_INGOT);
        SMELT_MAP.put(Blocks.GOLD_ORE, Items.GOLD_INGOT);
        SMELT_MAP.put(Blocks.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT);
        SMELT_MAP.put(Blocks.COPPER_ORE, Items.COPPER_INGOT);
        SMELT_MAP.put(Blocks.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT);
        SMELT_MAP.put(Blocks.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP);
        SMELT_MAP.put(Blocks.SAND, Items.GLASS);
        SMELT_MAP.put(Blocks.COBBLESTONE, Items.STONE);
    }

    public HeartfireFurnacePickItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient) {
            Block block = state.getBlock();
            // 检查被破坏的方块是否在我们的映射表中
            if (SMELT_MAP.containsKey(block)) {
                // 50% 概率触发自动熔炼
                if (world.random.nextFloat() < 0.50f) {
                    Item smeltedItem = SMELT_MAP.get(block);
                    // 在方块位置生成烧炼好的物品
                    world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(smeltedItem)));

                    // 关键：阻止原版掉落物生成。需要更高级的Mixin注入，这里我们用一个简单方法：
                    // 在服务器端破坏方块并替换掉落物。但 postMine 在掉落后才调用。
                    // 因此，一个更简单的（虽然不完美）实现是让它额外掉落。
                    // 完美的实现需要Mixin，对于初学者来说这个版本更容易理解。
                }
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.auto_smelt").formatted(Formatting.GRAY));

    }
}
