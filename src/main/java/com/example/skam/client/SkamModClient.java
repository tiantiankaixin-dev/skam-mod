// File Path: src/main/java/com/example/skam/client/SkamModClient.java
package com.example.skam.client;

import com.example.skam.Skam;
import com.example.skam.screen.ForgingTableScreen;
import com.example.skam.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import com.example.skam.SkamMod;
import com.example.skam.client.model.FireGodArmorModel;
import com.example.skam.client.render.*;
import com.example.skam.client.screen.HandheldDispenserScreen;
import com.example.skam.client.SwordSheathScreen;
import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.entity.ModEntities;
import com.example.skam.entity.client.FloatingShipRenderer;
import com.example.skam.hud.SoulLinkHud;
import com.example.skam.item.ModItems;
import com.example.skam.networking.ModMessages;
import com.example.skam.screen.EnchantmentUpgraderScreen;
import com.example.skam.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;

import static com.example.skam.item.SwordSheathItem.TRIDENT_KEY;

public class SkamModClient implements ClientModInitializer {
    // 用于魔王效果的NBT键，需要和服务器端保持一致
    private static final String ORIGINAL_ENCHANTS_KEY = "skam.original_enchantments";

    public static final EntityModelLayer FIRE_GOD_ARMOR_LAYER = new EntityModelLayer(new Identifier(SkamMod.MOD_ID, "fire_god_armor"), "main");
    public static final EntityModelLayer ICE_GOD_ARMOR_LAYER = new EntityModelLayer(new Identifier(SkamMod.MOD_ID, "ice_god_armor"), "main");
    public static final EntityModelLayer THUNDER_GOD_ARMOR_LAYER = new EntityModelLayer(new Identifier(SkamMod.MOD_ID, "thunder_god_armor"), "main");
    private boolean attackKeyWasDown = false;

    @Override
    public void onInitializeClient() {
        // 注册客户端事件
        registerClientTickEvents();
        registerTooltipCallback(); // <- 新增：注册Tooltip回调

        // 实体渲染器
        EntityRendererRegistry.register(ModEntities.FLAME_EMPEROR, FlameEmperorRenderer::new);
        EntityRendererRegistry.register(ModEntities.ICE_EMPEROR, IceEmperorRenderer::new);
        EntityRendererRegistry.register(ModEntities.LIGHTNING_EMPEROR, LightningEmperorRenderer::new);
        EntityRendererRegistry.register(ModEntities.LEGEND_ARROW, LegendArrowEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.BLACK_HOLE, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.FALLING_SWORD, FallingSwordEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.FLOATING_SHIP, FloatingShipRenderer::new);
        try {EntityRendererRegistry.register(EntityType.TRIDENT, CursedTridentBlockEntityRenderer::new);} catch (Exception e) {e.printStackTrace();}
   // 模型断言
        ModelPredicateProviderRegistry.register(SkamMod.SWORD_SHEATH, new Identifier("skam", "filled"), (itemStack, clientWorld, livingEntity, seed) -> {return itemStack.getSubNbt(TRIDENT_KEY) != null ? 1.0f : 0.0f;});
        registerLegendBowPredicates();
        try {
            ModelPredicateProviderRegistry.register(
                    Items.TRIDENT,
                    new Identifier("skam", "cursed"),
                    (stack, world, entity, seed) -> {
                        if (stack == null || stack.isEmpty()) {
                            return 0.0F;
                        }
                        int enchantLevel = EnchantmentHelper.getLevel(ModEnchantments.CURSED_TRIDENT, stack);
                        return enchantLevel > 0 ? 1.0F : 0.0F;});
        }
        catch (Exception e) {e.printStackTrace();}

        // 客户端处理器和屏幕
        FireGodArmorClientHandler.registerClientEvents();
        HandledScreens.register(SkamMod.HANDHELD_DISPENSER_SCREEN_HANDLER, HandheldDispenserScreen::new); ScreenRegistry.register(SkamMod.SWORD_SHEATH_SCREEN_HANDLER, SwordSheathScreen::new);
        HandledScreens.register(ModScreenHandlers.ENCHANTMENT_UPGRADER_SCREEN_HANDLER, EnchantmentUpgraderScreen::new);
        HandledScreens.register(ModScreenHandlers.FORGING_TABLE_SCREEN_HANDLER, ForgingTableScreen::new);


        // HUD
        HudRenderCallback.EVENT.register(new SoulLinkHud());

        // 模型层
        EntityModelLayerRegistry.registerModelLayer(FIRE_GOD_ARMOR_LAYER, FireGodArmorModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ICE_GOD_ARMOR_LAYER, FireGodArmorModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(THUNDER_GOD_ARMOR_LAYER, FireGodArmorModel::getTexturedModelData);

        // 盔甲渲染器
        ArmorRenderer.register(new FireGodArmorRenderer(), ModItems.FIRE_GOD_HELMET, ModItems.FIRE_GOD_CHESTPLATE, ModItems.FIRE_GOD_LEGGINGS, ModItems.FIRE_GOD_BOOTS);
        ArmorRenderer.register(new IceGodArmorRenderer(), ModItems.ICE_GOD_HELMET, ModItems.ICE_GOD_CHESTPLATE, ModItems.ICE_GOD_LEGGINGS, ModItems.ICE_GOD_BOOTS);
        ArmorRenderer.register(new ThunderGodArmorRenderer(), ModItems.THUNDER_GOD_HELMET, ModItems.THUNDER_GOD_CHESTPLATE, ModItems.THUNDER_GOD_LEGGINGS, ModItems.THUNDER_GOD_BOOTS);
    }

    /**
     * 新增方法：注册物品Tooltip渲染回调事件
     */
    private void registerTooltipCallback() {
        ItemTooltipCallback.EVENT.register(this::onRenderDemonKingTooltip);
    }

    /**
     * 新增方法：当渲染物品Tooltip时调用此方法，用于将魔王效果增强的附魔变为金色
     */
    private void onRenderDemonKingTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        NbtCompound nbt = stack.getNbt();
        // 1. 检查物品是否被我们的效果修改过
        if (nbt == null || !nbt.contains(ORIGINAL_ENCHANTS_KEY)) {
            return;
        }

        // 2. 获取物品上当前的所有（增强后的）附魔
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);
        if (enchantments.isEmpty()) {
            return;
        }

