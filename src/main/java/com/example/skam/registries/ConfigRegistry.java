package com.example.skam.registries;

import com.example.skam.config.BossConfig;
import com.example.skam.config.BowConfig;
import com.example.skam.config.SkamConfig;
import com.example.skam.MobConfig;
import com.example.skam.util.SkamAttributeConfig;

public class ConfigRegistry {

    public static void registerConfigs() {
        MobConfig.loadConfig();
        BossConfig.loadConfig();
        SkamConfig.loadConfig();
        BowConfig.load();
        SkamAttributeConfig.load();
    }
}
