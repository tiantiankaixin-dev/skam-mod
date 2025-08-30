// 文件: com.example.skam.potion.nbt/NbtInjector.java
package com.example.skam.potion.nbt;

import com.example.skam.item.core.CoreType;
import com.example.skam.util.SkamAttributeConfig;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem; // <-- 关键导入
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NbtInjector {

    // region UUIDs
    private static final UUID HEALTH_MODIFIER_BASE_UUID = UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");
    private static final UUID ARMOR_MODIFIER_BASE_UUID = UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B");
    private static final UUID DAMAGE_MODIFIER_BASE_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private static final UUID ATTACK_SPEED_MODIFIER_BASE_UUID = UUID.fromString("AF8B67CC-298B-4F16-8736-4FAC3B367C6A");
    private static final UUID MOVEMENT_SPEED_MODIFIER_BASE_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final UUID ARMOR_TOUGHNESS_MODIFIER_BASE_UUID = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_BASE_UUID = UUID.fromString("4B0E9980-6262-4AF8-98AF-49F151F51579");
    private static final UUID ATTACK_KNOCKBACK_MODIFIER_BASE_UUID = UUID.fromString("310E24A6-1413-4B07-9E40-4A37C4295F3A");
    private static final UUID LUCK_MODIFIER_BASE_UUID = UUID.fromString("03C31E81-5253-4496-B01C-93449A334E22");

    private static final List<UUID> OUR_BASE_UUIDS = List.of(
            HEALTH_MODIFIER_BASE_UUID, ARMOR_MODIFIER_BASE_UUID, DAMAGE_MODIFIER_BASE_UUID, ATTACK_SPEED_MODIFIER_BASE_UUID,
            MOVEMENT_SPEED_MODIFIER_BASE_UUID, ARMOR_TOUGHNESS_MODIFIER_BASE_UUID, KNOCKBACK_RESISTANCE_MODIFIER_BASE_UUID,
            ATTACK_KNOCKBACK_MODIFIER_BASE_UUID, LUCK_MODIFIER_BASE_UUID
    );
    // endregion

    public static CoreBonusData calculateTotalBonuses(Map<CoreType, Integer> appliedCores) {
        CoreBonusData data = new CoreBonusData();
        if (appliedCores == null || appliedCores.isEmpty()) return data;
        SkamAttributeConfig config = SkamAttributeConfig.get();
        for (Map.Entry<CoreType, Integer> entry : appliedCores.entrySet()) {
            CoreType type = entry.getKey();
            if (type == null) continue;
            int effectiveLevel = Math.min(entry.getValue(), type.getMaxLevel());
            if (effectiveLevel <= 0) continue;
            SkamAttributeConfig.CoreAttributes attributes = config.getAttributesFor(type);
            data.totalDamageBonus += (float) (attributes.damage_per_level * effectiveLevel);
            data.totalAttackSpeedBonus += (float) (attributes.attack_speed_per_level * effectiveLevel);
            data.totalAttackKnockbackBonus += (float) (attributes.attack_knockback_per_level * effectiveLevel);
            data.totalCritChanceBonus += (float) (attributes.crit_chance_per_level * effectiveLevel);
            data.totalHealthBonus += (float) (attributes.health_per_level * effectiveLevel);
            data.totalArmorBonus += (float) (attributes.armor_per_level * effectiveLevel);
            data.totalArmorToughnessBonus += (float) (attributes.armor_toughness_per_level * effectiveLevel);
            data.totalKnockbackResistanceBonus += (float) (attributes.knockback_resistance_per_level * effectiveLevel);
            data.totalMovementSpeedBonus += (float) (attributes.movement_speed_per_level * effectiveLevel);
            data.totalLuckBonus += (float) (attributes.luck_per_level * effectiveLevel);
            data.totalProjectileDamageBonus += (float) (attributes.projectile_damage_per_level * effectiveLevel);
            data.totalProjectileSpeedBonus += (float) (attributes.projectile_speed_per_level * effectiveLevel);
            data.totalCoreLevel += effectiveLevel;
        }
        return data;
    }

    public static void apply(ItemStack stack, CoreBonusData bonusData) {
        List<NbtCompound> foreignAttributes = preserveForeignAttributes(stack);
        final Item item = stack.getItem();

        // --- 核心修改点在这里 ---
        // 决定物品的属性应该应用到哪个槽位
        final EquipmentSlot slot;
        if (item instanceof ArmorItem armorItem) {
            slot = armorItem.getSlotType();
        } else if (item instanceof ShieldItem) {
            slot = EquipmentSlot.OFFHAND; // 盾牌使用副手槽
        } else {
            slot = EquipmentSlot.MAINHAND; // 其他物品默认使用主手槽
        }
        // --- 核心修改点结束 ---

        Multimap<EntityAttribute, EntityAttributeModifier> defaultModifiers = item.getAttributeModifiers(slot);

        // 清理旧的属性
        if (stack.hasNbt()) {
            stack.getNbt().remove("AttributeModifiers");
        }

        // 重新应用非本Mod的属性
        if (!foreignAttributes.isEmpty()) {
            NbtList attributeList = new NbtList();
            attributeList.addAll(foreignAttributes);
            stack.getOrCreateNbt().put("AttributeModifiers", attributeList);
        }

        // 应用物品默认属性
        for (Map.Entry<EntityAttribute, EntityAttributeModifier> entry : defaultModifiers.entries()) {
            stack.addAttributeModifier(entry.getKey(), entry.getValue(), slot);
        }

        // 应用核心提供的属性
        addCoreAttributeModifiers(stack, slot, bonusData);

        // 应用自定义NBT数据
        applyCustomNbtData(stack, bonusData);
    }

    private static void addCoreAttributeModifiers(ItemStack stack, EquipmentSlot slot, CoreBonusData bonusData) {
        if (bonusData.totalDamageBonus != 0)
            stack.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(getSlotSpecificUuid(DAMAGE_MODIFIER_BASE_UUID, slot), "Core Damage", bonusData.totalDamageBonus, EntityAttributeModifier.Operation.ADDITION), slot);
        if (bonusData.totalAttackSpeedBonus != 0)
            stack.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(getSlotSpecificUuid(ATTACK_SPEED_MODIFIER_BASE_UUID, slot), "Core Attack Speed", bonusData.totalAttackSpeedBonus, EntityAttributeModifier.Operation.ADDITION), slot);
        if (bonusData.totalAttackKnockbackBonus != 0)
            stack.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, new EntityAttributeModifier(getSlotSpecificUuid(ATTACK_KNOCKBACK_MODIFIER_BASE_UUID, slot), "Core Attack Knockback", bonusData.totalAttackKnockbackBonus, EntityAttributeModifier.Operation.ADDITION), slot);
        if (bonusData.totalHealthBonus != 0)
            stack.addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier(getSlotSpecificUuid(HEALTH_MODIFIER_BASE_UUID, slot), "Core Health", bonusData.totalHealthBonus, EntityAttributeModifier.Operation.ADDITION), slot);
        if (bonusData.totalArmorBonus != 0)
            stack.addAttributeModifier(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier(getSlotSpecificUuid(ARMOR_MODIFIER_BASE_UUID, slot), "Core Armor", bonusData.totalArmorBonus, EntityAttributeModifier.Operation.ADDITION), slot);
        if (bonusData.totalArmorToughnessBonus != 0)
            stack.addAttributeModifier(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, new EntityAttributeModifier(getSlotSpecificUuid(ARMOR_TOUGHNESS_MODIFIER_BASE_UUID, slot), "Core Armor Toughness", bonusData.totalArmorToughnessBonus, EntityAttributeModifier.Operation.ADDITION), slot);
        if (bonusData.totalKnockbackResistanceBonus != 0)
            stack.addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, new EntityAttributeModifier(getSlotSpecificUuid(KNOCKBACK_RESISTANCE_MODIFIER_BASE_UUID, slot), "Core Knockback Resistance", bonusData.totalKnockbackResistanceBonus, EntityAttributeModifier.Operation.ADDITION), slot);
        if (bonusData.totalMovementSpeedBonus != 0)
            stack.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(getSlotSpecificUuid(MOVEMENT_SPEED_MODIFIER_BASE_UUID, slot), "Core Movement Speed", bonusData.totalMovementSpeedBonus, EntityAttributeModifier.Operation.ADDITION), slot);
        if (bonusData.totalLuckBonus != 0)
            stack.addAttributeModifier(EntityAttributes.GENERIC_LUCK, new EntityAttributeModifier(getSlotSpecificUuid(LUCK_MODIFIER_BASE_UUID, slot), "Core Luck", bonusData.totalLuckBonus, EntityAttributeModifier.Operation.ADDITION), slot);
    }

    private static void applyCustomNbtData(ItemStack stack, CoreBonusData bonusData) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (bonusData.totalCritChanceBonus > 0) {
            nbt.putFloat("skam.crit_chance", bonusData.totalCritChanceBonus);
        } else {
            nbt.remove("skam.crit_chance");
        }

        if (bonusData.totalProjectileDamageBonus > 0) {
            nbt.putFloat("skam.projectile_damage", bonusData.totalProjectileDamageBonus);
        } else {
            nbt.remove("skam.projectile_damage");
        }

        if (bonusData.totalProjectileSpeedBonus > 0) {
            nbt.putFloat("skam.projectile_speed", bonusData.totalProjectileSpeedBonus);
        } else {
            nbt.remove("skam.projectile_speed");
        }
    }

    private static List<NbtCompound> preserveForeignAttributes(ItemStack stack) {
        List<NbtCompound> preserved = new ArrayList<>();
        if (!stack.hasNbt() || !stack.getNbt().contains("AttributeModifiers", NbtElement.LIST_TYPE)) {
            return preserved;
        }
        List<UUID> allOurPossibleUuids = Stream.of(EquipmentSlot.values())
                .flatMap(slot -> OUR_BASE_UUIDS.stream().map(base -> getSlotSpecificUuid(base, slot)))
                .collect(Collectors.toList());
        NbtList oldModifiers = stack.getNbt().getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
        for (NbtElement element : oldModifiers) {
            if (element instanceof NbtCompound attributeNbt) {
                if (attributeNbt.contains("UUID")) {
                    UUID uuid = attributeNbt.getUuid("UUID");
                    if (!allOurPossibleUuids.contains(uuid)) {
                        preserved.add(attributeNbt.copy());
                    }
                }
            }
        }
        return preserved;
    }

    private static UUID getSlotSpecificUuid(UUID baseUuid, EquipmentSlot slot) {
        return UUID.nameUUIDFromBytes((baseUuid.toString() + slot.getName()).getBytes(StandardCharsets.UTF_8));
    }
}

