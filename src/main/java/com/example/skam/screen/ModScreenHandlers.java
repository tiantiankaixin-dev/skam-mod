package com.example.skam.screen;


import com.example.skam.Skam;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import com.example.skam.Skam;
import com.example.skam.SkamMod;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    public static final ScreenHandlerType<EnchantmentUpgraderScreenHandler> ENCHANTMENT_UPGRADER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SkamMod.MOD_ID, "enchantment_upgrader"),
                    new ScreenHandlerType<>(EnchantmentUpgraderScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static final ScreenHandlerType<ForgingTableScreenHandler> FORGING_TABLE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(Skam.MOD_ID, "forging_table"),
                    //表达式解决构造器歧义 <--- 修复：使用 Lambda
                    new ExtendedScreenHandlerType<>((syncId, inv, buf) -> new ForgingTableScreenHandler(syncId, inv)));

    public static void registerScreenHandlers() {
        SkamMod.LOGGER.info("Registering Screen Handlers for " + SkamMod.MOD_ID);
    }
}
