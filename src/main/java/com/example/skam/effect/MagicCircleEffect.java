// src/main/java/com/example/skam/effect/MagicCircleEffect.java
package com.example.skam.effect;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MagicCircleEffect {
    private static final Map<UUID, CircleData> activeCircles = new ConcurrentHashMap<>();


    private static final Vector3f SILVER_BLUE = new Vector3f(0.7f, 0.85f, 1.0f);
    private static final Vector3f DEEP_BLUE = new Vector3f(0.2f, 0.4f, 0.9f);
    private static final Vector3f BRIGHT_SILVER = new Vector3f(0.9f, 0.95f, 1.0f);
    private static final Vector3f ELECTRIC_BLUE = new Vector3f(0.3f, 0.7f, 1.0f);

    public static class CircleData {
        public Vec3d position;
        public int level;
        public int age;
        public int maxAge;
        public float rotation;
        public float beaconRotation;
        public int soundPhase;

        public CircleData(Vec3d position, int level) {
            this.position = position;
            this.level = level;
            this.age = 0;
            this.maxAge = 400;
            this.rotation = 0;
            this.beaconRotation = 0;
            this.soundPhase = 0;
        }
    }

    public static void createMagicCircle(World world, Vec3d position, int level) {
        if (world.isClient) return;

        UUID circleId = UUID.randomUUID();
        CircleData data = new CircleData(position, level);
        activeCircles.put(circleId, data);

       world.playSound(null, position.x, position.y, position.z,
                SoundEvents.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.PLAYERS,
                2.0f * level, 0.5f);

        world.playSound(null, position.x, position.y, position.z,
                SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.PLAYERS,
                1.5f * level, 0.8f);

        world.playSound(null, position.x, position.y, position.z,
                SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS,
                1.0f * level, 1.2f);
    }

    public static void tickAll(ServerWorld world) {
        if (activeCircles.isEmpty()) return;

        activeCircles.entrySet().removeIf(entry -> {
            UUID id = entry.getKey();
            CircleData data = entry.getValue();

            data.age++;
            data.rotation += 2.0f + (data.level * 0.5f);
            data.beaconRotation += 1.0f;
            data.soundPhase++;

            if (data.soundPhase % 60 == 0 && data.age < data.maxAge - 40) {
                world.playSound(null, data.position.x, data.position.y, data.position.z,
                        SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS,
                        0.8f, 1.0f + (data.level * 0.2f));
            }

           spawnParticles(world, data);

            return data.age >= data.maxAge;
        });
    }

    private static void spawnParticles(ServerWorld world, CircleData data) {
        Vec3d pos = data.position;
        float scale = getScale(data);
        float baseRadius = 15.0f * scale * data.level; // 更大的基础半径

       spawnMainCircles(world, pos, baseRadius, data, scale);

        spawnRuneRings(world, pos, baseRadius, data);

       spawnBeaconBeam(world, pos, scale, data);

       spawnDragonReviveEffect(world, pos, baseRadius, data);

        spawnWardenSonicRings(world, pos, baseRadius, data);

        spawnConstellationPattern(world, pos, baseRadius, data);

        spawnEnergySpirals(world, pos, baseRadius, data);

        spawnFloatingRunes(world, pos, baseRadius, data);

        if (data.age % 40 == 0) {
            spawnParticleBurst(world, pos, baseRadius, data);
        }
    }

    private static void spawnMainCircles(ServerWorld world, Vec3d pos, float baseRadius, CircleData data, float scale) {

        spawnCircleParticles(world, pos, baseRadius, 48, SILVER_BLUE, data.rotation, 2.0f);


        spawnCircleParticles(world, pos.add(0, 0.2, 0), baseRadius * 0.7f, 36,
                ELECTRIC_BLUE, -data.rotation * 1.2f, 1.8f);


        spawnCircleParticles(world, pos.add(0, 0.4, 0), baseRadius * 0.4f, 24,
                BRIGHT_SILVER, data.rotation * 1.5f, 1.5f);


        spawnCircleParticles(world, pos.add(0, 0.1, 0), baseRadius * 0.2f, 16,
                DEEP_BLUE, -data.rotation * 2.0f, 2.2f);
    }

    private static void spawnRuneRings(ServerWorld world, Vec3d pos, float baseRadius, CircleData data) {
        int[] runeCounts = {12, 8, 16};
        float[] radiuses = {baseRadius * 0.85f, baseRadius * 0.55f, baseRadius * 1.15f};

        for (int ring = 0; ring < runeCounts.length; ring++) {
            int runeCount = runeCounts[ring];
            float radius = radiuses[ring];
            float rotation = data.rotation * (1.0f + ring * 0.3f);

            for (int i = 0; i < runeCount; i++) {
                double angle = (i * 2.0 * Math.PI / runeCount) + Math.toRadians(rotation);
                double x = pos.x + radius * Math.cos(angle);
                double z = pos.z + radius * Math.sin(angle);
                double y = pos.y + 0.3 + ring * 0.2;


                world.spawnParticles(ParticleTypes.ENCHANT,
                        x, y, z, 5, 0.3, 0.2, 0.3, 0.1);


                world.spawnParticles(ParticleTypes.GLOW,
                        x, y, z, 2, 0.1, 0.1, 0.1, 0.05);
            }
        }
    }

    private static void spawnBeaconBeam(ServerWorld world, Vec3d pos, float scale, CircleData data) {
        double height = 25.0 * scale;
        int layers = 20;

        for (int i = 0; i < layers; i++) {
            double y = pos.y + (i * height / layers);
            double beaconRadius = 0.5 + 0.3 * Math.sin(data.beaconRotation * 0.1 + i * 0.5);


            for (int j = 0; j < 4; j++) {
                double angle = (j * Math.PI / 2) + Math.toRadians(data.beaconRotation);
                double offsetX = beaconRadius * Math.cos(angle);
                double offsetZ = beaconRadius * Math.sin(angle);

                world.spawnParticles(ParticleTypes.END_ROD,
                        pos.x + offsetX, y, pos.z + offsetZ, 1, 0, 0, 0, 0.02);
            }


            world.spawnParticles(new DustParticleEffect(BRIGHT_SILVER, 2.5f),
                    pos.x, y, pos.z, 3, 0.1, 0, 0.1, 0);


            if (i % 3 == 0) {
                world.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        pos.x, y, pos.z, 1, 0.2, 0.1, 0.2, 0.05);
            }
        }
    }

    private static void spawnDragonReviveEffect(ServerWorld world, Vec3d pos, float baseRadius, CircleData data) {

        float dragonRadius = baseRadius * 1.3f;
        int segments = 60;

        for (int i = 0; i < segments; i++) {
            double angle = (i * 2.0 * Math.PI / segments) + Math.toRadians(data.rotation * 0.5f);
            double x = pos.x + dragonRadius * Math.cos(angle);
            double z = pos.z + dragonRadius * Math.sin(angle);
            double y = pos.y + 2.0 + 1.5 * Math.sin(angle * 3 + data.age * 0.1);


            world.spawnParticles(ParticleTypes.DRAGON_BREATH,
                    x, y, z, 2, 0.2, 0.3, 0.2, 0.1);


            Vector3f enderColor = new Vector3f(0.5f, 0.2f, 0.8f);
            world.spawnParticles(new DustParticleEffect(enderColor, 1.8f),
                    x, y, z, 1, 0, 0, 0, 0);
        }
    }

    private static void spawnWardenSonicRings(ServerWorld world, Vec3d pos, float baseRadius, CircleData data) {

        if (data.age % 20 == 0) {
            for (int ring = 0; ring < 3; ring++) {
                float sonicRadius = baseRadius * (0.6f + ring * 0.4f);
                int particles = 32 + ring * 8;

                for (int i = 0; i < particles; i++) {
                    double angle = i * 2.0 * Math.PI / particles;
                    double x = pos.x + sonicRadius * Math.cos(angle);
                    double z = pos.z + sonicRadius * Math.sin(angle);
                    double y = pos.y + 0.5 + ring * 0.3;

                    world.spawnParticles(ParticleTypes.SONIC_BOOM,
                            x, y, z, 1, 0, 0, 0, 0);


                    Vector3f sonicColor = new Vector3f(0.1f, 0.3f, 0.6f);
                    world.spawnParticles(new DustParticleEffect(sonicColor, 2.0f),
                            x, y, z, 1, 0.1, 0.05, 0.1, 0.02);
                }
            }
        }
    }

    private static void spawnConstellationPattern(ServerWorld world, Vec3d pos, float baseRadius, CircleData data) {

        Vec3d[] starPositions = {
                new Vec3d(baseRadius * 0.8, 3, 0),
                new Vec3d(baseRadius * 0.4, 3, baseRadius * 0.6),
                new Vec3d(-baseRadius * 0.3, 3, baseRadius * 0.7),
                new Vec3d(-baseRadius * 0.9, 3, -baseRadius * 0.2),
                new Vec3d(0, 3, -baseRadius * 0.8),
                new Vec3d(baseRadius * 0.6, 3, -baseRadius * 0.5)
        };

        for (Vec3d starPos : starPositions) {
            double rotatedX = starPos.x * Math.cos(Math.toRadians(data.rotation * 0.3))
                    - starPos.z * Math.sin(Math.toRadians(data.rotation * 0.3));
            double rotatedZ = starPos.x * Math.sin(Math.toRadians(data.rotation * 0.3))
                    + starPos.z * Math.cos(Math.toRadians(data.rotation * 0.3));

            double finalX = pos.x + rotatedX;
            double finalY = pos.y + starPos.y;
            double finalZ = pos.z + rotatedZ;


            world.spawnParticles(new DustParticleEffect(BRIGHT_SILVER, 2.5f),
                    finalX, finalY, finalZ, 8, 0.3, 0.3, 0.3, 0.1);

            world.spawnParticles(ParticleTypes.GLOW,
                    finalX, finalY, finalZ, 3, 0.2, 0.2, 0.2, 0.05);
        }
    }

    private static void spawnEnergySpirals(ServerWorld world, Vec3d pos, float baseRadius, CircleData data) {

        for (int spiral = 0; spiral < 4; spiral++) {
            double spiralHeight = 8.0;
            int spiralSegments = 30;

            for (int i = 0; i < spiralSegments; i++) {
                double progress = (double) i / spiralSegments;
                double spiralAngle = progress * 4 * Math.PI + data.age * 0.1 + spiral * Math.PI / 2;
                double spiralRadius = baseRadius * 0.3 * (1.0 - progress * 0.3);

                double x = pos.x + spiralRadius * Math.cos(spiralAngle);
                double z = pos.z + spiralRadius * Math.sin(spiralAngle);
                double y = pos.y + progress * spiralHeight;

                Vector3f spiralColor = new Vector3f(
                        0.6f + 0.4f * (float) Math.sin(spiralAngle + spiral),
                        0.7f + 0.3f * (float) Math.cos(spiralAngle),
                        1.0f
                );

                world.spawnParticles(new DustParticleEffect(spiralColor, 1.5f),
                        x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    private static void spawnFloatingRunes(ServerWorld world, Vec3d pos, float baseRadius, CircleData data) {

        int runeCount = 20;
        for (int i = 0; i < runeCount; i++) {
            double angle = (i * 2.0 * Math.PI / runeCount) + data.age * 0.02;
            double runeRadius = baseRadius * (0.9 + 0.2 * Math.sin(data.age * 0.05 + i));
            double runeHeight = 4.0 + 2.0 * Math.sin(data.age * 0.03 + i * 0.5);

            double x = pos.x + runeRadius * Math.cos(angle);
            double z = pos.z + runeRadius * Math.sin(angle);
            double y = pos.y + runeHeight;


            world.spawnParticles(ParticleTypes.WITCH,
                    x, y, z, 3, 0.2, 0.2, 0.2, 0.1);

            world.spawnParticles(new DustParticleEffect(ELECTRIC_BLUE, 1.8f),
                    x, y, z, 2, 0.15, 0.15, 0.15, 0.05);
        }
    }

    private static void spawnParticleBurst(ServerWorld world, Vec3d pos, float baseRadius, CircleData data) {

        int burstParticles = 50 * data.level;

        for (int i = 0; i < burstParticles; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * baseRadius;
            double height = Math.random() * 6;

            double x = pos.x + distance * Math.cos(angle);
            double z = pos.z + distance * Math.sin(angle);
            double y = pos.y + height;

            world.spawnParticles(ParticleTypes.FIREWORK,
                    x, y, z, 1, 0, 0, 0, 0.2);

            world.spawnParticles(new DustParticleEffect(SILVER_BLUE, 2.0f),
                    x, y, z, 1, 0.3, 0.3, 0.3, 0.1);
        }
    }

    private static float getScale(CircleData data) {
        if (data.age < 40) {

            float progress = (float) data.age / 40.0f;
            return progress * progress * (3.0f - 2.0f * progress); // 平滑曲线
        } else if (data.age > data.maxAge - 60) {

            float progress = (float) (data.maxAge - data.age) / 60.0f;
            return progress * progress;
        } else {

            return 1.0f + 0.15f * (float) Math.sin(data.age * 0.08f);
        }
    }

    private static void spawnCircleParticles(ServerWorld world, Vec3d center, float radius,
                                             int count, Vector3f color, float rotation, float size) {
        for (int i = 0; i < count; i++) {
            double angle = (i * 2.0 * Math.PI / count) + Math.toRadians(rotation);
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);
            double y = center.y + 0.15 * Math.sin(angle * 4); // 波浪效果

            world.spawnParticles(new DustParticleEffect(color, size),
                    x, y, z, 1, 0, 0, 0, 0);
        }
    }
}