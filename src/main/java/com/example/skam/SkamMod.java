package com.example.skam;

import com.example.skam.loot.ModLootTableModifiers;
import com.example.skam.registries.ModRegistries;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkamMod implements ModInitializer {
    public static final String MOD_ID = "skam";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // These keys and layers are fundamental and can remain here.
    // They don't involve complex registration logic.
    public static final RegistryKey<World> BROKEN_CONTINENT_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, new Identifier(MOD_ID, "broken_continent"));
    public static final EntityModelLayer FIRE_GOD_ARMOR_LAYER = new EntityModelLayer(new Identifier(MOD_ID, "fire_god_armor"), "main");

    @Override
    public void onInitialize() {
        // Initialize all mod content through the centralized registry.
        // This single call handles items, blocks, screen handlers, events, etc.
        ModRegistries.initialize();
        ModLootTableModifiers.modifyLootTables();
    }
}
