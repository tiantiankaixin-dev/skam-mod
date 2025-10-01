package com.example.skam.registries;

import com.example.skam.ModItemGroups;
import com.example.skam.TimedTreasureMobRule;
import com.example.skam.block.ModBlocks;
import com.example.skam.block.entity.ModBlockEntities;
import com.example.skam.effect.ModEffects;
import com.example.skam.effect.ModStatusEffects;
import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.entity.ModEntities;
import com.example.skam.item.ModItems;
import com.example.skam.loot.ModLootFunctionTypes;
import com.example.skam.networking.ModMessages;
import com.example.skam.potion.ModPotions;
import com.example.skam.screen.ModScreenHandlers;
import com.example.skam.tooltip.CoreTooltipHandler;
import com.example.skam.util.SkamDamageTypes;

public class ModRegistries {

    public static void initialize() {
        // Configuration should be loaded first
        ConfigRegistry.registerConfigs();

        // Register game content
        ModStatusEffects.registerEffects();
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModItemGroups.registerItemGroups();
        ModEntities.registerEntities();
        ModPotions.registerPotions();
        ModPotions.registerPotionRecipes();
        ModEnchantments.registerEnchantments();
        ModEntities.registerModEntities();
        ModEntities.registerEntityAttributes();
        ModBlockEntities.registerBlockEntities();
        ModScreenHandlers.registerScreenHandlers();
        ModEffects.registerEffects();
        ModLootFunctionTypes.register();

        // Register networking
        ModMessages.registerC2SPackets();

        // Register events, commands, and other handlers
        EventRegistry.registerEvents();
        CommandRegistry.registerCommands();
        CoreTooltipHandler.register();
        TimedTreasureMobRule.register();
    }
}
