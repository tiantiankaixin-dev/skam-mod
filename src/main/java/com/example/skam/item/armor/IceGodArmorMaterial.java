package com.example.skam.item.armor;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
public class IceGodArmorMaterial implements ArmorMaterial {
    public static final IceGodArmorMaterial INSTANCE = new IceGodArmorMaterial();
    private static final int[] BASE_DURABILITY = {13, 15, 16, 11};
    private static final int[] PROTECTION_VALUES = {4, 7, 9, 4};
    @Override
    public int getDurability(ArmorItem.Type type) {
        return BASE_DURABILITY[type.getEquipmentSlot().getEntitySlotId()] * 40;
    }
    @Override
    public int getProtection(ArmorItem.Type type) {
        return PROTECTION_VALUES[type.getEquipmentSlot().getEntitySlotId()];
    }
    @Override
    public int getEnchantability() {
        return 20;
    }
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND;
    }
    @Override
    public Ingredient getRepairIngredient() {

        return Ingredient.ofItems(Items.PACKED_ICE, Items.BLUE_ICE);
    }
    @Override
    public String getName() {

        return "ice_god";
    }
    @Override
    public float getToughness() {
        return 4.0F;
    }
    @Override
    public float getKnockbackResistance() {
        return 0.15F;
    }
}