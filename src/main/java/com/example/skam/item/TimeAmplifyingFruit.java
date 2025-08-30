package com.example.skam.item;

import com.example.skam.effect.ModEffects; // 导入你存放效果的类
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;

public class TimeAmplifyingFruit extends Item {

    // 定义食物属性
    private static final FoodComponent FOOD_COMPONENT = new FoodComponent.Builder()
            .hunger(4) // 恢复4点饥饿值 (2个鸡腿)
            .saturationModifier(0.5f) // 饱和度系数
            // 关键部分：添加状态效果
            // 格式：(效果, 持续时间(tick), 等级)
            // 20 ticks = 1 秒, 所以 20 * 60 = 1分钟
            .statusEffect(new StatusEffectInstance( ModEffects.POTION_TIME_AMPLIFICATION, 20 * 60, 0), 1.0f) // 1.0f 表示100%概率触发
            .alwaysEdible() // 即使饥饿值是满的也可以吃
            .build();

    public TimeAmplifyingFruit() {
        super(new FabricItemSettings().food(FOOD_COMPONENT));
    }
}
