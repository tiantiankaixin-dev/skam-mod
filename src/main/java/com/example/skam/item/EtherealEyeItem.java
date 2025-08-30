package com.example.skam.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EtherealEyeItem extends Item {

    private static final Map<UUID, PlayerState> SPECTATING_PLAYERS = new ConcurrentHashMap<>();
    private static final int DURATION_TICKS = 200;
    private static final int COOLDOWN_TICKS = 300;

    public EtherealEyeItem(Settings settings) {
        super(settings.maxCount(1));
    }

    private static class PlayerState {
        private final GameMode originalGameMode;
        private int ticksRemaining;

        PlayerState(GameMode originalGameMode, int ticks) {
            this.originalGameMode = originalGameMode;
            this.ticksRemaining = ticks;
        }
    }
    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) user;

            if (SPECTATING_PLAYERS.containsKey(serverPlayer.getUuid())) {
                return TypedActionResult.fail(itemStack);
            }

            GameMode originalMode = serverPlayer.interactionManager.getGameMode();
            SPECTATING_PLAYERS.put(serverPlayer.getUuid(), new PlayerState(originalMode, DURATION_TICKS));
            serverPlayer.changeGameMode(GameMode.SPECTATOR);
            user.getItemCooldownManager().set(this, COOLDOWN_TICKS);

            world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.5f);

            return TypedActionResult.success(itemStack, true);
        }
        return TypedActionResult.pass(itemStack);
    }

    public static void tick(MinecraftServer server) {
        SPECTATING_PLAYERS.entrySet().removeIf(entry -> {
            UUID playerUuid = entry.getKey();
            PlayerState state = entry.getValue();
            state.ticksRemaining--;

            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerUuid);

            if (state.ticksRemaining <= 0) {
                if (player != null) {
                    player.changeGameMode(state.originalGameMode);
                    player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.0f, 1.5f);
                    player.sendMessage(Text.translatable("item.skam.ethereal_eye.end"), true);
                }
                return true;
            } else {
                if (player != null) {
                    if (state.ticksRemaining % 20 == 0 || state.ticksRemaining <= 60) {
                        int secondsLeft = (state.ticksRemaining + 19) / 20;
                        Text timerText = Text.translatable("item.skam.ethereal_eye.timer", secondsLeft);
                        player.sendMessage(timerText, true);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.skam.ethereal_eye.tooltip").formatted(Formatting.GRAY));
        super.appendTooltip(stack, world, tooltip, context);
    }
}


