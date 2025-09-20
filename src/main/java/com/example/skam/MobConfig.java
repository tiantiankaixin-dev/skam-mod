package com.example.skam;

import com.example.skam.SkamMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MobConfig {
    public static boolean canRideTrident = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    // --- 修改开始: 更改文件路径 ---
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "skam/skam_treasure_mobs.json");
    // --- 修改结束 ---

    // --- 数据存储 ---
    private static final List<String> CONFIGURED_MOB_IDS = new ArrayList<>();
    private static List<EntityType<? extends HostileEntity>> resolvedMobTypesCache = null;

    // 新增：全局最低等级和特定怪物最低等级的映射
    private static int globalMinNaturalSpawnTier = 1;
    private static final Map<String, Integer> CONFIGURED_MOB_MIN_TIERS = new HashMap<>();

    private static final List<String> DEFAULT_MOB_IDS = Arrays.asList(
            "minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:creeper",
            "minecraft:enderman", "minecraft:witch", "minecraft:zombified_piglin",
            "minecraft:wither_skeleton", "minecraft:piglin", "minecraft:pillager"
    );

    public static void loadConfig() {
        // 重置所有配置
        CONFIGURED_MOB_IDS.clear();
        resolvedMobTypesCache = null;
        globalMinNaturalSpawnTier = 1;
        CONFIGURED_MOB_MIN_TIERS.clear();

        ConfigData configData;
        try {
            if (CONFIG_FILE.exists()) {
                FileReader reader = new FileReader(CONFIG_FILE);
                configData = GSON.fromJson(reader, ConfigData.class);
                if (configData == null) {
                    SkamMod.LOGGER.warn("配置文件为空，将重新创建。");
                    configData = createDefaultConfig();
                }
            } else {
                SkamMod.LOGGER.info("未找到配置文件，将创建默认配置文件: " + CONFIG_FILE.getName());
                configData = createDefaultConfig();
            }
        } catch (IOException e) {
            SkamMod.LOGGER.error("加载怪物配置文件失败！将使用默认配置。", e);
            configData = createDefaultConfig();
        }

        // 1. 加载可强化的怪物列表
        if (configData.mob_ids != null) {
            CONFIGURED_MOB_IDS.addAll(configData.mob_ids);
        } else {
            CONFIGURED_MOB_IDS.addAll(DEFAULT_MOB_IDS);
        }
        SkamMod.LOGGER.info("从配置文件加载了 " + CONFIGURED_MOB_IDS.size() + " 个可强化怪物ID。");


        // 2. 加载全局最低等级
        globalMinNaturalSpawnTier = Math.max(1, Math.min(3, configData.global_min_natural_spawn_tier));
        SkamMod.LOGGER.info("自然生成宝藏怪物的[全局]最低等级设置为: " + globalMinNaturalSpawnTier);

        // 3. 加载特定怪物的最低等级配置
        if (configData.mob_specific_min_tiers != null) {
            for (Map.Entry<String, Integer> entry : configData.mob_specific_min_tiers.entrySet()) {
                String mobId = entry.getKey();
                int tier = entry.getValue();
                // 验证ID和等级
                if (Identifier.tryParse(mobId) == null) {
                    SkamMod.LOGGER.warn("配置文件中发现无效的怪物ID: '" + mobId + "'，已跳过。");
                    continue;
                }
                int validatedTier = Math.max(1, Math.min(3, tier));
                CONFIGURED_MOB_MIN_TIERS.put(mobId, validatedTier);
                SkamMod.LOGGER.info("加载了特定怪物最低等级: " + mobId + " -> 等级 " + validatedTier);
            }
        }
    }

    private static ConfigData createDefaultConfig() {
        ConfigData defaultConfig = new ConfigData();
        defaultConfig.mob_ids = new ArrayList<>(DEFAULT_MOB_IDS);
        defaultConfig.global_min_natural_spawn_tier = 1;

        // 添加一个示例，方便用户理解如何配置
        defaultConfig.mob_specific_min_tiers = new HashMap<>();
        defaultConfig.mob_specific_min_tiers.put("minecraft:wither_skeleton", 3);
        defaultConfig.mob_specific_min_tiers.put("minecraft:pillager", 2);

        // --- 修改开始: 在写入文件前，确保父目录存在 ---
        try {
            File parentDir = CONFIG_FILE.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // 创建 config/skam 目录
            }
            // --- 修改结束 ---

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(defaultConfig, writer);
            }
        } catch (IOException e) {
            SkamMod.LOGGER.error("创建默认怪物配置文件失败！", e);
        }
        return defaultConfig;
    }

    // 新增：核心方法，获取指定怪物的最低等级
    public static int getMinTierForMob(HostileEntity mob) {
        EntityType<?> type = mob.getType();
        Identifier id = Registries.ENTITY_TYPE.getId(type);
        if (id == null) {
            return globalMinNaturalSpawnTier; // 如果获取不到ID，返回全局默认值
        }

        // 优先从特定配置中查找，如果找不到，则使用全局配置
        return CONFIGURED_MOB_MIN_TIERS.getOrDefault(id.toString(), globalMinNaturalSpawnTier);
    }

    // getValidMobTypes 方法保持不变
    public static List<EntityType<? extends HostileEntity>> getValidMobTypes() {
        if (resolvedMobTypesCache != null) {
            return resolvedMobTypesCache;
        }

        SkamMod.LOGGER.info("首次使用，开始运行时验证怪物列表...");
        List<EntityType<? extends HostileEntity>> resolvedList = new ArrayList<>();

        for (String id : CONFIGURED_MOB_IDS) {
            Identifier identifier = Identifier.tryParse(id);
            if (identifier == null) {
                SkamMod.LOGGER.warn("运行时验证：配置文件中的怪物ID格式无效: '" + id + "'");
                continue;
            }

            Optional<EntityType<?>> entityTypeOptional = Registries.ENTITY_TYPE.getOrEmpty(identifier);

            entityTypeOptional.ifPresentOrElse(
                    type -> {
                        if (type.getSpawnGroup() == SpawnGroup.MONSTER) {
                            resolvedList.add((EntityType<? extends HostileEntity>) type);
                        } else {
                            SkamMod.LOGGER.warn("运行时验证：实体 '" + id + "' 的生成组不是MONSTER (它是 " + type.getSpawnGroup().name() + ")，已跳过。");
                        }
                    },
                    () -> {
                        SkamMod.LOGGER.warn("运行时验证：实体ID '" + id + "' 在注册表中未找到。可能是ID错误或对应模组未加载。");
                    }
            );
        }
        resolvedMobTypesCache = resolvedList;
        SkamMod.LOGGER.info("怪物列表验证完成。成功加载 " + resolvedMobTypesCache.size() + " 种有效怪物。");
        return resolvedMobTypesCache;
    }

    // 修改内部类以包含新字段
    private static class ConfigData {
        List<String> mob_ids;
        int global_min_natural_spawn_tier;
        Map<String, Integer> mob_specific_min_tiers;
    }
}
