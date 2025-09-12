package com.example.skam.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource; // <-- 引入 DamageSource
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion; // <-- 引入 Explosion
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MoltenGreataxeItem extends AxeItem {
    public MoltenGreataxeItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.setFireTicks(100); // 燃烧5秒

        // 20%的几率触发爆炸
        World world = attacker.getWorld();
        if (!world.isClient() && world.random.nextFloat() < 0.20f) {

            // --- 这是修改后的代码块 ---

            // 1. 获取世界的伤害源管理器
            DamageSource damageSource = world.getDamageSources().explosion(attacker, attacker);

            // 2. 调用更详细的createExplosion方法
            world.createExplosion(
                    attacker,                                   // 爆炸的实体来源
                    damageSource,                               // **我们手动创建的伤害源**
                    null,                                       // 爆炸行为 (null使用默认)
                    target.getX(),                              // 爆炸位置 X
                    target.getY(),                              // 爆炸位置 Y
                    target.getZ(),                              // 爆炸位置 Z
                    2.0f,                                       // 爆炸威力
                    false,                                      // 是否产生火焰 (设为false更稳定)
                    World.ExplosionSourceType.NONE         // 破坏类型：不破坏方块
            );
            // ---------------------
        }
        return super.postHit(stack, target, attacker);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.fire_explosion").formatted(Formatting.DARK_RED));
        super.appendTooltip(stack, world, tooltip, context);
    }
}


