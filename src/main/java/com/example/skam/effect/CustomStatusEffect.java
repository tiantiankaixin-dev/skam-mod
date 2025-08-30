// 文件路径: src/main/java/com/example/skam/effect/CustomStatusEffect.java
package com.example.skam.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class CustomStatusEffect extends StatusEffect {
    public CustomStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }
}
