package com.example.skam.loot;

import com.example.skam.util.CoreNbtApplicator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.*;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.JsonHelper;
import java.util.Random;

public class SetRandomCoreCapacityFunction extends ConditionalLootFunction {

    final LootNumberProvider minCapacityProvider;
    final LootNumberProvider maxCapacityProvider;

    protected SetRandomCoreCapacityFunction(LootCondition[] conditions, LootNumberProvider min, LootNumberProvider max) {
        super(conditions);
        this.minCapacityProvider = min;
        this.maxCapacityProvider = max;
    }

    @Override
    public LootFunctionType getType() {
        return ModLootFunctionTypes.SET_RANDOM_CORE_CAPACITY;
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        Item item = stack.getItem();

        // [!!关键改动!!] 在这里加入了 ShieldItem
        if (item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem || item instanceof ArmorItem || item instanceof BowItem || item instanceof CrossbowItem || item instanceof ShieldItem) {

            int min = this.minCapacityProvider.nextInt(context);
            int max = this.maxCapacityProvider.nextInt(context);

            if (min > max) {
                int temp = min;
                min = max;
                max = temp;
            }

            int range = max - min + 1;
            int capacity = min + context.getRandom().nextInt(range);

            NbtCompound rootNbt = stack.getOrCreateNbt();
            NbtCompound skamNbt = rootNbt.contains("skam_mods", 10) ? rootNbt.getCompound("skam_mods") : new NbtCompound();

            skamNbt.putInt("max_capacity", capacity);
            rootNbt.put("skam_mods", skamNbt);

            CoreNbtApplicator.applyCoreModifications(stack);
        }

        return stack;
    }

    public static Builder builder(LootNumberProvider min, LootNumberProvider max) {
        return builder((conditions) -> {
            return new SetRandomCoreCapacityFunction(conditions, min, max);
        });
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<SetRandomCoreCapacityFunction> {
        @Override
        public void toJson(JsonObject jsonObject, SetRandomCoreCapacityFunction function, JsonSerializationContext context) {
            super.toJson(jsonObject, function, context);
            jsonObject.add("min_capacity", context.serialize(function.minCapacityProvider));
            jsonObject.add("max_capacity", context.serialize(function.maxCapacityProvider));
        }

        @Override
        public SetRandomCoreCapacityFunction fromJson(JsonObject jsonObject, JsonDeserializationContext context, LootCondition[] conditions) {
            LootNumberProvider min = JsonHelper.deserialize(jsonObject, "min_capacity", context, LootNumberProvider.class);
            LootNumberProvider max = JsonHelper.deserialize(jsonObject, "max_capacity", context, LootNumberProvider.class);
            return new SetRandomCoreCapacityFunction(conditions, min, max);
        }
    }
}
