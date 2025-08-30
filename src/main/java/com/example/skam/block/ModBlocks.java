// src/main/java/com/example/skam/block/ModBlocks.java
package com.example.skam.block;

import com.example.skam.SkamMod;
import com.example.skam.block.custom.ForgingTableBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class ModBlocks {


    public static Block ENCHANTMENT_UPGRADER_BLOCK;
    public static Block BURNING_GOLD_ORE;
    public static Block FROST_IRON_ORE;
    public static Block THUNDER_ALLOY_ORE;

    public static final Block BURNING_GOLD_BLOCK = registerBlock("burning_gold_block",
            new Block(FabricBlockSettings.create().strength(5.0f, 6.0f).requiresTool()));
    public static final Block FROST_IRON_BLOCK = registerBlock("frost_iron_block",
            new Block(FabricBlockSettings.create().strength(5.0f, 6.0f).requiresTool()));
    public static final Block THUNDER_BLOCK = registerBlock("thunder_block",
            new Block(FabricBlockSettings.create().strength(5.0f, 6.0f).requiresTool()));


    public static final Block FORGING_TABLE = registerBlock("forging_table",
            new ForgingTableBlock(FabricBlockSettings.copyOf(Blocks.SMITHING_TABLE)));


    public static void registerModBlocks() {
        SkamMod.LOGGER.info("Registering Mod Blocks for " + SkamMod.MOD_ID);

        ENCHANTMENT_UPGRADER_BLOCK = registerBlock("enchantment_upgrader_block",
                new EnchantmentUpgraderBlock(FabricBlockSettings.copyOf(Blocks.ANVIL)));

        BURNING_GOLD_ORE = registerBlock("burning_gold_ore",
                new ExperienceDroppingBlock(
                        FabricBlockSettings.create()
                                .mapColor(MapColor.GOLD)
                                .strength(3.0f, 3.0f)
                                .requiresTool()
                                .sounds(BlockSoundGroup.STONE)
                                .luminance(7),
                        UniformIntProvider.create(2, 5)
                ));

        FROST_IRON_ORE = registerBlock("frost_iron_ore",
                new ExperienceDroppingBlock(
                        FabricBlockSettings.create()
                                .mapColor(MapColor.LIGHT_BLUE)
                                .strength(3.0f, 3.0f)
                                .requiresTool()
                                .sounds(BlockSoundGroup.STONE),
                        UniformIntProvider.create(1, 4)
                ));

        THUNDER_ALLOY_ORE = registerBlock("thunder_alloy_ore",
                new ExperienceDroppingBlock(
                        FabricBlockSettings.create()
                                .mapColor(MapColor.PURPLE)
                                .strength(4.0f, 4.0f)
                                .requiresTool()
                                .sounds(BlockSoundGroup.STONE)
                                .luminance(5),
                        UniformIntProvider.create(3, 7)
                ));

        addBlocksToVanillaItemGroups();
    }

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(SkamMod.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(SkamMod.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    private static void addBlocksToVanillaItemGroups() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(ENCHANTMENT_UPGRADER_BLOCK);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(entries -> {
            entries.add(BURNING_GOLD_ORE);
            entries.add(FROST_IRON_ORE);
            entries.add(THUNDER_ALLOY_ORE);
        });
    }
}
