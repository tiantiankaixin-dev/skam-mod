package com.example.skam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SkamConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static SkamConfig INSTANCE;
    private static final String CONFIG_FILE_NAME = "skam_config.json";
    private static final Path CONFIG_PATH = Paths.get("config", "skam", CONFIG_FILE_NAME);

    public Weapons weapons = new Weapons();

    public static class Weapons {
        public NetheriteFireSwordConfig netherite_fire_sword = new NetheriteFireSwordConfig();
        public NetheriteIceSwordConfig netherite_ice_sword = new NetheriteIceSwordConfig();
        public NetheriteLightningSwordConfig netherite_lightning_sword = new NetheriteLightningSwordConfig();

        public DiamondFireSwordConfig diamond_fire_sword = new DiamondFireSwordConfig();
        public DiamondIceSwordConfig diamond_ice_sword = new DiamondIceSwordConfig();
        public DiamondLightningSwordConfig diamond_lightning_sword = new DiamondLightningSwordConfig();
    }

    // --- NETHERITE SWORD CONFIGS (Unchanged) ---
    public static class NetheriteFireSwordConfig {
        public FireDomainConfig fire_domain = new FireDomainConfig();
        public ExplosionBeamConfig explosion_beam = new ExplosionBeamConfig();

        public static class FireDomainConfig {
            public int damage = 30;
            public int cooldown_ticks = 1200;
            public int base_radius = 20;
            public int duration_ticks = 600;
            public int damage_interval_ticks = 20;
            public int particle_interval_ticks = 5;
            public int fire_duration_ticks = 100;
        }

        public static class ExplosionBeamConfig {
            public int damage = 100;
            public int cooldown_ticks = 20;
            public int range = 100;
            public double aoe_radius = 5.0;
            public int fire_duration_ticks = 100;
        }
    }

    public static class NetheriteIceSwordConfig {
        public IceDomainConfig ice_domain = new IceDomainConfig();
        public IceExplosionBeamConfig ice_explosion_beam = new IceExplosionBeamConfig();

        public static class IceDomainConfig {
            public int damage = 30;
            public int cooldown_ticks = 1200;
            public int base_radius = 20;
            public int duration_ticks = 600;
            public int damage_interval_ticks = 4;
            public int particle_interval_ticks = 5;
            public int snowball_particle_interval_ticks = 20;
            public int slowness_amplifier = 4;
            public int slowness_duration_ticks = 100;
        }

        public static class IceExplosionBeamConfig {
            public int damage = 100;
            public int cooldown_ticks = 20;
            public int range = 100;
            public double aoe_radius = 5.0;
            public int slowness_amplifier = 2;
            public int slowness_duration_ticks = 100;
        }
    }

    public static class NetheriteLightningSwordConfig {
        public LightningStormConfig lightning_storm = new LightningStormConfig();
        public LightningBeamConfig lightning_beam = new LightningBeamConfig();

        public static class LightningStormConfig {
            public int damage_per_strike = 200;
            public int cooldown_ticks = 1200;
            public int base_radius = 15;
            public int duration_ticks = 100;
            public int strike_interval_ticks = 2;
            public int particle_interval_ticks = 4;
        }

        public static class LightningBeamConfig {
            public int potion_damage = 100;
            public int lightning_damage = 10;
            public int cooldown_ticks = 20;
            public int range = 50;
            public double aoe_on_block_radius = 2.0;
        }
    }

    // --- DIAMOND SWORD CONFIGS (MODIFIED) ---

    public static class DiamondFireSwordConfig {
        public FireExplosionConfig fire_explosion = new FireExplosionConfig();
        public ExplosionBeamConfig explosion_beam = new ExplosionBeamConfig();
        public static class FireExplosionConfig {
            public int damage = 25;
            public int cooldown_ticks = 600; // 30 seconds
            public int base_radius = 8;
            public int fire_duration_ticks = 100; // 5 seconds
        }

        public static class ExplosionBeamConfig {
            public int damage = 50;
            public int cooldown_ticks = 20;
            public int range = 80;
            public double aoe_radius = 4.0;
            public int fire_duration_ticks = 80;
        }
    }

    public static class DiamondIceSwordConfig {
        public IceExplosionConfig ice_explosion = new IceExplosionConfig();
        public IceExplosionBeamConfig ice_explosion_beam = new IceExplosionBeamConfig();
        public static class IceExplosionConfig {
            public int damage = 20;
            public int cooldown_ticks = 600; // 30 seconds
            public int radius = 10;
            public int slowness_amplifier = 3; // Slowness IV
            public int slowness_duration_ticks = 120; // 6 seconds
        }

        public static class IceExplosionBeamConfig {
            public int damage = 50;
            public int cooldown_ticks = 20;
            public int range = 80;
            public double aoe_radius = 4.0;
            public int slowness_amplifier = 1; // Slowness II
            public int slowness_duration_ticks = 80;
        }
    }

    public static class DiamondLightningSwordConfig {
        public LightningExplosionConfig lightning_explosion = new LightningExplosionConfig();
        public LightningBeamConfig lightning_beam = new LightningBeamConfig();
        public static class  LightningExplosionConfig {

            public int damage_per_strike = 40;
            public int cooldown_ticks = 800;
            public int radius = 12;
            public int strike_count = 8;
        }

        public static class LightningBeamConfig {
            public int potion_damage = 50;
            public int lightning_damage = 70;
            public int cooldown_ticks = 20;
            public int range = 40;
            public double aoe_on_block_radius = 1.5;
        }
    }

    // --- CONFIG MANAGEMENT ---

    private SkamConfig() {}

    public static void loadConfig() {
        INSTANCE = new SkamConfig();

        if (Files.exists(CONFIG_PATH)) {
            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(CONFIG_PATH), StandardCharsets.UTF_8)) {
                INSTANCE = GSON.fromJson(reader, SkamConfig.class);
            } catch (IOException | JsonSyntaxException e) {
                System.err.println("Failed to read or parse config file " + CONFIG_PATH + ", using default values.");
            }
        } else {
            saveDefaultConfig();
        }

        if (INSTANCE.weapons == null) INSTANCE.weapons = new Weapons();
        if (INSTANCE.weapons.netherite_fire_sword == null) INSTANCE.weapons.netherite_fire_sword = new NetheriteFireSwordConfig();
        if (INSTANCE.weapons.netherite_fire_sword.fire_domain == null) INSTANCE.weapons.netherite_fire_sword.fire_domain = new NetheriteFireSwordConfig.FireDomainConfig();
        if (INSTANCE.weapons.netherite_fire_sword.explosion_beam == null) INSTANCE.weapons.netherite_fire_sword.explosion_beam = new NetheriteFireSwordConfig.ExplosionBeamConfig();

        if (INSTANCE.weapons.diamond_fire_sword == null) INSTANCE.weapons.diamond_fire_sword = new DiamondFireSwordConfig();
        if (INSTANCE.weapons.diamond_fire_sword.fire_explosion == null) INSTANCE.weapons.diamond_fire_sword.fire_explosion = new DiamondFireSwordConfig.FireExplosionConfig();
        if (INSTANCE.weapons.diamond_fire_sword.explosion_beam == null) INSTANCE.weapons.diamond_fire_sword.explosion_beam = new DiamondFireSwordConfig.ExplosionBeamConfig();

        if (INSTANCE.weapons.diamond_ice_sword == null) INSTANCE.weapons.diamond_ice_sword = new DiamondIceSwordConfig();
        if (INSTANCE.weapons.diamond_ice_sword.ice_explosion == null) INSTANCE.weapons.diamond_ice_sword.ice_explosion = new DiamondIceSwordConfig.IceExplosionConfig();
        if (INSTANCE.weapons.diamond_ice_sword.ice_explosion_beam == null) INSTANCE.weapons.diamond_ice_sword.ice_explosion_beam = new DiamondIceSwordConfig.IceExplosionBeamConfig();

        if (INSTANCE.weapons.diamond_lightning_sword == null) INSTANCE.weapons.diamond_lightning_sword = new DiamondLightningSwordConfig();
        if (INSTANCE.weapons.diamond_lightning_sword.lightning_explosion == null) INSTANCE.weapons.diamond_lightning_sword.lightning_explosion = new DiamondLightningSwordConfig.LightningExplosionConfig();
        if (INSTANCE.weapons.diamond_lightning_sword.lightning_beam == null) INSTANCE.weapons.diamond_lightning_sword.lightning_beam = new DiamondLightningSwordConfig.LightningBeamConfig();

        saveConfig();
    }

    public static void saveConfig() {
        if (INSTANCE != null) {
            try {
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.write(CONFIG_PATH, GSON.toJson(INSTANCE).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
            }
        }
    }

    private static void saveDefaultConfig() {
        SkamConfig defaultConfig = new SkamConfig();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.write(CONFIG_PATH, GSON.toJson(defaultConfig).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
        }
    }

    public static SkamConfig getInstance() {
        if (INSTANCE == null) {
            loadConfig();
        }
        return INSTANCE;
    }
}
