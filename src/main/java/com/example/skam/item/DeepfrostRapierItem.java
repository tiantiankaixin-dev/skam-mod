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

public class DeepfrostRapierItem extends SwordItem {
    public DeepfrostRapierItem(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
        super(toolMaterial, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.getWorld().isClient()) {
            // 施加 3 秒的 缓慢II 和 虚弱I
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2)); // 60 ticks = 3 seconds, amplifier 1 = Level II
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1)); // amplifier 0 = Level I
        }
        return super.postHit(stack, target, attacker);
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.literal("§7攻击时，会给敌人施加强力的缓慢和虚弱效果").formatted(Formatting.GRAY));

    }
}
