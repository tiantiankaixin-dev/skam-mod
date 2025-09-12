package com.example.skam.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class StormScepterItem extends Item {
    public StormScepterItem(Settings settings) {
        // 建议在注册物品时设置耐久度，例如 .maxDamage(100)
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        // 仅在服务器端执行逻辑
        if (!world.isClient) {
            // --- 关键修正 1：先获取通用的 HitResult，不要立即转换 ---
            HitResult hitResult = user.raycast(128.0D, 0.0F, false);

            // --- 关键修正 2：先检查类型，再进行安全的强制转换，避免崩溃 ---
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                // 确认是方块命中后，再安全地转换为 BlockHitResult
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;

                // --- 优化点 1：让闪电劈在方块顶部，而不是底部，视觉效果更佳 ---
                BlockPos targetPos = blockHitResult.getBlockPos().up();

                ServerWorld serverWorld = (ServerWorld) world;

                // 在目标位置召唤一道真正的、会造成伤害的闪电
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(serverWorld);
                if (lightning != null) {
                    // 使用 Vec3d 来更精确地定位到方块中心
                    lightning.setPos(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
                    serverWorld.spawnEntity(lightning);
                }

                // 消耗5点耐久度
                itemStack.damage(5, user, p -> p.sendToolBreakStatus(hand));
                // 设置2秒冷却时间
                user.getItemCooldownManager().set(this, 40);

                // 操作成功
                return TypedActionResult.success(itemStack);
            }
            // 在 if (hitResult.getType() == HitResult.Type.BLOCK) { ... } 之后添加

            else if (hitResult.getType() == HitResult.Type.ENTITY) {
                // 确认是实体命中后，再安全地转换为 EntityHitResult
                net.minecraft.util.hit.EntityHitResult entityHitResult = (net.minecraft.util.hit.EntityHitResult) hitResult;
                net.minecraft.entity.Entity targetEntity = entityHitResult.getEntity();

                ServerWorld serverWorld = (ServerWorld) world;

                // 在目标实体的位置召唤闪电
                LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(serverWorld);
                if (lightning != null) {
                    lightning.setPos(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
                    serverWorld.spawnEntity(lightning);
                }

                // 同样消耗耐久和设置冷却
                itemStack.damage(5, user, p -> p.sendToolBreakStatus(hand));
                user.getItemCooldownManager().set(this, 40);

                return TypedActionResult.success(itemStack);
            }

        }

        // 如果没有命中方块，或者在客户端，则返回 pass，允许其他操作
        return TypedActionResult.pass(itemStack);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.summon_lightning").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.skam.max_distance").formatted(Formatting.DARK_GRAY)); }
}
