// src/main/java/com/example/skam/entity/ModEntities.java
package com.example.skam.entity;

import com.example.skam.Skam;
import com.example.skam.SkamMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {


    public static final EntityType<FloatingShipEntity> FLOATING_SHIP = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Skam.MOD_ID, "floating_ship"),

            FabricEntityTypeBuilder.<FloatingShipEntity>create(SpawnGroup.MISC, FloatingShipEntity::new)
                    .dimensions(EntityType.BOAT.getDimensions())
                    .build()
    );


    public static final EntityType<FallingSwordEntity> FALLING_SWORD = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(SkamMod.MOD_ID, "falling_sword"),
            FabricEntityTypeBuilder.<FallingSwordEntity>create(SpawnGroup.MISC, FallingSwordEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 2.0f))
                    .trackRangeBlocks(128)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<FlameEmperorEntity> FLAME_EMPEROR = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(SkamMod.MOD_ID, "flame_emperor"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, FlameEmperorEntity::new)
                    .dimensions(EntityDimensions.fixed(1.4f, 3.5f))
                    .build()
    );

    public static final EntityType<IceEmperorEntity> ICE_EMPEROR = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(SkamMod.MOD_ID, "ice_emperor"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, IceEmperorEntity::new)
                    .dimensions(EntityDimensions.fixed(1.4f, 3.5f))
                    .build()
    );

    public static final EntityType<LightningEmperorEntity> LIGHTNING_EMPEROR = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(SkamMod.MOD_ID, "lightning_emperor"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, LightningEmperorEntity::new)
                    .dimensions(EntityDimensions.fixed(1.4f, 3.5f))
                    .build()
    );

    public static final EntityType<LegendArrowEntity> LEGEND_ARROW = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(SkamMod.MOD_ID, "legend_arrow"),
            FabricEntityTypeBuilder.<LegendArrowEntity>create(SpawnGroup.MISC, LegendArrowEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
                    .trackRangeBlocks(4).trackedUpdateRate(20)
                    .build()
    );

    public static final EntityType<BlackHoleEntity> BLACK_HOLE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(SkamMod.MOD_ID, "black_hole"),
            FabricEntityTypeBuilder.<BlackHoleEntity>create(SpawnGroup.MISC, BlackHoleEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .build()
    );

    public static void registerEntities() {
    }

    public static void registerModEntities() {
    }

    public static void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(FLAME_EMPEROR, FlameEmperorEntity.createFlameEmperorAttributes());
        FabricDefaultAttributeRegistry.register(ICE_EMPEROR, IceEmperorEntity.createIceEmperorAttributes());
        FabricDefaultAttributeRegistry.register(LIGHTNING_EMPEROR, LightningEmperorEntity.createLightningEmperorAttributes());
    }
}
