package com.example.skam.item.armor;

import com.example.skam.effect.ModEffects;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class ThunderGodArmorItem extends ArmorItem {

    public ThunderGodArmorItem(ArmorMaterial material, Type type, Settings settings) {
        super(material, type, settings);
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

        if (world.isClient() && hasFullSet) {

            if (world.getTime() % 4 == 0) {
                spawnAuraParticles(player, world);
            }
  if (world.getTime() % 2 == 0) {
                spawnGroundCircleParticles(player, world);
            }

        }

        if (world.isClient() && player.hasStatusEffect(ModEffects.THUNDER_CHARGE)) {
            if (world.getTime() % 2 == 0) {
                spawnChargeParticles(player, world);
            }
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    private void spawnGroundCircleParticles(PlayerEntity player, World world) {
       final double radius = 2;
        final int particleCount = 8;

        for (int i = 0; i < particleCount; i++) {
           double angle = world.random.nextDouble() * 2.0 * Math.PI;
 double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
  double px = player.getX() + offsetX;
            double py = player.getY();
            double pz = player.getZ() + offsetZ;
  world.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 0.0, 0.0, 0.0);
        }
    }

    private void spawnAuraParticles(PlayerEntity player, World world) {
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();
        double offsetX = (world.random.nextDouble() - 0.5) * 1.5;
        double offsetY = world.random.nextDouble() * player.getHeight();
        double offsetZ = (world.random.nextDouble() - 0.5) * 1.5;
        world.addParticle(ParticleTypes.ELECTRIC_SPARK, px + offsetX, py + offsetY, pz + offsetZ, 0.0, 0.0, 0.0);
    }

    private void spawnChargeParticles(PlayerEntity player, World world) {
        double px = player.getX();
        double py = player.getY() + world.random.nextDouble() * player.getHeight();
        double pz = player.getZ();
        double offsetX = (world.random.nextDouble() - 0.5) * 1.5;
        double offsetZ = (world.random.nextDouble() - 0.5) * 1.5;
        world.addParticle(ParticleTypes.ELECTRIC_SPARK, px + offsetX, py, pz + offsetZ, 0, 0, 0);
    }

    public static boolean hasFullArmorSet(PlayerEntity player) {
        ItemStack helmet = player.getInventory().getArmorStack(3);
        ItemStack chestplate = player.getInventory().getArmorStack(2);
        ItemStack leggings = player.getInventory().getArmorStack(1);
        ItemStack boots = player.getInventory().getArmorStack(0);

        return !helmet.isEmpty() && helmet.getItem() instanceof ThunderGodArmorItem &&
                !chestplate.isEmpty() && chestplate.getItem() instanceof ThunderGodArmorItem &&
                !leggings.isEmpty() && leggings.getItem() instanceof ThunderGodArmorItem &&
                !boots.isEmpty() && boots.getItem() instanceof ThunderGodArmorItem;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.thunder_god_armor.shift_prompt").formatted(Formatting.GRAY));
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.translatable("tooltip.skam.thunder_god_armor.set_bonus_header").formatted(Formatting.YELLOW));
            tooltip.add(Text.translatable("tooltip.skam.thunder_god_armor.set_bonus_1").formatted(Formatting.GRAY));
            tooltip.add(Text.translatable("tooltip.skam.thunder_god_armor.set_bonus_active").formatted(Formatting.AQUA));
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.getMaterial().getRepairIngredient().test(ingredient) || super.canRepair(stack, ingredient);
    }
}
