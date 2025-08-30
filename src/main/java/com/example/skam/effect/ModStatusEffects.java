package com.example.skam.effect;

import com.example.skam.SkamMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModStatusEffects {
    // 1. 只声明变量，不要在这里初始化！
    //    我们把它设为 public static，这样其他类也能访问它。
    public static StatusEffect DEMON_KING;

    // 2. 在注册方法中同时进行“创建”和“注册”
    public static void registerEffects() {
        SkamMod.LOGGER.info("为 " + SkamMod.MOD_ID + " 注册状态效果");

        // 在这里，我们将新创建的 DemonKingEffect 对象直接传入注册方法。
        // Registry.register 会返回注册好的效果实例，我们再把它赋值给静态变量 DEMON_KING。
        // 这样就保证了创建和注册发生在正确的时机。
        DEMON_KING = Registry.register(Registries.STATUS_EFFECT,
                new Identifier(SkamMod.MOD_ID, "demon_king"),
                new DemonKingEffect(StatusEffectCategory.BENEFICIAL, 0x980000));
    }
}
