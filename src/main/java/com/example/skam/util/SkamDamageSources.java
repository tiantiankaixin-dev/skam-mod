// 文件路径: src/main/java/com/example/skam/util/SkamDamageSources.java
package com.example.skam.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SkamDamageSources {

    // 1. 定义我们的自定义伤害类型的注册密钥
    public static final RegistryKey<DamageType> STRENGTH_CORE_BONUS = RegistryKey.of(
            RegistryKeys.DAMAGE_TYPE,
            new Identifier("skam", "strength_core_bonus")
    );

    // 2. 创建一个辅助方法来生成伤害源实例
    // 这个方法会从世界中获取注册的伤害类型，并附加上攻击者信息
    public static DamageSource of(World world, RegistryKey<DamageType> key, @Nullable Entity attacker) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key), attacker);
    }
}
