package com.example.skam.mixin;

import com.example.skam.effect.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(LivingEntity.class)
public abstract class PotionTimeAmplificationMixin {

    private static final Map<UUID, Map<StatusEffect, Integer>> originalDurations = new ConcurrentHashMap<>();

    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);
    @Shadow public abstract Map<StatusEffect, StatusEffectInstance> getActiveStatusEffects();
    @Shadow public abstract boolean addStatusEffect(StatusEffectInstance effect);
    @Shadow public abstract boolean removeStatusEffect(StatusEffect type);

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickCheckAmplification(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        UUID entityUuid = entity.getUuid();

        boolean hasAmplification = this.hasStatusEffect(ModEffects.POTION_TIME_AMPLIFICATION);
        boolean wasAmplified = originalDurations.containsKey(entityUuid);

        // 情况一：效果刚刚被加上
        if (hasAmplification && !wasAmplified) {
            // --- 关键修复：读写分离 ---
            Map<StatusEffect, Integer> durationsToStore = new HashMap<>();
            // 1. 创建一个临时的列表，用于存放我们将要应用的“无限”效果
            List<StatusEffectInstance> effectsToMakeInfinite = new ArrayList<>();

            // 2. 第一遍（只读）：遍历现有效果，记录信息，并将要做的改动存入临时列表
            for (StatusEffectInstance effectInstance : this.getActiveStatusEffects().values()) {
                if (effectInstance.getEffectType() == ModEffects.POTION_TIME_AMPLIFICATION) continue;
                if (!effectInstance.getEffectType().isInstant()) {
                    durationsToStore.put(effectInstance.getEffectType(), effectInstance.getDuration());
                    StatusEffectInstance infiniteEffect = new StatusEffectInstance(
                            effectInstance.getEffectType(), Integer.MAX_VALUE, effectInstance.getAmplifier(),
                            effectInstance.isAmbient(), effectInstance.shouldShowParticles(), effectInstance.shouldShowIcon()
                    );
                    // 不在这里直接应用，而是先加到任务列表里
                    effectsToMakeInfinite.add(infiniteEffect);
                }
            }

            // 3. 只有在需要存储时长时，才进行后续操作
            if (!durationsToStore.isEmpty()) {
                // 存储原始时长
                originalDurations.put(entityUuid, durationsToStore);

                // 4. 第二遍（只写）：现在安全地应用所有改动
                for (StatusEffectInstance effectToApply : effectsToMakeInfinite) {
                    this.addStatusEffect(effectToApply);
                }
            }
        }
        // 情况二：效果刚刚消失 (这部分逻辑已在上一版中修复，保持不变)
        else if (!hasAmplification && wasAmplified) {
            Map<StatusEffect, Integer> savedDurations = originalDurations.get(entityUuid);
            if (savedDurations != null) {
                int amplificationTotalDuration = 20 * 30; // 假设增幅效果时长为30秒

                for (Map.Entry<StatusEffect, Integer> entry : savedDurations.entrySet()) {
                    StatusEffect effectType = entry.getKey();
                    if (this.hasStatusEffect(effectType)) {
                        int originalDuration = entry.getValue();
                        int remainingDuration = Math.max(0, originalDuration - amplificationTotalDuration);

                        StatusEffectInstance currentInfiniteEffect = this.getActiveStatusEffects().get(effectType);
                        int amplifier = currentInfiniteEffect.getAmplifier();
                        boolean isAmbient = currentInfiniteEffect.isAmbient();
                        boolean showParticles = currentInfiniteEffect.shouldShowParticles();
                        boolean showIcon = currentInfiniteEffect.shouldShowIcon();

                        this.removeStatusEffect(effectType);

                        if (remainingDuration > 0) {
                            StatusEffectInstance restoredEffect = new StatusEffectInstance(
                                    effectType, remainingDuration, amplifier,
                                    isAmbient, showParticles, showIcon
                            );
                            this.addStatusEffect(restoredEffect);
                        }
                    }
                }
            }
            originalDurations.remove(entityUuid);
        }
    }
}

