package com.example.skam.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity; // 导入 PlayerEntity
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

public class StaticDirkItem extends SwordItem {
    public StaticDirkItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.getWorld() instanceof ServerWorld world) {
            if (world.random.nextFloat() < 0.20f) { // 保持20%的概率
                Box area = target.getBoundingBox().expand(5.0D);
                List<LivingEntity> nearbyEntities = world.getNonSpectatingEntities(LivingEntity.class, area);

                // --- 这是关键的修改 ---
                // 创建伤害来源前，先判断攻击者类型
                DamageSource damageSource;
                if (attacker instanceof PlayerEntity) {
                    // 如果攻击者是玩家，就创建一个归属于该玩家的伤害来源
                    // 这里需要将 attacker 强制类型转换为 PlayerEntity
                    damageSource = attacker.getDamageSources().playerAttack((PlayerEntity) attacker);
                } else {
                    // 如果攻击者是其他生物（比如僵尸），就创建一个归属于该生物的伤害来源
                    damageSource = attacker.getDamageSources().mobAttack(attacker);
                }
                // ---------------------

                for (LivingEntity entityToSplash : nearbyEntities) {
                    if (entityToSplash != attacker && entityToSplash != target && entityToSplash.isAlive()) {
                        // 使用我们刚刚创建的、正确的伤害来源
                        entityToSplash.damage(damageSource, 3.0F);

                        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                                entityToSplash.getX(), entityToSplash.getY() + entityToSplash.getHeight() / 2, entityToSplash.getZ(),
                                15, 0.3, 0.3, 0.3, 0.1);
                        world.playSound(null, entityToSplash.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, 0.5f, 2.0f);
                    }
                }
            }
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        // 推荐使用 translatable text 以便国际化
        tooltip.add(Text.translatable("item.skam.static_dirk.tooltip").formatted(Formatting.GRAY));
    }
}

