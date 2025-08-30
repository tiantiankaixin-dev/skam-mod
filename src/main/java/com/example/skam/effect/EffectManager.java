// src/main/java/com/example/skam/effect/EffectManager.java
package com.example.skam.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EffectManager {
    private static final ConcurrentHashMap<UUID, List<ScheduledEffect>> scheduledEffects = new ConcurrentHashMap<>();

    public static class ScheduledEffect {
        public final Runnable effect;
        public final int delay;
        public int currentTick;

        public ScheduledEffect(Runnable effect, int delay) {
            this.effect = effect;
            this.delay = delay;
            this.currentTick = 0;
        }

        public boolean tick() {
            currentTick++;
            if (currentTick >= delay) {
                effect.run();
                return true;
            }
            return false;
        }
    }

    public static void scheduleEffect(UUID circleId, Runnable effect, int delayTicks) {
        scheduledEffects.computeIfAbsent(circleId, k -> new ArrayList<>())
                .add(new ScheduledEffect(effect, delayTicks));
    }

    public static void tickScheduledEffects() {
        scheduledEffects.entrySet().removeIf(entry -> {
            List<ScheduledEffect> effects = entry.getValue();
            effects.removeIf(ScheduledEffect::tick);
            return effects.isEmpty();
        });
    }

    public static void clearEffects(UUID circleId) {
        scheduledEffects.remove(circleId);
    }
}


