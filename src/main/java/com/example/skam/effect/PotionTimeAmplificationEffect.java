package com.example.skam.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class PotionTimeAmplificationEffect extends StatusEffect {
    public PotionTimeAmplificationEffect() {
        // StatusEffectCategory.BENEFICIAL 表示这是个有益效果
        // 0xFFD700 是金色的十六进制颜色码
        super(StatusEffectCategory.BENEFICIAL, 0xFFD700);
    }
}
