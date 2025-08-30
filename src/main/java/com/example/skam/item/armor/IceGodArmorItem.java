package com.example.skam.item.armor;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import java.util.List;
import java.util.UUID;

public class IceGodArmorItem extends ArmorItem {

    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("A2B649D1-5C34-4523-A32A-30239B3DFE27");
    private static final String MOVEMENT_SPEED_MODIFIER_NAME = "Ice God's Grace";

    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("B3C149D1-5C34-4523-A32A-30239B3DFE28");
    private static final String ATTACK_DAMAGE_MODIFIER_NAME = "Ice God's Chill";


    public IceGodArmorItem(ArmorMaterial material, Type type, Settings settings) {
        super(material, type, settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof PlayerEntity player)) {
            super.inventoryTick(stack, world, entity, slot, selected);
            return;
        }
        // 我们已经移除了那个限制，现在每一件盔甲都会执行检查
        boolean hasFullSet = hasFullArmorSet(player);
        if (!world.isClient()) {
            EntityAttributeInstance speedAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            EntityAttributeInstance attackAttribute = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (speedAttribute == null || attackAttribute == null) {
                super.inventoryTick(stack, world, entity, slot, selected);
                return; // 提前返回，避免后续代码出错
            }
            // 检查玩家是否拥有完整的套装
            if (hasFullSet) {
                // 添加药水效果
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 220, 0, false, false, true));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 220, 0, false, false, true));
                // 如果没有攻击力加成，就添加它
                if (attackAttribute.getModifier(ATTACK_DAMAGE_MODIFIER_ID) == null) {
                    attackAttribute.addPersistentModifier(new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, ATTACK_DAMAGE_MODIFIER_NAME, 20.0, EntityAttributeModifier.Operation.ADDITION));
                }
                // 根据条件添加或移除速度加成
                boolean shouldHaveSpeedBonus = player.isWet() || world.isRaining();
                if (shouldHaveSpeedBonus) {
                    if (speedAttribute.getModifier(MOVEMENT_SPEED_MODIFIER_ID) == null) {
                        speedAttribute.addTemporaryModifier(new EntityAttributeModifier(MOVEMENT_SPEED_MODIFIER_ID, MOVEMENT_SPEED_MODIFIER_NAME, 0.2, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                    }
                } else {
                    if (speedAttribute.getModifier(MOVEMENT_SPEED_MODIFIER_ID) != null) {
                        speedAttribute.removeModifier(MOVEMENT_SPEED_MODIFIER_ID);
                    }
                }
            } else { // 如果没有完整的套装
                // 如果存在攻击力加成，就移除它
                if (attackAttribute.getModifier(ATTACK_DAMAGE_MODIFIER_ID) != null) {
                    attackAttribute.removeModifier(ATTACK_DAMAGE_MODIFIER_ID);
                }
                // 如果存在速度加成，就移除它
                if (speedAttribute.getModifier(MOVEMENT_SPEED_MODIFIER_ID) != null) {
                    speedAttribute.removeModifier(MOVEMENT_SPEED_MODIFIER_ID);
                }
            }
        }
        // 客户端粒子效果逻辑
        if (world.isClient() && hasFullSet) {
            if (world.getTime() % 4 == 0) {
                spawnChillingAura(player, world);
            }
        }
        super.inventoryTick(stack, world, entity, slot, selected);
    }

    private void spawnChillingAura(PlayerEntity player, World world) {
        double radius = 0.8;
        int particleCount = 8;
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();
        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI / particleCount) * i + (world.getTime() / 25.0);
            double xOffset = radius * Math.cos(angle);
            double zOffset = radius * Math.sin(angle);
            world.addParticle(ParticleTypes.SNOWFLAKE, playerX + xOffset, playerY + 0.2, playerZ + zOffset, 0, 0.05, 0);
        }
    }

    private boolean hasFullArmorSet(PlayerEntity player) {
        ItemStack helmet = player.getInventory().getArmorStack(3);
        ItemStack chestplate = player.getInventory().getArmorStack(2);
        ItemStack leggings = player.getInventory().getArmorStack(1);
        ItemStack boots = player.getInventory().getArmorStack(0);
        return !helmet.isEmpty() && helmet.getItem() instanceof IceGodArmorItem &&
                !chestplate.isEmpty() && chestplate.getItem() instanceof IceGodArmorItem &&
                !leggings.isEmpty() && leggings.getItem() instanceof IceGodArmorItem &&
                !boots.isEmpty() && boots.getItem() instanceof IceGodArmorItem;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.ice_god_armor.shift_prompt").formatted(Formatting.GRAY));
        if(Screen.hasShiftDown()) {
            tooltip.add(Text.literal(""));
            tooltip.add(Text.translatable("tooltip.skam.ice_god_armor.set_bonus_header").formatted(Formatting.AQUA));
            tooltip.add(Text.translatable("tooltip.skam.ice_god_armor.set_bonus_1").formatted(Formatting.GRAY)); // 水下呼吸
            tooltip.add(Text.translatable("tooltip.skam.ice_god_armor.set_bonus_2").formatted(Formatting.GRAY)); // 海豚的恩惠
            tooltip.add(Text.translatable("tooltip.skam.ice_god_armor.set_bonus_active").formatted(Formatting.BLUE)); // 在水中或雨中时...
        }
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.getMaterial().getRepairIngredient().test(ingredient) || super.canRepair(stack, ingredient);
    }
}
