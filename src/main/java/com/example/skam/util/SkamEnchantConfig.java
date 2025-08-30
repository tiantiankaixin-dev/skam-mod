// 文件: com.example.skam.util/SkamEnchantConfig.java
package com.example.skam.util;

import com.example.skam.Skam;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class SkamEnchantConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("skam").resolve("skamenchant.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static SkamEnchantConfig INSTANCE;

    // 为每个附魔创建独立的配置类
    public TimeLagThornEnchant time_lag_thorn = new TimeLagThornEnchant();
    public CursedTridentEnchant cursed_trident = new CursedTridentEnchant();
    public FallingSwordEnchant falling_sword = new FallingSwordEnchant();
    public ThunderCallerEnchant thunder_caller = new ThunderCallerEnchant();
    public ExplosiveImpactEnchant explosive_impact = new ExplosiveImpactEnchant();

    public static class TimeLagThornEnchant {
        public int stasis_timeout_ticks = 30;
        public int initial_fly_ticks = 5;
    }

    public static class CursedTridentEnchant {
        public float scale_per_level = 0.75f;
    }

    public static class FallingSwordEnchant {
        public float damage_per_level = 20.0f;
        public float explosion_power = 1.4f;
        public double damage_radius = 4.0;
        public int despawn_ticks = 400; // 20 seconds
    }

    public static class ThunderCallerEnchant {
        public float damage = 10.0f;
        public float damage_radius = 3.0f;
    }

    public static class ExplosiveImpactEnchant {
        public float damage_per_level = 10.0f;
        public float base_radius = 2.0f;
        public float radius_per_level = 0.5f;
        public int base_shockwave_particles = 70;
        public int shockwave_particles_per_level = 20;
        public int base_swirling_particles = 40;
        public int swirling_particles_per_level = 15;
    }

    public static SkamEnchantConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(reader, SkamEnchantConfig.class);
                if (INSTANCE == null) { // 如果文件是空的或无效的
                    throw new IllegalStateException("Config file is empty or malformed.");
                }
                Skam.LOGGER.info("Successfully loaded skamenchant.json config.");
            } catch (Exception e) {
                Skam.LOGGER.error("Failed to read skamenchant.json, using default values.", e);
                createDefaultConfig();
            }
        } else {
            Skam.LOGGER.warn("skamenchant.json not found, creating a new one with default values.");
            createDefaultConfig();
        }
    }

    private static void createDefaultConfig() {
        INSTANCE = new SkamEnchantConfig();
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            Skam.LOGGER.error("Failed to write default skamenchant.json config.", e);
        }
    }
}
