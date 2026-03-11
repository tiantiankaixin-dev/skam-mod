package com.example.skam.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

/**
 * 作弊切换器 - 允许玩家在生存和创造模式之间切换
 */
public class CheatTogglerItem extends Item {

    public CheatTogglerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity serverPlayer) {
            // 获取当前游戏模式
            GameMode currentMode = serverPlayer.interactionManager.getGameMode();
            
            // 切换游戏模式
            GameMode newMode;
            String modeName;
            Formatting color;
            
            if (currentMode == GameMode.CREATIVE) {
                newMode = GameMode.SURVIVAL;
                modeName = "生存模式";
                color = Formatting.GREEN;
            } else {
                newMode = GameMode.CREATIVE;
                modeName = "创造模式";
                color = Formatting.GOLD;
            }
            
            // 设置新的游戏模式
            serverPlayer.changeGameMode(newMode);
            
            // 发送消息给玩家
            serverPlayer.sendMessage(
                Text.literal("已切换到 ").formatted(Formatting.GRAY)
                    .append(Text.literal(modeName).formatted(color))
                    .append(Text.literal("！").formatted(Formatting.GRAY)),
                false
            );
            
            // 播放音效
            world.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                SoundEvents.ENTITY_PLAYER_LEVELUP,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
            );
            
            // 如果不是创造模式，消耗物品耐久
            if (!user.getAbilities().creativeMode) {
                stack.damage(1, user, (p) -> p.sendToolBreakStatus(hand));
            }
            
            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        // 让物品有附魔光效
        return true;
    }
}

