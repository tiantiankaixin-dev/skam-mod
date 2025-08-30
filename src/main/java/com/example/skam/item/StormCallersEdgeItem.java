package com.example.skam.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.List;

public class StormCallersEdgeItem extends SwordItem {
    public StormCallersEdgeItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.getWorld().isClient()) {
            // --- 这是需要修改的行 ---
            // 将最后一个参数从 0 (等级I) 修改为 2 (等级III)
            attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 3));
            // ---------------------
        }
        return super.postHit(stack, target, attacker);
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§7攻击敌人时，会为使用者提供短暂的速度爆发").formatted(Formatting.GRAY));

    }
}
