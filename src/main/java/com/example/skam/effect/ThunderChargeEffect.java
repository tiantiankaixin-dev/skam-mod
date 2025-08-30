package com.example.skam.effect;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import java.util.UUID;

public class ThunderChargeEffect extends StatusEffect {
    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("7b1a364b-3e42-4217-a548-c146c373516f");
    public ThunderChargeEffect(StatusEffectCategory category, int color) {
        super(category, color);
       this.addAttributeModifier(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER_ID.toString(),
                100.0,
                EntityAttributeModifier.Operation.ADDITION
        );
    }
}