package com.example.skam.item;

import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import java.util.function.Supplier;

public enum ModToolMaterials implements ToolMaterial {

    LEVEL1_CORE_MATERIAL(2, 300, 6.0f, 2.0f, 18,
            () -> Ingredient.ofItems(Items.IRON_INGOT)),

    LEVEL3_CORE_MATERIAL(4, 2200, 9.0f, 4.0f, 25,
            () -> Ingredient.ofItems(Items.NETHERITE_INGOT)),

    // [新增] 为“极冬之怒”设计的专属材质
    FROST_STEEL(4, 2031, 9.0f, 4.0f, 25,
            () -> Ingredient.ofItems(ModItems.FROST_IRON_INGOT));
    // 挖掘等级4 (同下界合金), 耐久2031 (同下界合金), 挖掘速度9.0, 基础攻击力加成4.0, 附魔能力25 (高于下界合金)


    private final int miningLevel;
    private final int itemDurability;
    private final float miningSpeed;
    private final float attackDamage;
    private final int enchantability;
    private final Supplier<Ingredient> repairIngredient;

    ModToolMaterials(int miningLevel, int itemDurability, float miningSpeed, float attackDamage, int enchantability, Supplier<Ingredient> repairIngredient) {
        this.miningLevel = miningLevel;
        this.itemDurability = itemDurability;
        this.miningSpeed = miningSpeed;
        this.attackDamage = attackDamage;
        this.enchantability = enchantability;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurability() {
        return this.itemDurability;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return this.miningSpeed;
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamage;
    }

    @Override
    public int getMiningLevel() {
        return this.miningLevel;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
}
