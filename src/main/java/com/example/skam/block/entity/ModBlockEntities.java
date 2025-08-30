package com.example.skam.block.entity;

import com.example.skam.Skam;
import com.example.skam.SkamMod;
import com.example.skam.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static final BlockEntityType<ForgingTableBlockEntity> FORGING_TABLE_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(Skam.MOD_ID, "forging_table_be"),
                    FabricBlockEntityTypeBuilder.create(ForgingTableBlockEntity::new,
                            ModBlocks.FORGING_TABLE).build());

    public static final BlockEntityType<EnchantmentUpgraderBlockEntity> ENCHANTMENT_UPGRADER_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(SkamMod.MOD_ID, "enchantment_upgrader_be"),
                    FabricBlockEntityTypeBuilder.create(EnchantmentUpgraderBlockEntity::new,
                            ModBlocks.ENCHANTMENT_UPGRADER_BLOCK).build());

    public static void registerBlockEntities() {

    }
}
