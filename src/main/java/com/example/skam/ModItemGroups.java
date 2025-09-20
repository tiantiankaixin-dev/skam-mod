// src/main/java/com/example/skam/item/ModItemGroups.java

package com.example.skam;


import com.example.skam.block.ModBlocks;
import com.example.skam.item.ArcherCore;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.item.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;

import java.util.List;

public class ModItemGroups {
    // 1. 声明你的物品组
    public static ItemGroup SKAM_GROUP;
    public static ItemGroup SKAM_BLOCK_GROUP;

    // 2. 创建一个注册方法
    public static void registerItemGroups() {
        SkamMod.LOGGER.info("Registering Item Groups for " + SkamMod.MOD_ID);

        SKAM_GROUP = Registry.register(Registries.ITEM_GROUP,
                new Identifier(SkamMod.MOD_ID, "skam_group"),
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemgroup.skam_group"))
                        .icon(() -> new ItemStack(ModItems.NETHERITE_ICE_SWORD)) // 确保图标物品已注册
                        .entries((displayContext, entries) -> {
                            entries.add(ModItems.IRON_FIRE_SWORD);
                            entries.add(ModItems.IRON_ICE_SWORD);
                            entries.add(ModItems.IRON_LIGHTNING_SWORD);
                            entries.add(ModItems.DIAMOND_FIRE_SWORD);
                            entries.add(ModItems.DIAMOND_ICE_SWORD);
                            entries.add(ModItems.DIAMOND_LIGHTNING_SWORD);
                            entries.add(ModItems.NETHERITE_FIRE_SWORD);
                            entries.add(ModItems.NETHERITE_ICE_SWORD);
                            entries.add(ModItems.NETHERITE_LIGHTNING_SWORD);
                            entries.add(ModItems.LEVEL1_FIRE_CORE);
                            entries.add(ModItems.LEVEL1_ICE_CORE);
                            entries.add(ModItems.LEVEL1_LIGHTNING_CORE);
                            entries.add(ModItems.LEVEL2_FIRE_CORE);
                            entries.add(ModItems.LEVEL2_ICE_CORE);
                            entries.add(ModItems.LEVEL2_LIGHTNING_CORE);
                            entries.add(ModItems.LEVEL3_FIRE_CORE);
                            entries.add(ModItems.LEVEL3_ICE_CORE);
                            entries.add(ModItems.LEVEL3_LIGHTNING_CORE);
                            entries.add(ModItems.LEVEL4_FIRE_CORE);
                            entries.add(ModItems.LEVEL4_ICE_CORE);
                            entries.add(ModItems.LEVEL4_LIGHTNING_CORE);
                            entries.add(ModItems.LEVEL5_FIRE_CORE);
                            entries.add(ModItems.LEVEL5_ICE_CORE);
                            entries.add(ModItems.LEVEL5_LIGHTNING_CORE);
                            //entries.add(ModItems.FIRE_GOD_HELMET);
                            //entries.add(ModItems.FIRE_GOD_CHESTPLATE);
                            //entries.add(ModItems.FIRE_GOD_LEGGINGS);
                            //entries.add(ModItems.FIRE_GOD_BOOTS);
                            //entries.add(ModItems.ICE_GOD_HELMET);
                            //entries.add(ModItems.ICE_GOD_CHESTPLATE);
                            //entries.add(ModItems.ICE_GOD_LEGGINGS);
                            //entries.add(ModItems.ICE_GOD_BOOTS);
                            //entries.add(ModItems.THUNDER_GOD_HELMET);
                            //entries.add(ModItems.THUNDER_GOD_CHESTPLATE);
                            //entries.add(ModItems.THUNDER_GOD_LEGGINGS);
                            //entries.add(ModItems.THUNDER_GOD_BOOTS);
                            entries.add(ModItems.BURNING_RAW_GOLD);
                            entries.add(ModItems.FROST_RAW_IRON);
                            entries.add(ModItems.THUNDER_DEBRIS);
                            entries.add(ModItems.BURNING_GOLD_INGOT);
                            entries.add(ModItems.FROST_IRON_INGOT);
                            entries.add(ModItems.THUNDER_INGOT);
                            entries.add(ModItems.FLAME_UPGRADE);
                            entries.add(ModItems.ICE_UPGRADE);
                            entries.add(ModItems.LIGHTNING_UPGRADE);
                            entries.add(ModItems.CORE_CAPACITY_EXPANDER);
                            entries.add(ModItems.EMBER_STING);
                            entries.add(ModItems.CHILL_IRON_BLADE);
                            entries.add(ModItems.STATIC_DIRK);
                            entries.add(ModItems.MOLTEN_GREATAXE);
                            entries.add(ModItems.HEARTFIRE_FURNACE_PICK);
                            entries.add(ModItems.DEEPFROST_RAPIER);
                            entries.add(ModItems.GLACIAL_SCYTHE);
                            entries.add(ModItems.STORM_CALLERS_EDGE);
                            entries.add(ModItems.STORM_SCEPTER);
                            entries.add(ModItems.STAFF_OF_THUNDER);
                            entries.add(ModItems.HEART_OF_BURNING_GOLD);
                            entries.add(ModItems.FROST_BLADE);
                            entries.add(ModItems.SCEPTER_OF_THE_SOLAR_FLARE);
                            entries.add(ModItems.MAGIC_ENERGY_GEM);
                            entries.add(ModItems.PURIFYING_APPLE);
                            entries.add(ModItems.TIME_AMPLIFYING_FRUIT);
                            entries.add(ModItems.DEMON_KING_HEART);
                            entries.add(ModItems.TELEPORTING_TOTEM);
                            entries.add(ModItems.WIND_CALLER_HORN);
                            entries.add(ModItems.ETHEREAL_EYE);
                            entries.add(ModItems.BLOCK_EXTRACTOR);
                            entries.add(ModItems.SOUL_LINK_CHARM);
                            entries.add(ModItems.LEGEND_BOW);
                            entries.add(ModItems.HANDHELD_DISPENSER);
                            entries.add(SkamMod.SWORD_SHEATH);
                            entries.add(ModItems.STANCE_GENERATOR);
                            entries.add(ModItems.ORE_DETECTOR);
                            entries.add(ModItems.LEVEL_1_ARCHER_CORE);
                            entries.add(ModItems.LEVEL_2_ARCHER_CORE);
                            entries.add(ModItems.LEVEL_3_ARCHER_CORE);
                            entries.add(ModItems.LEVEL_4_ARCHER_CORE);
                            entries.add(ModItems.LEVEL_5_ARCHER_CORE);
                            entries.add(ModItems.PURIFICATION_CORE_1);
                            entries.add(ModItems.PURIFICATION_CORE_2);
                            entries.add(ModItems.PURIFICATION_CORE_3);
                            entries.add(ModItems.PURIFICATION_CORE_4);
                            entries.add(ModItems.PURIFICATION_CORE_5);
                            entries.add(ModItems.VAMPIRE_CORE_LVL_1);
                            entries.add(ModItems.VAMPIRE_CORE_LVL_2);
                            entries.add(ModItems.VAMPIRE_CORE_LVL_3);
                            entries.add(ModItems.VAMPIRE_CORE_LVL_4);
                            entries.add(ModItems.VAMPIRE_CORE_LVL_5);
                            entries.add(ModItems.STRENGTH_CORE_LV1);
                            entries.add(ModItems.STRENGTH_CORE_LV2);
                            entries.add(ModItems.STRENGTH_CORE_LV3);
                            entries.add(ModItems.STRENGTH_CORE_LV4);
                            entries.add(ModItems.STRENGTH_CORE_LV5);

                            // entries.add(ModItems.FLOATING_SHIP_ITEM);

                            for (int level = 1; level <= ModEnchantments.SKY_STRIKE.getMaxLevel(); level++) {
                                ItemStack skyStrikeBook = new ItemStack(Items.ENCHANTED_BOOK);
                                EnchantedBookItem.addEnchantment(skyStrikeBook, new EnchantmentLevelEntry(ModEnchantments.SKY_STRIKE, level));
                                entries.add(skyStrikeBook);
                            }

                            List<Enchantment> charms = List.of(
                                    ModEnchantments.POWER_CHARM, ModEnchantments.NIGHT_VISION_CHARM,
                                    ModEnchantments.HASTE_CHARM, ModEnchantments.WATER_BREATHING_CHARM, ModEnchantments.DOLPHINS_GRACE_CHARM,
                                    ModEnchantments.REGENERATION_CHARM, ModEnchantments.ABSORPTION_CHARM,ModEnchantments.TRIDENT_RIDER
                            );

                            for (Enchantment charm : charms) {
                                int maxLevelToShow = charm.equals(ModEnchantments.POWER_CHARM) ? 2 : 1;
                                for (int i = 1; i <= maxLevelToShow; i++) {
                                    ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                                    EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentLevelEntry(charm, i));
                                    entries.add(enchantedBook);
                                }
                            }

                            for (int level = 1; level <= ModEnchantments.SPEED_CHARM.getMaxLevel(); level++) {
                                ItemStack speedCharmBook= new ItemStack(Items.ENCHANTED_BOOK);
                                EnchantedBookItem.addEnchantment(speedCharmBook, new EnchantmentLevelEntry(ModEnchantments.SPEED_CHARM, level));
                                entries.add(speedCharmBook);
                            }

                            for (int level = 1; level <= ModEnchantments.HEALTH_BOOST_CHARM.getMaxLevel(); level++) {
                                ItemStack healthBoostCharmBook= new ItemStack(Items.ENCHANTED_BOOK);
                                EnchantedBookItem.addEnchantment(healthBoostCharmBook, new EnchantmentLevelEntry(ModEnchantments.HEALTH_BOOST_CHARM, level));
                                entries.add(healthBoostCharmBook);
                            }


                            for (int level = 1; level <= ModEnchantments.EXPLOSIVE_IMPACT.getMaxLevel(); level++) {
                                ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
                                EnchantedBookItem.addEnchantment(book, new EnchantmentLevelEntry(ModEnchantments.EXPLOSIVE_IMPACT, level));
                                entries.add(book);
                            }
                            ItemStack thunderCallerBook = new ItemStack(Items.ENCHANTED_BOOK);
                            EnchantedBookItem.addEnchantment(thunderCallerBook, new EnchantmentLevelEntry(ModEnchantments.THUNDER_CALLER, 1));
                            entries.add(thunderCallerBook);

                            for (int level = 1; level <= ModEnchantments.AIR_JUMPER.getMaxLevel(); level++) {
                                ItemStack airJumperBook= new ItemStack(Items.ENCHANTED_BOOK);
                                EnchantedBookItem.addEnchantment(airJumperBook, new EnchantmentLevelEntry(ModEnchantments.AIR_JUMPER, level));
                                entries.add(airJumperBook);
                            }

                            for (int level = 1; level <= ModEnchantments.TIME_LAG_THORN.getMaxLevel(); level++) {
                                ItemStack timelagthornBook= new ItemStack(Items.ENCHANTED_BOOK);
                                EnchantedBookItem.addEnchantment(timelagthornBook, new EnchantmentLevelEntry(ModEnchantments.TIME_LAG_THORN, level));
                                entries.add(timelagthornBook);
                            }



                            entries.add(ModItems.FLAME_EMPEROR_SPAWN_EGG);
                            entries.add(ModItems.ICE_EMPEROR_SPAWN_EGG);
                            entries.add(ModItems.LIGHTNING_EMPEROR_SPAWN_EGG);
                            entries.add(SkamMod.WHITE_PORTAL_BLOCK);
                            entries.add(ModItems.TREASURE_SUMMONER);
                        })

                        .build());

        SKAM_BLOCK_GROUP = Registry.register(Registries.ITEM_GROUP,
                new Identifier(SkamMod.MOD_ID, "skam_block_group"), // 这是方块组的ID
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemgroup.skam_blocks")) // 方块组的语言键
                        .icon(() -> new ItemStack(ModBlocks.THUNDER_ALLOY_ORE)) // 用一个漂亮的矿石做图标
                        .entries((displayContext, entries) -> {
                            // 这里只添加方块
                            entries.add(ModBlocks.ENCHANTMENT_UPGRADER_BLOCK);
                            entries.add(ModBlocks.BURNING_GOLD_ORE);
                            entries.add(ModBlocks.FROST_IRON_ORE);
                            entries.add(ModBlocks.THUNDER_ALLOY_ORE);
                            entries.add(ModBlocks.FORGING_TABLE);
                            entries.add(ModBlocks.BURNING_GOLD_BLOCK);
                            entries.add(ModBlocks.FROST_IRON_BLOCK);
                            entries.add(ModBlocks.THUNDER_BLOCK);
                            // 如果你未来有更多的“方块”，都加在这里
                        })
                        .build());
    }
}
