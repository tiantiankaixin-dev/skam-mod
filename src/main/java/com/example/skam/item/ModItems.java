
package com.example.skam.item;

import com.example.skam.SkamMod;
import com.example.skam.effect.ModStatusEffects;
import com.example.skam.item.core.CoreItem;
import com.example.skam.item.core.CoreType;
import com.example.skam.item.core.PurificationCoreItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.example.skam.item.armor.*;
import com.example.skam.entity.ModEntities;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item LEVEL5_FIRE_CORE = registerItem("level5_fire_core", new Level5FireCore(new FabricItemSettings().rarity(Rarity.EPIC)));
    public static final Item LEVEL5_ICE_CORE = registerItem("level5_ice_core", new Level5IceCore(new FabricItemSettings().rarity(Rarity.EPIC)));
    public static final Item LEVEL5_LIGHTNING_CORE = registerItem("level5_lightning_core", new Level5LightningCore(new FabricItemSettings().rarity(Rarity.EPIC)));
    public static final Item LEVEL4_FIRE_CORE = registerItem("level4_fire_core", new Level4FireCore(new FabricItemSettings().rarity(Rarity.RARE)));
    public static final Item LEVEL4_ICE_CORE = registerItem("level4_ice_core", new Level4IceCore(new FabricItemSettings().rarity(Rarity.RARE)));
    public static final Item LEVEL4_LIGHTNING_CORE = registerItem("level4_lightning_core", new Level4LightningCore(new FabricItemSettings().rarity(Rarity.RARE)));
    public static final Item LEVEL3_FIRE_CORE = registerItem("level3_fire_core", new Level3FireCore(new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item LEVEL3_ICE_CORE = registerItem("level3_ice_core", new Level3IceCore(new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item LEVEL3_LIGHTNING_CORE = registerItem("level3_lightning_core", new Level3LightningCore(new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item LEVEL2_FIRE_CORE = registerItem("level2_fire_core", new Level2FireCore(new FabricItemSettings()));
    public static final Item LEVEL2_ICE_CORE = registerItem("level2_ice_core", new Level2IceCore(new FabricItemSettings()));
    public static final Item LEVEL2_LIGHTNING_CORE = registerItem("level2_lightning_core", new Level2LightningCore(new FabricItemSettings()));
    public static final Item LEVEL1_FIRE_CORE = registerItem("level1_fire_core", new Level1FireCore(new FabricItemSettings()));
    public static final Item LEVEL1_ICE_CORE = registerItem("level1_ice_core", new Level1IceCore(new FabricItemSettings()));
    public static final Item LEVEL1_LIGHTNING_CORE = registerItem("level1_lightning_core", new Level1LightningCore(new FabricItemSettings()));
    public static final Item LEVEL_1_ARCHER_CORE = registerItem("level_1_archer_core", new ArcherCore(new FabricItemSettings(), 1));
    public static final Item LEVEL_2_ARCHER_CORE = registerItem("level_2_archer_core", new ArcherCore(new FabricItemSettings(), 2));
    public static final Item LEVEL_3_ARCHER_CORE = registerItem("level_3_archer_core", new ArcherCore(new FabricItemSettings(), 3));
    public static final Item LEVEL_4_ARCHER_CORE = registerItem("level_4_archer_core", new ArcherCore(new FabricItemSettings(), 4));
    public static final Item LEVEL_5_ARCHER_CORE = registerItem("level_5_archer_core", new ArcherCore(new FabricItemSettings(), 5));
    public static final Item PURIFICATION_CORE_1 = registerItem("purification_core_1", new PurificationCoreItem(1, new FabricItemSettings()));
    public static final Item PURIFICATION_CORE_2 = registerItem("purification_core_2", new PurificationCoreItem(2, new FabricItemSettings()));
    public static final Item PURIFICATION_CORE_3 = registerItem("purification_core_3", new PurificationCoreItem(3, new FabricItemSettings()));
    public static final Item PURIFICATION_CORE_4 = registerItem("purification_core_4", new PurificationCoreItem(4, new FabricItemSettings()));
    public static final Item PURIFICATION_CORE_5 = registerItem("purification_core_5", new PurificationCoreItem(5, new FabricItemSettings()));
    public static final Item VAMPIRE_CORE_LVL_1 = registerItem("vampire_core_lvl_1", new CoreItem(new FabricItemSettings(), CoreType.VAMPIRE, 1));
    public static final Item VAMPIRE_CORE_LVL_2 = registerItem("vampire_core_lvl_2", new CoreItem(new FabricItemSettings(), CoreType.VAMPIRE, 2));
    public static final Item VAMPIRE_CORE_LVL_3 = registerItem("vampire_core_lvl_3", new CoreItem(new FabricItemSettings(), CoreType.VAMPIRE, 3));
    public static final Item VAMPIRE_CORE_LVL_4 = registerItem("vampire_core_lvl_4",new CoreItem(new FabricItemSettings(), CoreType.VAMPIRE, 4));
    public static final Item VAMPIRE_CORE_LVL_5 = registerItem("vampire_core_lvl_5", new CoreItem(new FabricItemSettings(), CoreType.VAMPIRE, 5));
    public static final Item STRENGTH_CORE_LV1= registerItem("strength_core_lv1", new CoreItem(new FabricItemSettings(), CoreType.STRENGTH, 1));
    public static final Item STRENGTH_CORE_LV2 = registerItem("strength_core_lv2", new CoreItem(new FabricItemSettings(), CoreType.STRENGTH, 2));
    public static final Item STRENGTH_CORE_LV3 = registerItem("strength_core_lv3", new CoreItem(new FabricItemSettings(), CoreType.STRENGTH, 3));
    public static final Item STRENGTH_CORE_LV4 = registerItem("strength_core_lv4", new CoreItem(new FabricItemSettings(), CoreType.STRENGTH, 4));
    public static final Item STRENGTH_CORE_LV5 = registerItem("strength_core_lv5", new CoreItem(new FabricItemSettings(), CoreType.STRENGTH, 5));



    // --- 材料与资源 (Materials & Resources) ---
    public static final Item BURNING_RAW_GOLD = registerItem("burning_raw_gold", new Item(new FabricItemSettings()));
    public static final Item FIRE_ETHER = registerItem("fire_ether", new Item(new FabricItemSettings()));
    public static final Item FROST_RAW_IRON = registerItem("frost_raw_iron", new Item(new FabricItemSettings()));
    public static final Item THUNDER_DEBRIS = registerItem("thunder_debris", new Item(new FabricItemSettings().fireproof())); // 远古残骸是防火的
    public static final Item BURNING_GOLD_INGOT = registerItem("burning_gold_ingot", new Item(new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item FROST_IRON_INGOT = registerItem("frost_iron_ingot", new Item(new FabricItemSettings().rarity(Rarity.UNCOMMON)));
    public static final Item THUNDER_INGOT = registerItem("thunder_ingot", new Item(new FabricItemSettings().rarity(Rarity.UNCOMMON)));

    // --- 魔法物品 (Magical Items) ---
   public static final Item MAGIC_ENERGY_GEM = registerItem("magic_energy_gem", new MagicEnergyGemItem(new FabricItemSettings().rarity(Rarity.EPIC)));
    public static final Item DIMENSION_HOPPER = registerItem("dimension_hopper", new DimensionHopperItem(new FabricItemSettings()));

    // --- 武器 (Weapons) ---
   public static final Item NETHERITE_FIRE_SWORD = registerItem("netherite_fire_sword", new NetheriteFireSword());
    public static final Item NETHERITE_ICE_SWORD = registerItem("netherite_ice_sword", new NetheriteIceSword());
    public static final Item NETHERITE_LIGHTNING_SWORD = registerItem("netherite_lightning_sword", new NetheriteLightningSword());
    public static final Item DIAMOND_FIRE_SWORD = registerItem("diamond_fire_sword", new DiamondFireSword());
    public static final Item DIAMOND_ICE_SWORD = registerItem("diamond_ice_sword", new DiamondIceSword());
    public static final Item DIAMOND_LIGHTNING_SWORD = registerItem("diamond_lightning_sword", new DiamondLightningSword());
    public static final Item IRON_FIRE_SWORD = registerItem("iron_fire_sword", new IronFireSword());
    public static final Item IRON_ICE_SWORD = registerItem("iron_ice_sword", new IronIceSword());
    public static final Item IRON_LIGHTNING_SWORD = registerItem("iron_lightning_sword", new IronLightningSword());
    public static final Item LEGEND_BOW = registerItem("legend_bow", new LegendBowItem(new FabricItemSettings().maxDamage(1024).rarity(Rarity.EPIC)));
    public static final Item EMBER_STING = registerItem("ember_sting", new EmberStingItem(ModToolMaterials.LEVEL1_CORE_MATERIAL, 4, -2.4f, new FabricItemSettings()));
    public static final Item CHILL_IRON_BLADE = registerItem("chill_iron_blade", new ChillIronBladeItem(ModToolMaterials.LEVEL1_CORE_MATERIAL, 4, -2.4f, new FabricItemSettings()));
    public static final Item STATIC_DIRK = registerItem("static_dirk", new StaticDirkItem(ModToolMaterials.LEVEL1_CORE_MATERIAL, 3, -2.1f, new FabricItemSettings()));
    public static final Item MOLTEN_GREATAXE = registerItem("molten_greataxe", new MoltenGreataxeItem(ModToolMaterials.LEVEL3_CORE_MATERIAL, 6, -3.0f, new FabricItemSettings().fireproof())); // 巨斧伤害更高，攻速更慢，并且防火
    public static final Item HEARTFIRE_FURNACE_PICK = registerItem("heartfire_furnace_pick", new HeartfireFurnacePickItem(ModToolMaterials.LEVEL3_CORE_MATERIAL, 2, -2.8f, new FabricItemSettings().fireproof())); // 防火
    public static final Item DEEPFROST_RAPIER = registerItem("deepfrost_rapier", new DeepfrostRapierItem(ModToolMaterials.LEVEL3_CORE_MATERIAL, 4, -2.4f, new FabricItemSettings().fireproof())); // 防火
    public static final Item GLACIAL_SCYTHE = registerItem("glacial_scythe", new GlacialScytheItem(ModToolMaterials.LEVEL3_CORE_MATERIAL, 1, -3.0f, new FabricItemSettings().fireproof())); // 防火
    public static final Item STORM_CALLERS_EDGE = registerItem("storm_callers_edge", new StormCallersEdgeItem(ModToolMaterials.LEVEL3_CORE_MATERIAL, 4, -2.2f, new FabricItemSettings().fireproof())); // 防火
    public static final Item STORM_SCEPTER = registerItem("storm_scepter", new StormScepterItem(new FabricItemSettings().maxDamage(256).fireproof())); // 设置耐久和防火
    public static final Item FROST_BLADE = registerItem("frost_blade", new FrostBladeItem(ModToolMaterials.FROST_STEEL, 5, -2.4f, new FabricItemSettings().rarity(Rarity.RARE).fireproof())); // 如此强大的武器理应防火
    public static final Item STAFF_OF_THUNDER = registerItem("staff_of_thunder", new StaffOfThunderItem(new FabricItemSettings().rarity(Rarity.EPIC).fireproof()));
    public static final Item HEART_OF_BURNING_GOLD = registerItem("heart_of_burning_gold", new HeartOfBurningGoldItem(new FabricItemSettings().rarity(Rarity.RARE)));
    public static final Item SCEPTER_OF_THE_SOLAR_FLARE = registerItem("scepter_of_the_solar_flare", new com.example.skam.item.ScepterOfTheSolarFlareItem(new FabricItemSettings().maxDamage(50).rarity(Rarity.EPIC).fireproof()));

    // --- 工具与功能性物品 (Tools & Utility) ---
    public static final Item HANDHELD_DISPENSER = registerItem("handheld_dispenser", new HandheldDispenserItem(new FabricItemSettings().maxCount(1)));
    public static final Item STANCE_GENERATOR = registerItem("stance_generator", new StanceGeneratorItem(new FabricItemSettings().maxCount(1)));
    public static final Item SWORD_SHEATH = registerItem("sword_sheath", new SwordSheathItem(new FabricItemSettings()));
    public static final Item CURSED_TRIDENT_ENTITY_ITEM = registerItem("cursed_trident_entity", new Item(new Item.Settings()));
    public static final Item ORE_DETECTOR = registerItem("ore_detector", new OreDetectorItem(new FabricItemSettings().maxDamage(100).rarity(Rarity.UNCOMMON)));
    public static final Item FLOATING_SHIP_ITEM = registerItem("floating_ship_item", new FloatingShipItem(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE)));
    public static final Item TELEPORTING_TOTEM = registerItem("teleporting_totem", new TeleportingTotemItem(new FabricItemSettings()));
    public static final Item WIND_CALLER_HORN = registerItem("wind_caller_horn", new WindCallerHornItem(new FabricItemSettings()));
    public static final Item ETHEREAL_EYE = registerItem("ethereal_eye", new EtherealEyeItem(new FabricItemSettings()));
    public static final Item BLOCK_EXTRACTOR = registerItem("block_extractor", new BlockExtractorItem(new FabricItemSettings().maxCount(1)));
    public static final Item SOUL_LINK_CHARM = registerItem("soul_link_charm", new Item(new FabricItemSettings().maxCount(1)));
    public static final Item FLAME_UPGRADE = registerItem("flame_upgrade", new Item(new FabricItemSettings().maxCount(64)));
    public static final Item ICE_UPGRADE = registerItem("ice_upgrade", new Item(new FabricItemSettings().maxCount(64)));
    public static final Item LIGHTNING_UPGRADE = registerItem("lightning_upgrade", new Item(new FabricItemSettings().maxCount(64)));
    public static final Item CORE_CAPACITY_EXPANDER = registerItem("core_capacity_expander", new CoreCapacityExpander(new FabricItemSettings().rarity(Rarity.EPIC).maxCount(16)));
    public static final Item TREASURE_SUMMONER = registerItem("treasure_summoner", new TreasureSummonerItem(new FabricItemSettings().maxCount(16)));


    // --- 神之盔甲 (God Armors) ---
    public static final Item FIRE_GOD_HELMET = registerItem("fire_god_helmet", new FireGodArmorItem(FireGodArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings().fireproof().rarity(Rarity.EPIC)));
    public static final Item FIRE_GOD_CHESTPLATE = registerItem("fire_god_chestplate", new FireGodArmorItem(FireGodArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings().fireproof().rarity(Rarity.EPIC)));
    public static final Item FIRE_GOD_LEGGINGS = registerItem("fire_god_leggings", new FireGodArmorItem(FireGodArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings().fireproof().rarity(Rarity.EPIC)));
    public static final Item FIRE_GOD_BOOTS = registerItem("fire_god_boots", new FireGodArmorItem(FireGodArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings().fireproof().rarity(Rarity.EPIC)));

    public static final Item ICE_GOD_HELMET = registerItem("ice_god_helmet", new IceGodArmorItem(IceGodArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings().rarity(Rarity.EPIC)));
    public static final Item ICE_GOD_CHESTPLATE = registerItem("ice_god_chestplate", new IceGodArmorItem(IceGodArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings().rarity(Rarity.EPIC)));
    public static final Item ICE_GOD_LEGGINGS = registerItem("ice_god_leggings", new IceGodArmorItem(IceGodArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings().rarity(Rarity.EPIC)));
    public static final Item ICE_GOD_BOOTS = registerItem("ice_god_boots", new IceGodArmorItem(IceGodArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings().rarity(Rarity.EPIC)));

    public static final Item THUNDER_GOD_HELMET = registerItem("thunder_god_helmet", new ThunderGodArmorItem(ThunderGodArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new FabricItemSettings().rarity(Rarity.EPIC)));
    public static final Item THUNDER_GOD_CHESTPLATE = registerItem("thunder_god_chestplate", new ThunderGodArmorItem(ThunderGodArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new FabricItemSettings().rarity(Rarity.EPIC)));
    public static final Item THUNDER_GOD_LEGGINGS = registerItem("thunder_god_leggings", new ThunderGodArmorItem(ThunderGodArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new FabricItemSettings().rarity(Rarity.EPIC)));
    public static final Item THUNDER_GOD_BOOTS = registerItem("thunder_god_boots", new ThunderGodArmorItem(ThunderGodArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new FabricItemSettings().rarity(Rarity.EPIC)));

    // --- 刷怪蛋 (Spawn Eggs) ---
    public static final Item FLAME_EMPEROR_SPAWN_EGG = registerItem("flame_emperor_spawn_egg", new SpawnEggItem(ModEntities.FLAME_EMPEROR, 0xFF4500, 0xFF0000, new FabricItemSettings()));
    public static final Item ICE_EMPEROR_SPAWN_EGG = registerItem("ice_emperor_spawn_egg", new SpawnEggItem(ModEntities.ICE_EMPEROR, 0x87CEEB, 0x4169E1, new FabricItemSettings()));
    public static final Item LIGHTNING_EMPEROR_SPAWN_EGG = registerItem("lightning_emperor_spawn_egg", new SpawnEggItem(ModEntities.LIGHTNING_EMPEROR, 0xFFD700, 0x4B0082, new FabricItemSettings()));

    //吃的
    public static final Item PURIFYING_APPLE = registerItem("purifying_apple", new PurifyingAppleItem(new FabricItemSettings()));
    public static final Item TIME_AMPLIFYING_FRUIT = registerItem("time_amplifying_fruit", new TimeAmplifyingFruit());
    public static final FoodComponent DEMON_KING_HEART_FOOD_COMPONENT = new FoodComponent.Builder().hunger(6).saturationModifier(0.8f).meat().alwaysEdible().statusEffect(new StatusEffectInstance(ModStatusEffects.DEMON_KING, 20 * 60, 0), 1.0f)

            .build();

    public static final Item DEMON_KING_HEART = registerItem("demon_king_heart", new DemonKingHeartItem(new FabricItemSettings().food(DEMON_KING_HEART_FOOD_COMPONENT)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(SkamMod.MOD_ID, name), item);
    }

    public static void registerModItems() {
        SkamMod.LOGGER.info("Registering Mod Items for " + SkamMod.MOD_ID);
    }
}
