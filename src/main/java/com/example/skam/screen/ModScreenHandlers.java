package com.example.skam.screen;

import com.example.skam.SkamMod;
import com.example.skam.screen.HandheldDispenserScreenHandler;
import com.example.skam.item.SwordSheathScreenHandler;
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
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SkamMod.MOD_ID, "forging_table"),
                    new ExtendedScreenHandlerType<>(ForgingTableScreenHandler::new));

    public static final ScreenHandlerType<HandheldDispenserScreenHandler> HANDHELD_DISPENSER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SkamMod.MOD_ID, "handheld_dispenser"),
                    new ExtendedScreenHandlerType<>(HandheldDispenserScreenHandler::new));

    public static final ScreenHandlerType<SwordSheathScreenHandler> SWORD_SHEATH_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(SkamMod.MOD_ID, "sword_sheath"),
                    new ExtendedScreenHandlerType<>(SwordSheathScreenHandler::new));


    public static void registerScreenHandlers() {
        SkamMod.LOGGER.info("Registering Screen Handlers for " + SkamMod.MOD_ID);
    }
}
