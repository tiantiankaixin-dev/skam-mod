package com.example.skam.item.armor;

import com.example.skam.effect.ModEffects;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class FireGodArmorItem extends ArmorItem {

    private static final Logger LOGGER = LoggerFactory.getLogger("FireGodArmor");

    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private static final String ATTACK_DAMAGE_MODIFIER_NAME = "Fire God's Wrath";

    public FireGodArmorItem(ArmorMaterial material, Type type, Settings settings) {
        super(material, type, settings.fireproof());
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        if (!(entity instanceof PlayerEntity player)) {
            super.inventoryTick(stack, world, entity, slot, selected);
            return;
        }


        if (player.getInventory().getArmorStack(2) != stack) {
            super.inventoryTick(stack, world, entity, slot, selected);
            return;
        }

        boolean hasFullSet = hasFullArmorSet(player);


        if (!world.isClient()) {
            EntityAttributeInstance attributeInstance = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (attributeInstance == null) {
                return;
            }


            if (hasFullSet) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 220, 0, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 220, 0, false, false, true));
            }


            boolean shouldHaveAttackBonus = hasFullSet && player.isOnFire();
            boolean hasModifier = attributeInstance.getModifier(ATTACK_DAMAGE_MODIFIER_ID) != null;

            if (shouldHaveAttackBonus) {
                player.addStatusEffect(new StatusEffectInstance(ModEffects.WRATH_OF_FIRE_GOD, 5, 0, false, false, true));
                if (!hasModifier) {
                    attributeInstance.addTemporaryModifier(new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, ATTACK_DAMAGE_MODIFIER_NAME, 50.0, EntityAttributeModifier.Operation.ADDITION));
                }
            } else {
                if (hasModifier) {
                    attributeInstance.removeModifier(ATTACK_DAMAGE_MODIFIER_ID);
                }
            }
        }


        if (world.isClient()) {
            if (hasFullSet) {
                if (world.getTime() % 4 == 0) {
                    spawnFireRing(player, world);
                }

               Vec3d playerVelocity = player.getVelocity();
               boolean isMoving = playerVelocity.x * playerVelocity.x + playerVelocity.z * playerVelocity.z > 0.001;
                if (isMoving && player.isOnGround()) {
                    spawnTrailParticles(player, world);
                }
            }
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    private void spawnFireRing(PlayerEntity player, World world) {
        double radius = 0.8;
        int particleCount = 6;
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        for (int i = 0; i < particleCount; i++) {
           double angle = (2 * Math.PI / particleCount) * i + (world.getTime() / 20.0);
            double xOffset = radius * Math.cos(angle);
            double zOffset = radius * Math.sin(angle);
            world.addParticle(ParticleTypes.FLAME, playerX + xOffset, playerY + 0.1, playerZ + zOffset, 0, 0, 0);
        }
    }

    private void spawnTrailParticles(PlayerEntity player, World world) {
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();
        double randomX = playerX + (world.random.nextDouble() - 0.5) * 0.5;
        double randomZ = playerZ + (world.random.nextDouble() - 0.5) * 0.5;

        world.addParticle(ParticleTypes.FLAME, randomX, playerY + 0.1, randomZ, 0, 0.05, 0);
    }

    private boolean hasFullArmorSet(PlayerEntity player) {
        ItemStack helmet = player.getInventory().getArmorStack(3);
        ItemStack chestplate = player.getInventory().getArmorStack(2);
        ItemStack leggings = player.getInventory().getArmorStack(1);
        ItemStack boots = player.getInventory().getArmorStack(0);

        return !helmet.isEmpty() && helmet.getItem() instanceof FireGodArmorItem &&
                !chestplate.isEmpty() && chestplate.getItem() instanceof FireGodArmorItem &&
                !leggings.isEmpty() && leggings.getItem() instanceof FireGodArmorItem &&
                !boots.isEmpty() && boots.getItem() instanceof FireGodArmorItem;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.fire_god_armor.shift_prompt").formatted(Formatting.GRAY));

        if(Screen.hasShiftDown()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.translatable("tooltip.skam.fire_god_armor.set_bonus_header").formatted(Formatting.GOLD));
            tooltip.add(Text.translatable("tooltip.skam.fire_god_armor.set_bonus_1").formatted(Formatting.GRAY));
            tooltip.add(Text.translatable("tooltip.skam.fire_god_armor.set_bonus_2").formatted(Formatting.GRAY));
            tooltip.add(Text.translatable("tooltip.skam.fire_god_armor.set_bonus_active").formatted(Formatting.RED));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.getMaterial().getRepairIngredient().test(ingredient) || super.canRepair(stack, ingredient);
    }
}
