package com.example.skam.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ModLootTableModifiers {

    private static final Identifier COMMON_TREASURE = new Identifier("skam", "chests/common_treasure");
    private static final Identifier UNCOMMON_TREASURE = new Identifier("skam", "chests/uncommon_treasure");
    private static final Identifier RARE_TREASURE = new Identifier("skam", "chests/rare_treasure");
    private static final Identifier LEGENDARY_TREASURE = new Identifier("skam", "chests/legendary_treasure");

    // --- Loot Table Identifiers ---
    private static final List<Identifier> OVERWORLD_COMMON = new ArrayList<>() {{
        add(new Identifier("minecraft", "chests/spawn_bonus_chest"));
        add(new Identifier("minecraft", "chests/village/village_weaponsmith"));
        add(new Identifier("minecraft", "chests/abandoned_mineshaft"));
    }};

    private static final List<Identifier> OVERWORLD_UNCOMMON = new ArrayList<>() {{
        add(new Identifier("minecraft", "chests/jungle_temple"));
        add(new Identifier("minecraft", "chests/pillager_outpost"));
        add(new Identifier("minecraft", "chests/shipwreck_treasure"));
    }};

    private static final List<Identifier> OVERWORLD_RARE = new ArrayList<>() {{
        add(new Identifier("minecraft", "chests/desert_pyramid"));
        add(new Identifier("minecraft", "chests/stronghold_corridor"));
        add(new Identifier("minecraft", "chests/woodland_mansion"));
    }};

    private static final List<Identifier> NETHER_TABLES = new ArrayList<>() {{
        add(new Identifier("minecraft", "chests/bastion_treasure"));
        add(new Identifier("minecraft", "chests/bastion_other"));
        add(new Identifier("minecraft", "chests/bastion_bridge"));
        add(new Identifier("minecraft", "chests/nether_bridge"));
        add(new Identifier("minecraft", "chests/ruined_portal"));
    }};

    private static final List<Identifier> END_TABLES = new ArrayList<>() {{
        add(new Identifier("minecraft", "chests/end_city_treasure"));
    }};

    public static void modifyLootTables() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            String namespace = id.getNamespace();

            // --- Nether Logic ---
            if (NETHER_TABLES.contains(id)) {
                // Nether chests get common items and a chance for fire-related uncommon items
                tableBuilder.pool(buildPool(COMMON_TREASURE, 1.0f));
                tableBuilder.pool(buildPool(new Identifier("skam", "item_pools/fire_cores_only"), 0.4f));
                return; // Stop further processing for Nether chests
            }

            // --- Overworld Logic ---
            if (OVERWORLD_COMMON.contains(id)) {
                tableBuilder.pool(buildPool(COMMON_TREASURE, 0.8f));
            } else if (OVERWORLD_UNCOMMON.contains(id)) {
                tableBuilder.pool(buildPool(COMMON_TREASURE, 1.0f));
                tableBuilder.pool(buildPool(UNCOMMON_TREASURE, 0.6f));
            } else if (OVERWORLD_RARE.contains(id)) {
                tableBuilder.pool(buildPool(UNCOMMON_TREASURE, 1.0f));
                tableBuilder.pool(buildPool(RARE_TREASURE, 0.7f));
                tableBuilder.pool(buildPool(LEGENDARY_TREASURE, 0.1f));
                tableBuilder.pool(buildCoreCapacityPool());
            }

            // --- End Logic ---
            if (END_TABLES.contains(id)) {
                tableBuilder.pool(buildPool(RARE_TREASURE, 1.0f));
                tableBuilder.pool(buildPool(LEGENDARY_TREASURE, 0.5f));
                tableBuilder.pool(buildCoreCapacityPool());
            }

            // --- Other Mods Logic ---
            if (!namespace.equals("minecraft") && !namespace.equals("skam")) {
                tableBuilder.pool(buildPool(UNCOMMON_TREASURE, 0.5f));
            }
        });
    }

    private static LootPool.Builder buildPool(Identifier lootTableId, float chance) {
        return LootPool.builder()
                .with(LootTableEntry.builder(lootTableId))
                .conditionally(RandomChanceLootCondition.builder(chance));
    }

    private static LootPool.Builder buildCoreCapacityPool() {
        return LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(net.minecraft.loot.entry.EmptyEntry.builder()) // This pool adds no items, only applies a function
                .apply(SetRandomCoreCapacityFunction.builder(ConstantLootNumberProvider.create(1.0f), ConstantLootNumberProvider.create(2.0f)))
                .conditionally(RandomChanceLootCondition.builder(0.1f));
    }
}
