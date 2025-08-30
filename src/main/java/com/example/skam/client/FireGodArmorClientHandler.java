package com.example.skam.client;

import com.example.skam.item.armor.FireGodArmorItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class FireGodArmorClientHandler {

    public static void registerClientEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.world != null) {
                handleFireGodArmorEffects(client.player, client.world);
            }
        });
    }

    private static void handleFireGodArmorEffects(PlayerEntity player, World world) {
        if (hasFireGodArmor(player)) {
            if (world.getTime() % 3 == 0) {
                spawnClientFireParticles(world, player);
            }
        }
    }

    private static boolean hasFireGodArmor(PlayerEntity player) {
        for (int i = 0; i < 4; i++) {
            ItemStack armorStack = player.getInventory().getArmorStack(i);
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof FireGodArmorItem) {
                return true;
            }
        }
        return false;
    }

    private static void spawnClientFireParticles(World world, PlayerEntity player) {
        Vec3d pos = player.getPos();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;

        double time = world.getTime() * 0.1;
        for (int i = 0; i < 2; i++) {
            double angle = time + (i * Math.PI);
            double radius = 0.8 + Math.sin(time * 0.5) * 0.2;

            double spiralX = x + Math.cos(angle) * radius;
            double spiralY = y + 0.5 + Math.sin(time * 2) * 0.3;
            double spiralZ = z + Math.sin(angle) * radius;

            world.addParticle(ParticleTypes.FLAME,
                    spiralX, spiralY, spiralZ,
                    0, 0.05, 0);
        }


        if (world.random.nextFloat() < 0.3f) {
            double sparkX = x + (world.random.nextDouble() - 0.5) * 2;
            double sparkY = y + world.random.nextDouble() * 2;
            double sparkZ = z + (world.random.nextDouble() - 0.5) * 2;

            world.addParticle(ParticleTypes.LAVA,
                    sparkX, sparkY, sparkZ,
                    (world.random.nextDouble() - 0.5) * 0.1,
                    0.02,
                    (world.random.nextDouble() - 0.5) * 0.1);
        }
    }
}
