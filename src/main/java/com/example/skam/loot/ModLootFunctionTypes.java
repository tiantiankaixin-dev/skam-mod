package com.example.skam.loot;

import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModLootFunctionTypes {

    // 创建一个静态实例，ID为 "skam:set_random_core_capacity"
    public static final LootFunctionType SET_RANDOM_CORE_CAPACITY = new LootFunctionType(new SetRandomCoreCapacityFunction.Serializer());

    /**
     * 注册方法
     */
    public static void register() {
        Registry.register(
                Registries.LOOT_FUNCTION_TYPE,
                new Identifier("skam", "set_random_core_capacity"),
                SET_RANDOM_CORE_CAPACITY
        );
    }
}
