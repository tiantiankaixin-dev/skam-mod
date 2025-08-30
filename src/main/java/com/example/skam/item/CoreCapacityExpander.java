package com.example.skam.item;

import com.example.skam.util.CoreNbtApplicator;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoreCapacityExpander extends Item {

    private static final int CAPACITY_PER_UPGRADE = 5;
    private static final int MAX_UPGRADES = 4;
    private static final String UPGRADE_COUNT_KEY = "capacity_upgrades";

    public CoreCapacityExpander(Settings settings) {
        super(settings);
    }

    /**
     * 这个方法只会在该物品被拿到鼠标上，然后去右击另一个物品时被调用。
     * @param stack      当前在鼠标光标上的物品 (也就是这个扩展器自己)
     * @param otherStack 被右击的、在物品栏格子里的物品 (目标武器)
     * @param slot       被右击的格子
     * @param clickType  点击类型
     * @param player     玩家
     * @param cursorStackReference 鼠标物品的引用
     * @return true 表示我们处理了这次点击，游戏不要再执行默认行为 (如交换物品)
     */
    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        // 1. 验证交互：必须是右键，并且目标格子不能为空，且不能是另一个扩展器
        if (clickType != ClickType.RIGHT || otherStack.isEmpty() || otherStack.isOf(this)) {
            return false; // 条件不满足，交还给游戏处理（通常是交换物品）
        }

        // 2. 获取目标物品(otherStack)的NBT数据，准备升级
        NbtCompound skamNbt = otherStack.getOrCreateSubNbt("skam_mods");
        int currentUpgrades = skamNbt.getInt(UPGRADE_COUNT_KEY);

        // 3. 检查是否已达升级上限
        if (currentUpgrades >= MAX_UPGRADES) {
            player.playSound(SoundEvents.BLOCK_ANVIL_LAND, 0.5F, 1.5F);
            if (player.getWorld().isClient) {
                player.sendMessage(Text.translatable("tooltip.skam.max_upgrades_reached").formatted(Formatting.RED), true);
            }
            return true; // 我们处理了，但升级失败，同样阻止默认行为
        }

        // 4. 执行升级
        int currentCapacity = skamNbt.getInt("max_capacity");
        int newCapacity = currentCapacity + CAPACITY_PER_UPGRADE;

        skamNbt.putInt("max_capacity", newCapacity);
        skamNbt.putInt(UPGRADE_COUNT_KEY, currentUpgrades + 1);

        // 可选：如果你的NBT应用器需要，就调用它
        CoreNbtApplicator.applyCoreModifications(otherStack);

        // 5. 消耗一个在鼠标上的扩展器 (stack)
        stack.decrement(1);

        // 6. 播放音效和发送消息
        player.playSound(SoundEvents.BLOCK_ANVIL_USE, 1.0F, 1.2F);
        if (!player.getWorld().isClient) {
            player.sendMessage(Text.translatable("tooltip.skam.capacity_expanded", otherStack.getName(), newCapacity).formatted(Formatting.GREEN), true);
        }

        // 7. 返回 true，表示升级成功，阻止游戏交换物品
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.core_capacity_expander.description", CAPACITY_PER_UPGRADE).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.core_capacity_expander.limit", MAX_UPGRADES).formatted(Formatting.DARK_GRAY));
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
