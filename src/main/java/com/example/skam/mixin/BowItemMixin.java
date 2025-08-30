// 文件: com/example/skam/mixin/BowItemMixin.java

package com.example.skam.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BowItem.class)
public abstract class BowItemMixin {

    /**
     * 这个 Mixin 会在弓创建箭矢实体后，但在其被添加到世界之前，捕获这个箭矢实体。
     * 它的参数列表必须匹配 BowItem.onStoppedUsing 的参数列表。
     *
     * @param originalArrow      被修改的局部变量 (箭矢实体)
     * @param stack              onStoppedUsing 的第一个参数 (弓的 ItemStack)
     * @param world              onStoppedUsing 的第二个参数
     * @param user               onStoppedUsing 的第三个参数 (射手)
     * @param remainingUseTicks  onStoppedUsing 的第四个参数
     * @return 修改后的箭矢实体
     */
    @ModifyVariable(
            method = "onStoppedUsing",
            at = @At(value = "STORE"),
            ordinal = 0 // 捕获第一个 PersistentProjectileEntity 类型的局部变量
    )
    // --- 签名已修正 ---
    private PersistentProjectileEntity modifyArrow(PersistentProjectileEntity originalArrow, ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {

        // 检查弓是否有 NBT 数据 (现在使用 'stack' 而不是 'bowStack')
        if (!stack.hasNbt()) {
            return originalArrow;
        }

        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return originalArrow;
        }

        // 应用弹射物伤害加成
        if (nbt.contains("skam.projectile_damage")) {
            float bonusDamage = nbt.getFloat("skam.projectile_damage");
            originalArrow.setDamage(originalArrow.getDamage() + bonusDamage);
        }

        // 应用弹射物速度加成
        if (nbt.contains("skam.projectile_speed")) {
            float speedMultiplier = 1.0f + nbt.getFloat("skam.projectile_speed");
            // 直接将现有速度向量乘以倍率
            originalArrow.setVelocity(originalArrow.getVelocity().multiply(speedMultiplier));
        }

        return originalArrow;
    }
}

