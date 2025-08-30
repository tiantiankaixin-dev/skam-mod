// 文件: com.example.skam.util/SkamAttributeConfig.java
package com.example.skam.util;

import com.example.skam.Skam;
import com.example.skam.item.core.CoreType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

public class SkamAttributeConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("skam").resolve("skamattribute.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static SkamAttributeConfig INSTANCE;

    public Map<CoreType, CoreAttributes> attributes = new EnumMap<>(CoreType.class);

    public static class CoreAttributes {
        // --- 修改开始: 添加 max_level 属性 ---
        public int max_level = 5; // 默认等级上限为 5
        // --- 修改结束 ---

        // 攻击属性
        public double damage_per_level = 0.0;
        public double attack_speed_per_level = 0.0;
        public double attack_knockback_per_level = 0.0;
        public double crit_chance_per_level = 0.0;

        // 防御属性
        public double health_per_level = 0.0;
        public double armor_per_level = 0.0;
        public double armor_toughness_per_level = 0.0;
        public double knockback_resistance_per_level = 0.0;

        // 通用/移动属性
        public double movement_speed_per_level = 0.0;
        public double luck_per_level = 0.0;

        //弹射物伤害
        public double projectile_damage_per_level = 0.0;
        public double projectile_speed_per_level = 0.0;

        //吸血核心
        public double vampire_chance_per_level = 0.0;
        public double vampire_min_heal_per_level = 0.0;
        public double vampire_max_heal_per_level = 0.0;

        //伤害倍率
        public double damage_multiplier_per_level = 0.0;
    }

    // get() 和 load() 方法保持不变
    public static SkamAttributeConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(reader, SkamAttributeConfig.class);
                Skam.LOGGER.info("Successfully loaded skamattribute.json config.");
                // 确保所有核心类型都有一个条目，即使配置文件中缺少
                for (CoreType type : CoreType.values()) {
                    INSTANCE.attributes.computeIfAbsent(type, k -> new CoreAttributes());
                }
            } catch (Exception e) {
                Skam.LOGGER.error("Failed to read skamattribute.json, using default values.", e);
                createDefaultConfig();
            }
        } else {
            Skam.LOGGER.warn("skamattribute.json not found, creating a new one with default values.");
            createDefaultConfig();
        }
    }

    private static void createDefaultConfig() {
        INSTANCE = new SkamAttributeConfig();

        // --- 修改开始: 为每个核心设置默认属性和等级上限 ---
        CoreAttributes fireDefaults = new CoreAttributes();
        fireDefaults.max_level = 666;
        fireDefaults.damage_per_level = 1.0;
        fireDefaults.attack_knockback_per_level = 0.1;
        INSTANCE.attributes.put(CoreType.FIRE, fireDefaults);

        CoreAttributes lightningDefaults = new CoreAttributes();
        lightningDefaults.max_level = 666666;
        lightningDefaults.attack_speed_per_level = 0.1;
        lightningDefaults.movement_speed_per_level = 0.005;
        INSTANCE.attributes.put(CoreType.LIGHTNING, lightningDefaults);

        CoreAttributes iceDefaults = new CoreAttributes();
        iceDefaults.max_level = 666666;
        iceDefaults.health_per_level = 1.0;
        iceDefaults.armor_per_level = 0.5;
        iceDefaults.knockback_resistance_per_level = 0.05;
        INSTANCE.attributes.put(CoreType.ICE, iceDefaults);

        CoreAttributes archerDefaults = new CoreAttributes();
        archerDefaults.max_level = 666666;
        archerDefaults.projectile_damage_per_level = 0.25;
        archerDefaults.projectile_speed_per_level = 0.02;
        INSTANCE.attributes.put(CoreType.ARCHER, archerDefaults);

        // 纯化核心没有实际效果，但需要一个无上限的等级
        CoreAttributes purificationDefaults = new CoreAttributes();
        purificationDefaults.max_level = Integer.MAX_VALUE; // 实际上无上限
        INSTANCE.attributes.put(CoreType.PURIFICATION, purificationDefaults);

        CoreAttributes vampireDefaults = new CoreAttributes();
        vampireDefaults.max_level = 5;
        vampireDefaults.vampire_chance_per_level = 0.10;
        vampireDefaults.vampire_min_heal_per_level = 1.0;
        vampireDefaults.vampire_max_heal_per_level = 2.0;
        INSTANCE.attributes.put(CoreType.VAMPIRE, vampireDefaults);

        CoreAttributes strengthDefaults = new CoreAttributes();
        strengthDefaults.max_level = 5;
        strengthDefaults.damage_multiplier_per_level = 0.1;
        INSTANCE.attributes.put(CoreType.STRENGTH, strengthDefaults);
        // --- 修改结束 ---

        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            Skam.LOGGER.error("Failed to write default skamattribute.json config.", e);
        }
    }

    public CoreAttributes getAttributesFor(CoreType type) {
        // 使用 computeIfAbsent 确保即使在旧配置文件中也不会返回 null
        return attributes.computeIfAbsent(type, k -> new CoreAttributes());
    }
}