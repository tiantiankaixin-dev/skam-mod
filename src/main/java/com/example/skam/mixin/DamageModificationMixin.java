// 文件路径: src/main/java/com/example/skam/mixin/DamageModificationMixin.java
package com.example.skam.mixin;

import com.example.skam.SkamMod;
import com.example.skam.item.core.CoreType;
import com.example.skam.util.CoreNbtApplicator;
import com.example.skam.util.SkamAttributeConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

    /**
     * 使用 @ModifyArg 修改传递给 _damage 方法的伤害值。
     * 这是一个更稳定、更兼容的方法，可以避免二次伤害和无敌帧的问题。
     */
    @Mixin(LivingEntity.class)
    public abstract class DamageModificationMixin {
        @ModifyArg(
                method = "damage",
                at = @At(
                        value = "INVOKE",
                        // -------------------  这里是唯一的改动！ ------------------- //
                        // 将 "_damage" 修改为 "applyDamage"
                        target = "Lnet/minecraft/entity/LivingEntity;applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"
                        // ----------------------------------------------------------- //
                ),
                index = 1
        )
    private float skam_applyUniversalStrengthBonus(DamageSource source, float finalDamage) {
        // 这个方法的参数列表：
        // 1. 必须包含被修改的参数类型 (float finalDamage)。
        // 2. 可以选择性地包含原始方法 (damage) 的所有参数 (DamageSource source, float initialAmount)。
        //    我们只需要 DamageSource，所以我们只声明它。Mixin 会自动把它传进来。
        //    `finalDamage` 是已经过护甲和附魔减免后的最终伤害。

        // --- 安全检查 #1: 如果最终伤害为0或负数，直接返回 ---
        if (finalDamage <= 0) {
            return finalDamage;
        }

        // 1. 识别伤害来源是否为玩家
        PlayerEntity player = null;
        Entity attacker = source.getAttacker();
        Entity sourceEntity = source.getSource();

        if (attacker instanceof PlayerEntity p) {
            player = p;
        } else if (sourceEntity instanceof ProjectileEntity projectile && projectile.getOwner() instanceof PlayerEntity p) {
            // 检查投射物的主人
            player = p;
        }

        // 如果找不到玩家，返回原始伤害
        if (player == null) {
            return finalDamage;
        }

        // --- 伤害源已确认为玩家 ---
        SkamMod.LOGGER.info("[SKAM STRENGTH CORE] Player '{}' is dealing {} final damage. Checking for Strength Cores.", player.getName().getString(), finalDamage);

        // 2. 收集玩家所有装备上的力量核心等级
        int totalStrengthLevel = 0;
        List<ItemStack> equipment = new ArrayList<>();
        player.getArmorItems().forEach(equipment::add);
        equipment.add(player.getMainHandStack());
        equipment.add(player.getOffHandStack());

        for (ItemStack stack : equipment) {
            if (!stack.isEmpty()) {
                Map<CoreType, Integer> cores = CoreNbtApplicator.readAppliedCoresFromStack(stack);
                totalStrengthLevel += cores.getOrDefault(CoreType.STRENGTH, 0);
            }
        }

        // 3. 如果没有力量核心，则返回原始伤害
        if (totalStrengthLevel <= 0) {
            return finalDamage;
        }

        SkamMod.LOGGER.info("[SKAM STRENGTH CORE] Total Strength Core level: {}. Applying bonus damage.", totalStrengthLevel);

        // 4. 计算额外伤害并加到原始伤害上
        SkamAttributeConfig config = SkamAttributeConfig.get();
        double bonusMultiplierPerLevel = config.getAttributesFor(CoreType.STRENGTH).damage_multiplier_per_level;
        float totalBonusMultiplier = (float) (bonusMultiplierPerLevel * totalStrengthLevel);

        // 额外伤害 = 最终伤害（经过护甲减免后） * 核心总加成
        float bonusDamage = finalDamage * totalBonusMultiplier;

        float newTotalDamage = finalDamage + bonusDamage;

        SkamMod.LOGGER.info("[SKAM STRENGTH CORE] Original Final Damage: {}, Bonus Damage: {}, New Total Damage: {}", finalDamage, bonusDamage, newTotalDamage);

        // 5. 返回修改后的新伤害值
        return newTotalDamage;
    }
}

