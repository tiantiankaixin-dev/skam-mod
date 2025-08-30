package com.example.skam.hud;

import com.example.skam.util.ISoulLinkDataAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.UUID;

public class SoulLinkHud implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.world != null) {
            UUID playerUuid = client.player.getUuid();
            long longestExpiry = 0;
            for (LivingEntity entity : client.world.getEntitiesByClass(LivingEntity.class, client.player.getBoundingBox().expand(128), e -> true)) {
                NbtCompound nbt = ((ISoulLinkDataAccessor) entity).getPersistentData();
                if (nbt.contains("soul_linker_uuid") && nbt.getUuid("soul_linker_uuid").equals(playerUuid)) {
                    long expiry = nbt.getLong("soul_link_expiry");
                    if (expiry > client.world.getTime() && expiry > longestExpiry) {
                        longestExpiry = expiry;
                    }
                }
            }
                if (longestExpiry > 0) {
                long remainingTicks = longestExpiry - client.world.getTime();
                float remainingSeconds = remainingTicks / 20.0f;
                String formattedTime = String.format("%.1fs", remainingSeconds);
                Text text = Text.translatable("hud.skam.soul_link_timer", formattedTime).formatted(Formatting.GREEN);
                TextRenderer textRenderer = client.textRenderer;
                int screenWidth = drawContext.getScaledWindowWidth();
                int screenHeight = drawContext.getScaledWindowHeight();
                int x = screenWidth / 2 - textRenderer.getWidth(text) / 2;
                int y = screenHeight - 40;
                drawContext.drawTextWithShadow(textRenderer, text, x, y, 0xFFFFFF);
            }
        }
    }
}
