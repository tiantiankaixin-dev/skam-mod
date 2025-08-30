package com.example.skam.enchantment;

import com.example.skam.Skam;
import com.example.skam.SkamMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEnchantments {


    public static final Enchantment DOMAIN_EXPANSION = register("domain_expansion",
            new DomainExpansionEnchantment());

    // --- 所有护符附魔 ---
    public static final Enchantment POWER_CHARM = register("power_charm", new PowerCharmEnchantment());
    public static final Enchantment SPEED_CHARM = register("speed_charm", new SpeedCharmEnchantment());
    public static final Enchantment NIGHT_VISION_CHARM = register("night_vision_charm", new NightVisionCharmEnchantment());
    public static final Enchantment HASTE_CHARM = register("haste_charm", new HasteCharmEnchantment());
    public static final Enchantment WATER_BREATHING_CHARM = register("water_breathing_charm", new WaterBreathingCharmEnchantment());
    public static final Enchantment DOLPHINS_GRACE_CHARM = register("dolphins_grace_charm", new DolphinsGraceCharmEnchantment());
    public static final Enchantment REGENERATION_CHARM = register("regeneration_charm", new RegenerationCharmEnchantment());
    public static final Enchantment HEALTH_BOOST_CHARM = register("health_boost_charm", new HealthBoostCharmEnchantment());
    public static final Enchantment ABSORPTION_CHARM = register("absorption_charm",new AbsorptionCharmEnchantment());
    public static final Enchantment TIME_LAG_THORN = register("time_lag_thorn",new TimeLagThornEnchantment());
    public static final Enchantment EXPLOSIVE_IMPACT = register("explosive_impact", new ExplosiveImpactEnchantment(Enchantment.Rarity.VERY_RARE, EquipmentSlot.MAINHAND));
    public static final Enchantment THUNDER_CALLER = register("thunder_caller", new ThunderCallerEnchantment());
    public static final Enchantment CURSED_TRIDENT = new CursedTridentEnchantment();
    public static final Enchantment SKY_STRIKE = Registry.register(Registries.ENCHANTMENT, new Identifier(SkamMod.MOD_ID, "sky_strike"), new SkyStrikeEnchantment());
    public static final Enchantment AIR_JUMPER = new AirJumperEnchantment();


    private static Enchantment register(String name, Enchantment enchantment) {
        return Registry.register(Registries.ENCHANTMENT, new Identifier(SkamMod.MOD_ID, name), enchantment);
    }


    public static void registerEnchantments() {
        Registry.register(Registries.ENCHANTMENT, new Identifier(Skam.MOD_ID, "air_jumper"), AIR_JUMPER);
        Registry.register(Registries.ENCHANTMENT, new Identifier("skam", "cursed_trident"), CURSED_TRIDENT);
        Enchantment registered = Registries.ENCHANTMENT.get(new Identifier("skam", "cursed_trident"));
        SkamMod.LOGGER.info("为 " + SkamMod.MOD_ID + " 注册所有附魔...");

    }
}
