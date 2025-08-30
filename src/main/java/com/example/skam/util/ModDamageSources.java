package com.example.skam.util;

import com.example.skam.SkamMod;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ModDamageSources {
   public static final RegistryKey<DamageType> SOUL_LINK_DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(SkamMod.MOD_ID, "soul_link"));
    public static DamageSource of(World world) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(SOUL_LINK_DAMAGE_TYPE));
    }
}
