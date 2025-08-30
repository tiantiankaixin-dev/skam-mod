package com.example.skam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BossConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // --- 修改开始: 更改文件路径 ---
    // 将路径从根目录改为 'skam' 子目录
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "skam/skam_boss.json");
    // --- 修改结束 ---

    private static final Logger LOGGER = LoggerFactory.getLogger("SkamMod-Config");
    public static BossSettings bossSettings; // 保持您原有的 public static 字段，以修复编译错误

    // ... (所有的内部类 BossSettings, FlameEmperorSettings 等保持原样，此处省略以保持简洁) ...
    public static class BossSettings {
        public FlameEmperorSettings flameEmperor = new FlameEmperorSettings();
        public IceEmperorSettings iceEmperor = new IceEmperorSettings();
        public LightningEmperorSettings lightningEmperor = new LightningEmperorSettings();

        public static class FlameEmperorSettings {
            public double health = 5000.0;
            public double attackDamage = 25.0;
            public double armor = 20.0;
            public double followRange = 40.0;
            public double movementSpeed = 0.3;

            public int weaponRightClickInterval = 200; // 右键间隔（tick）

            public FlameAuraSettings flameAura = new FlameAuraSettings();

            public VexSummonSettings vexSummon = new VexSummonSettings();

            public MeteorStrikeSettings meteorStrike = new MeteorStrikeSettings();

            public EruptionSettings eruption = new EruptionSettings();

            public FlameMagicCircleSettings flameMagicCircle = new FlameMagicCircleSettings();


            public static class FlameAuraSettings {
                public double triggerHealthThreshold = 3000.0; // 触发血量阈值
                public float dragonBreathDamage = 2.0f; // 龙息伤害
                public float fireDamage = 10.0f; // 火焰伤害
                public double auraRadius = 10.0; // 领域半径
                public int damageInterval = 20; // 伤害间隔（tick，20tick=1秒）
            }

            public static class VexSummonSettings {
                public double triggerHealthThreshold = 4000.0; // 触发血量阈值
                public int vexCount = 10; // 召唤恼鬼数量
                public double summonRadius = 5.0; // 召唤半径
                public double vexHealth = 20.0; // 恼鬼血量
                public double vexAttackDamage = 15.0; // 恼鬼攻击伤害
                public int bossStrengthLevel = 2; // boss力量等级（0-2对应1-3级）
                public int bossSpeedLevel = 1; // boss速度等级（0-1对应1-2级）
            }

            public static class MeteorStrikeSettings {
                public double triggerHealthThreshold = 2500.0; // 触发血量阈值
                public int cooldown = 300; // 技能冷却时间 (tick)
                public int meteorCount = 15; // 陨石数量
                public double targetRadius = 15.0; // 陨石散布半径
                public float explosionPower = 3.0f; // 爆炸威力
            }

            public static class EruptionSettings {
                public int cooldown = 240; // 技能冷却时间 (tick)
                public float damage = 15.0f; // 喷发伤害
                public double knockupStrength = 1.2; // 向上击飞力度
                public int fireTicks = 80; // 燃烧持续时间 (tick)
                public int warningTicks = 40; // 预警时间 (tick)
                public double eruptionRadius = 2.0; // 喷发半径
            }

            public static class FlameMagicCircleSettings {
                public double additionalAttackDamage = 20.0; // 额外攻击伤害
                public double circleRadius = 1.5; // 魔法阵半径
            }
        }

        public static class IceEmperorSettings {
            public double health = 5000.0;
            public double attackDamage = 25.0;
            public double armor = 20.0;
            public double followRange = 40.0;
            public double movementSpeed = 0.3;

            // 武器使用配置
            public int weaponRightClickInterval = 100; // 右键间隔（tick）

            // 冰霜领域配置
            public IceAuraSettings iceAura = new IceAuraSettings();

            // 召唤配置
            public SummonSettings summon = new SummonSettings();

            // 新增：冰霜魔法阵配置
            public FrostMagicCircleSettings frostMagicCircle = new FrostMagicCircleSettings();

            // 新增：冰锥术技能配置
            public IceShardsSettings iceShards = new IceShardsSettings();

            // 新增：冰霜牢笼技能配置
            public GlacialPrisonSettings glacialPrison = new GlacialPrisonSettings();

            // 新增：暴风雪技能配置
            public BlizzardSettings blizzard = new BlizzardSettings();

            public static class IceAuraSettings {
                public double triggerHealthThreshold = 4000.0; // 触发血量阈值
                public float freezeDamage = 2.0f; // 冰冻伤害
                public float iceDamage = 10.0f; // 冰霜伤害
                public double auraRadius = 10.0; // 领域半径
                public int damageInterval = 20; // 伤害间隔（tick，20tick=1秒）
                public int slownessDuration = 100; // 缓慢效果持续时间
                public int slownessLevel = 2; // 缓慢等级
            }

            public static class SummonSettings {
                public double triggerHealthThreshold = 2500.0; // 触发血量阈值
                public int summonCount = 8; // 召唤数量
                public double summonRadius = 5.0; // 召唤半径
                public double summonHealth = 25.0; // 召唤物血量
                public double summonAttackDamage = 12.0; // 召唤物攻击伤害
                public int bossResistanceLevel = 1; // boss抗性等级
                public int bossSlownessResistanceLevel = 3; // boss缓慢抗性等级
            }

            // 新增：冰霜魔法阵配置
            public static class FrostMagicCircleSettings {
                public double additionalArmor = 10.0; // 额外护甲
                public double circleRadius = 1.5; // 魔法阵半径
            }

            // 新增：冰锥术技能配置
            public static class IceShardsSettings {
                public int cooldown = 100; // 技能冷却时间 (5秒)
                public float damage = 8.0f; // 每个冰锥的伤害
                public int shardCount = 5; // 冰锥数量
                public int fireRate = 3; // 冰锥发射间隔 (tick)
            }

            // 新增：冰霜牢笼技能配置
            public static class GlacialPrisonSettings {
                public int cooldown = 1000; // 技能冷却时间 (20秒)
                public int prisonDuration = 100; // 牢笼持续时间 (5秒)
                public int slownessLevel = 3; // 施加的缓慢等级
                public int slownessDuration = 100; // 缓慢效果持续时间
                public int prisonRadius = 2; // 牢笼半径
            }

            // 新增：暴风雪技能配置
            public static class BlizzardSettings {
                public double triggerHealthThreshold = 1000.0; // 触发血量阈值
                public int cooldown = 600; // 技能冷却时间 (30秒)
                public int duration = 200; // 暴风雪持续时间 (10秒)
                public double radius = 12.0; // 暴风雪半径
                public float damagePerTick = 1.0f; // 每次伤害值
                public int damageInterval = 10; // 伤害间隔 (tick)
                public int slownessLevel = 1; // 缓慢效果等级
            }
        }

        public static class LightningEmperorSettings {
            public double health = 5000.0;
            public double attackDamage = 25.0;
            public double armor = 20.0;
            public double followRange = 40.0;
            public double movementSpeed = 0.3;

            // 远古守卫者召唤配置 (保留并重构)
            public GuardianSummonSettings guardianSummon = new GuardianSummonSettings();

            // 新增：雷电魔法阵配置
            public LightningMagicCircleSettings lightningMagicCircle = new LightningMagicCircleSettings();

            // 新增：雷电链技能配置
            public ChainLightningSettings chainLightning = new ChainLightningSettings();

            // 新增：雷霆跳跃技能配置
            public ThunderousLeapSettings thunderousLeap = new ThunderousLeapSettings();

            // 新增：静电场技能配置
            public StaticFieldSettings staticField = new StaticFieldSettings();

            public static class GuardianSummonSettings {
                public double triggerHealthThreshold = 4000.0; // 触发血量阈值
                public int guardianCount = 10; // 召唤远古守卫者数量
                public double summonRadius = 8.0; // 召唤半径
                public double guardianHealth = 100.0; // 守卫者血量
                public double guardianDamage = 10.0; // 守卫者伤害
            }

            public static class LightningMagicCircleSettings {
                public double additionalMovementSpeed = 0.05; // 额外移动速度
                public double circleRadius = 1.5; // 魔法阵半径
            }

            public static class ChainLightningSettings {
                public int cooldown = 120; // 技能冷却 (6秒)
                public float damage = 15.0f; // 闪电伤害
                public int maxJumps = 3; // 最大跳跃次数
                public double jumpRange = 10.0; // 跳跃范围
            }

            public static class ThunderousLeapSettings {
                public int cooldown = 300; // 技能冷却 (15秒)
                public float damage = 20.0f; // 落地伤害
                public double knockbackStrength = 1.5; // 击退力度
                public double leapHeight = 1.2; // 跳跃高度
                public double leapMinRange = 5.0; // 最短跳跃距离
            }

            public static class StaticFieldSettings {
                public double triggerHealthThreshold = 2500.0; // 触发血量阈值
                public int duration = 300; // 持续时间 (15秒)
                public double radius = 8.0; // 领域半径
                public float damagePerTick = 2.0f; // 每次伤害
                public int damageInterval = 20; // 伤害间隔 (1秒)
                public int weaknessLevel = 1; // 虚弱等级
            }
        }
    }


    public static void loadConfig()
    {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                bossSettings = GSON.fromJson(reader, BossSettings.class);
                if (bossSettings == null) {
                    LOGGER.warn("Config file 'skam_boss.json' is empty or corrupted. Creating a new one with default values.");
                    bossSettings = new BossSettings();
                    saveConfig();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to read config file. Using default values.", e);
                bossSettings = new BossSettings();
            }
        } else {
            LOGGER.info("Config file 'skam_boss.json' not found. Creating a new one with default values.");
            bossSettings = new BossSettings();
            saveConfig();
        }
    }

    public static void saveConfig()
    {
        if (bossSettings == null) {
            LOGGER.error("Attempted to save config, but settings object is null. Aborting.");
            return;
        }

        // --- 修改开始: 在保存文件前，确保父目录存在 ---
        try {
            File parentDir = CONFIG_FILE.getParentFile();
            if (!parentDir.exists()) {
                // 如果 config/skam 目录不存在，则创建它
                parentDir.mkdirs();
            }
            // --- 修改结束 ---

            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(bossSettings, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config file.", e);
        }
    }
}
