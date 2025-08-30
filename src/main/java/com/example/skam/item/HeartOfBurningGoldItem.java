package com.example.skam.item; // 请确保包名正确

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

public class HeartOfBurningGoldItem extends Item {

    // 熔炼配方表：定义了什么物品可以被熔炼，以及它的产物
    private static final Map<Item, Item> SMELTING_PAIRS = new ImmutableMap.Builder<Item, Item>()
            .put(Items.RAW_IRON, Items.IRON_INGOT)
            .put(Items.RAW_GOLD, Items.GOLD_INGOT)
            .put(Items.RAW_COPPER, Items.COPPER_INGOT)
            .put(Items.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP) // 甚至可以熔炼远古残骸！
            .put(Items.SAND, Items.GLASS)
            .put(Items.COBBLESTONE, Items.STONE)
            .put(Items.CLAY_BALL, Items.BRICK)
            .put(Items.NETHERRACK, Items.NETHER_BRICK)
            .build();

    public HeartOfBurningGoldItem(Settings settings) {
        super(settings.maxDamage(150)); // 增加了耐久度，因为它现在更有用
    }

    /**
     * 主要的使用方法，现在包含了两种功能
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack heldStack = user.getStackInHand(hand); // 手中持有的“炼金术士之魂”

        // 逻辑只在服务器端运行
        if (!world.isClient) {
            // --- 功能一：应急灭火 ---
            if (user.isOnFire()) {
                user.extinguish();
                user.heal(4.0f);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1.0f, 1.0f);
                user.getItemCooldownManager().set(this, 100); // 5秒冷却
                heldStack.damage(5, user, p -> p.sendToolBreakStatus(hand)); // 灭火消耗更多耐久
                return TypedActionResult.success(heldStack);
            }

            // --- 功能二：背包内熔炼 ---
            // 遍历玩家的主物品栏 (不包括盔甲和副手)
            for (int i = 0; i < user.getInventory().main.size(); i++) {
                ItemStack inventoryStack = user.getInventory().getStack(i);

                // 检查这个物品是否在我们的熔炼配方里
                if (SMELTING_PAIRS.containsKey(inventoryStack.getItem())) {
                    // 找到了！执行熔炼
                    Item sourceItem = inventoryStack.getItem();
                    Item resultItem = SMELTING_PAIRS.get(sourceItem);

                    // 1. 消耗一个原材料
                    inventoryStack.decrement(1);

                    // 2. 创建3个成品
                    ItemStack resultStack = new ItemStack(resultItem, 3);

                    // 3. 将成品给予玩家 (如果背包满了会掉在地上)
                    user.getInventory().offerOrDrop(resultStack);

                    // 4. 播放音效和粒子效果
                    world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.PLAYERS, 1.0f, 1.0f);
                    if (world instanceof ServerWorld) {
                        ((ServerWorld) world).spawnParticles(ParticleTypes.FLAME, user.getX(), user.getY() + 1, user.getZ(), 15, 0.3, 0.5, 0.3, 0.05);
                    }

                    // 5. 消耗“炼金术士之魂”的耐久并设置冷却
                    user.getItemCooldownManager().set(this, 20); // 1秒冷却
                    heldStack.damage(1, user, p -> p.sendToolBreakStatus(hand));

                    // 6. 操作成功，立即返回，防止一次右键熔炼多个物品
                    return TypedActionResult.success(heldStack);
                }
            }
        }

        // 如果既没有着火，背包里也没有可熔炼的东西，则返回失败 (右键无反应)
        return TypedActionResult.fail(heldStack);
    }


    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    // 更新物品提示，以反映新的功能
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        // 灭火功能的提示
        tooltip.add(Text.translatable("item.skam.heart_of_burning_gold.tooltip_fire").formatted(Formatting.GRAY));
        // 新的熔炼功能的提示
        tooltip.add(Text.translatable("item.skam.heart_of_burning_gold.tooltip_smelt").formatted(Formatting.GOLD));
        super.appendTooltip(stack, world, tooltip, context);
    }
}