        // 3. 遍历Tooltip的每一行文本
        for (int i = 0; i < lines.size(); i++) {
            Text line = lines.get(i);
            // 确保我们只修改可变文本，避免崩溃
            if (!(line instanceof MutableText mutableLine)) {
                continue;
            }

            // 4. 检查这一行是否是附魔描述
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Text enchantmentText = entry.getKey().getName(entry.getValue());
                // 通过比较字符串内容来判断是否为对应的附魔行
                if (line.getString().equals(enchantmentText.getString())) {
                    // 5. 如果是，就地修改这一行，将其颜色变为金色
                    lines.set(i, mutableLine.formatted(Formatting.GOLD));
                    // 找到后就跳出内层循环，继续检查下一行Tooltip文本
                    break;
                }
            }
        }
    }

    private void registerClientTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }
            boolean isAttackKeyDown = client.options.attackKey.isPressed();

            if (isAttackKeyDown && !this.attackKeyWasDown) {
                HitResult hitResult = client.player.raycast(50.0D, 0.0F, false);
                Vec3d targetPos;
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    targetPos = hitResult.getPos();}
                else {
                    targetPos = client.player.getEyePos().add(client.player.getRotationVector().multiply(50.0D));}

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeDouble(targetPos.x);
                buf.writeDouble(targetPos.y);
                buf.writeDouble(targetPos.z);
                ClientPlayNetworking.send(ModMessages.RETARGET_TRIDENT_ID, buf);
            }
            this.attackKeyWasDown = isAttackKeyDown;
        });
    }

    private void registerLegendBowPredicates() {
        ModelPredicateProviderRegistry.register(ModItems.LEGEND_BOW, new Identifier("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0f;
            }
            if (entity.getActiveItem() != stack) {
                return 0.0f;
            }
            return (float)(stack.getMaxUseTime() - entity.getItemUseTimeLeft()) / 20.0f;
        });

        ModelPredicateProviderRegistry.register(ModItems.LEGEND_BOW, new Identifier("pulling"), (stack, world, entity, seed) ->
                entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0f : 0.0f
        );
    }

    // 删除了旧的、未使用的 modifyDemonKingTooltip 方法
}

