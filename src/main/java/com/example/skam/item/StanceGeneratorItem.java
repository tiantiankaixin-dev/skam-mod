package com.example.skam.item;

import com.example.skam.accessor.IAuraAccessor; // 导入我们新的接口
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class StanceGeneratorItem extends Item {

    public StanceGeneratorItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.addStatusEffect(new StatusEffectInstance(
                StatusEffects.RESISTANCE, 100, 4, true, true, true));
        ((IAuraAccessor) user).setAuraTicks(100);
        user.playSound(SoundEvents.ITEM_TRIDENT_RIPTIDE_3, 1.0F, 1.0F);
        user.getItemCooldownManager().set(this, 600);
        return TypedActionResult.success(itemStack, true);
    }
    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}

