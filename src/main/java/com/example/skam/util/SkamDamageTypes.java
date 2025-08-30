// 文件路径: src/main/java/com/example/skam/util/SkamDamageTypes.java (建议改个更有意义的名字)
package com.example.skam.util;

import com.example.skam.SkamMod; // 确保导入你的主类
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable; // <--- 使用这个 Nullable

public class SkamDamageTypes {

    // 1. 创建一个 RegistryKey 来引用我们的 JSON 文件
    //    "skam" 是你的 Mod ID, "skill_damage" 是你的 JSON 文件名
    public static final RegistryKey<DamageType> SKILL_DAMAGE_TYPE = RegistryKey.of(
            RegistryKeys.DAMAGE_TYPE,
            new Identifier(SkamMod.MOD_ID, "skill_damage")
    );

    /**
     * 一个方便的工具方法，用于在游戏世界中获取我们的技能伤害源。
     * @param world 当前世界
     * @param attacker 造成伤害的实体（通常是玩家）
     * @return 一个配置好的 DamageSource 实例
     */
    public static DamageSource of(World world, @Nullable Entity attacker) {
        // 从世界的注册表中获取我们定义好的 DamageType，然后用它创建一个 DamageSource
        return new DamageSource(
                world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getEntry(SKILL_DAMAGE_TYPE).get(),
                attacker
        );
    }

    // 你可以在这里添加一个空的 register() 方法，以保持代码整洁，
    // 尽管对于 DamageType 来说，只要 JSON 文件存在，它就会被自动加载。
    // 但有一个显式的调用可以提醒我们这个类的存在。
    public static void register() {
        SkamMod.LOGGER.info("Registering Skam Damage Types");
    }
}
