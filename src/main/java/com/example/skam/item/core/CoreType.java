// 文件: com.example.skam.item.core/CoreType.java
package com.example.skam.item.core;

import com.example.skam.util.SkamAttributeConfig;
import net.minecraft.util.Formatting;

public enum CoreType {


    // --- 修改开始: 移除构造函数中的 maxLevel 参数 ---
    FIRE("tooltip.skam.core.name.fire", '\uE001', Formatting.RED),
    LIGHTNING("tooltip.skam.core.name.lightning", '\uE002', Formatting.YELLOW),
    ICE("tooltip.skam.core.name.ice", '\uE003', Formatting.AQUA),
    ARCHER("tooltip.skam.core.name.archer", '\uE004', Formatting.GREEN),
    PURIFICATION("tooltip.skam.core.name.purification", '\uE005', Formatting.LIGHT_PURPLE),
    VAMPIRE("tooltip.skam.core.name.vampire", '\uE006', Formatting.DARK_RED),
    STRENGTH("tooltip.skam.core.name.strength", '\uE007', Formatting.LIGHT_PURPLE);
    // --- 修改结束 ---

    private final String nameTranslationKey;
    private final char iconChar;
    private final Formatting color;



    CoreType(String nameTranslationKey, char iconChar, Formatting color) {
        this.nameTranslationKey = nameTranslationKey;
        this.iconChar = iconChar;
        this.color = color;
    }

    public String getNameTranslationKey() {
        return nameTranslationKey;
    }

    public char getIconChar() {
        return iconChar;
    }

    public Formatting getColor() {
        return color;
    }

    public int getMaxLevel() {
        return SkamAttributeConfig.get().getAttributesFor(this).max_level;
    }
}
