// 文件路径: src/main/java/com/example/skam/effect/ModEffects.java
package com.example.skam.effect;

import com.example.skam.SkamMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEffects {

    public static final StatusEffect POTION_TIME_AMPLIFICATION = new PotionTimeAmplificationEffect();
    // 2. 创建对应的药水 (30秒)
    public static final Potion POTION_TIME_AMPLIFICATION_POTION = new Potion(
            new StatusEffectInstance(POTION_TIME_AMPLIFICATION, 20 * 30, 0) // 20 ticks/sec * 30 sec, level 0 (I)
    );

    public static StatusEffect WRATH_OF_FIRE_GOD;
    public static StatusEffect PRESENCE_OF_ICE_GOD;
    public static StatusEffect FURY_OF_THUNDER_GOD;
    public static StatusEffect THUNDER_CHARGE;

    public static void registerEffects() {



        Registry.register(Registries.STATUS_EFFECT, new Identifier(SkamMod.MOD_ID, "potion_time_amplification"), POTION_TIME_AMPLIFICATION);
        // 注册药水
        Registry.register(Registries.POTION, new Identifier(SkamMod.MOD_ID, "potion_time_amplification_potion"), POTION_TIME_AMPLIFICATION_POTION);

        WRATH_OF_FIRE_GOD = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(SkamMod.MOD_ID, "wrath_of_fire_god"),
                new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF4500)); // 橙红色

        PRESENCE_OF_ICE_GOD = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(SkamMod.MOD_ID, "presence_of_ice_god"),
                new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0xADD8E6)); // 淡蓝色

        FURY_OF_THUNDER_GOD = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(SkamMod.MOD_ID, "fury_of_thunder_god"),
                new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFFF00)); // 黄色

        THUNDER_CHARGE = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(SkamMod.MOD_ID, "thunder_charge"),
                new ThunderChargeEffect(StatusEffectCategory.BENEFICIAL, 0x98D8FF)); // 代表闪电的淡蓝色
    }
}
