package com.example.skam.potion;

import com.example.skam.SkamMod;
import com.example.skam.effect.ModStatusEffects;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModPotions {
    public static Potion DEMON_KING_POTION;
    public static Potion STRONG_DEMON_KING_POTION;
    public static void registerPotions() {
        SkamMod.LOGGER.info("Registering Potions for " + SkamMod.MOD_ID);
        DEMON_KING_POTION = Registry.register(Registries.POTION,
                new Identifier(SkamMod.MOD_ID, "demon_king_potion"),
                new Potion(new StatusEffectInstance(ModStatusEffects.DEMON_KING, 3600, 0)));
        STRONG_DEMON_KING_POTION = Registry.register(Registries.POTION, new Identifier(SkamMod.MOD_ID, "strong_demon_king_potion"),
                new Potion("demon_king", new StatusEffectInstance(ModStatusEffects.DEMON_KING, 1800, 1)));
    }

    public static void registerPotionRecipes() {
        SkamMod.LOGGER.info("Registering Potion Recipes for " + SkamMod.MOD_ID);
      BrewingRecipeRegistry.registerPotionRecipe(
                Potions.AWKWARD,
                Items.NETHER_STAR,
                ModPotions.DEMON_KING_POTION
        );
        BrewingRecipeRegistry.registerPotionRecipe(
                ModPotions.DEMON_KING_POTION,
                Items.GLOWSTONE_DUST,
                ModPotions.STRONG_DEMON_KING_POTION
        );
    }
}
