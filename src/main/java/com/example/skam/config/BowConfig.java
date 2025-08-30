package com.example.skam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BowConfig {

    private static BowConfig INSTANCE;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // --- 修改开始: 更改文件路径 ---
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "skam/bowconfig.json");
    // --- 修改结束 ---

    public BlackHole black_hole = new BlackHole();
    public FinalExplosion final_explosion = new FinalExplosion();
    public FireDomain fire_domain = new FireDomain();
    public LegendBow legend_bow = new LegendBow();

    public static class BlackHole {
        public int lifetime_ticks = 106; // 5.3 seconds
        public double attraction_radius = 5.0;
        public double attraction_strength = 0.3;
    }

    public static class FinalExplosion {
        public float explosion_power = 5.0f; // For sound/particles, doesn't break blocks
        public float magic_damage = 600.0f;
        public double damage_radius = 5.0; // The radius for the initial magic damage
    }

    public static class FireDomain {
        public int duration_ticks = 200; // 10 seconds
        public float radius = 5.0f;
        public float damage_amount = 2.0f; // Damage per tick
        public int damage_interval_ticks = 10; // e.g., damage every 0.5 seconds
        public int fire_duration_seconds = 4; // How long entities are set on fire
    }

    public static class LegendBow {
        public int special_attack_cooldown_ticks = 600; // 30 seconds * 20 ticks/sec
    }

    public static BowConfig getInstance() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, BowConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new BowConfig();
                    // 如果文件损坏或为空，创建一个新的并保存
                    save();
                }
            } catch (IOException e) {
                e.printStackTrace();
                INSTANCE = new BowConfig();
            }
        } else {
            INSTANCE = new BowConfig();
            save();
        }
    }

    public static void save() {
        // --- 修改开始: 在保存文件前，确保父目录存在 ---
        try {
            File parentDir = CONFIG_FILE.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // 创建 config/skam 目录
            }
            // --- 修改结束 ---

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            System.err.println("Could not save skam/bowconfig.json.");
            e.printStackTrace();
        }
    }
}
