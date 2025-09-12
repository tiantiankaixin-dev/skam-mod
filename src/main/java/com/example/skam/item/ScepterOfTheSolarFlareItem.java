package com.example.skam.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScepterOfTheSolarFlareItem extends Item {

    private static final String COOLDOWN_END_TICK_KEY = "cooldown_end_tick";
    private static final int COOLDOWN_DURATION_TICKS = 600; // 30秒 (30 * 20)

    public ScepterOfTheSolarFlareItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        long currentTime = world.getTime();
        NbtCompound nbt = stack.getNbt();

        if (nbt != null && nbt.contains(COOLDOWN_END_TICK_KEY)) {
            long cooldownEndTick = nbt.getLong(COOLDOWN_END_TICK_KEY);
            if (currentTime < cooldownEndTick) {
                if (world.isClient) {
                    long remainingTicks = cooldownEndTick - currentTime;
                    double remainingSeconds = remainingTicks / 20.0;
                    user.sendMessage(Text.literal(String.format("法杖冷却中... 剩余 %.1f 秒", remainingSeconds)).formatted(Formatting.RED), true);
                }
                return TypedActionResult.fail(stack);
            }
        }

        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) {
            return;
        }

        double maxDistance = 100.0;
        HitResult hitResult = player.raycast(maxDistance, 1.0F, false);

        if (world.isClient) {
            Vec3d startPos = player.getEyePos();
            Vec3d endPos = hitResult.getType() == HitResult.Type.MISS ?
                    startPos.add(player.getRotationVector().multiply(maxDistance)) :
                    hitResult.getPos();

            Vec3d direction = endPos.subtract(startPos).normalize();
            for (double d = 0; d < startPos.distanceTo(endPos); d += 0.5) {
                Vec3d particlePos = startPos.add(direction.multiply(d));
                world.addParticle(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            }
        }

        if (!world.isClient) {
            if (hitResult.getType() != HitResult.Type.MISS) {
                player.stopUsingItem();

                Vec3d hitPos = hitResult.getPos();
                Vec3d spawnPos = hitPos.add(0, 30, 0);

                world.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.PLAYERS, 2.0F, 1.5F);

                int explosionPower = 8;
                FireballEntity fireball = new FireballEntity(world, player, 0, -0.2, 0, explosionPower);
                fireball.setPosition(spawnPos);
                world.spawnEntity(fireball);

                long cooldownEndTick = world.getTime() + COOLDOWN_DURATION_TICKS;
                stack.getOrCreateNbt().putLong(COOLDOWN_END_TICK_KEY, cooldownEndTick);
                stack.damage(5, player, p -> p.sendToolBreakStatus(player.getActiveHand()));
            }
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.contains(COOLDOWN_END_TICK_KEY);
    }

    // *** 这里是修改过的方法 ***
    @Override
    public int getItemBarStep(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(COOLDOWN_END_TICK_KEY)) {
            // 这个方法只在客户端运行，所以我们可以安全地获取客户端的世界实例
            World world = net.minecraft.client.MinecraftClient.getInstance().world;

            // 在某些情况（如在主菜单查看物品时），world可能是null，需要检查
            if (world == null) {
                return 0;
            }

            long currentTime = world.getTime(); // 直接从world对象获取时间
            long cooldownEndTick = nbt.getLong(COOLDOWN_END_TICK_KEY);
            long remainingTicks = cooldownEndTick - currentTime;

            if (remainingTicks <= 0) {
                // 如果冷却结束，我们甚至可以考虑在这里移除NBT标签，以保持物品数据干净
                // stack.getNbt().remove(COOLDOWN_END_TICK_KEY); // 但这可能会引起并发修改异常，暂时不加
                return 0;
            }

            // 计算冷却条的填充比例 (0-13)
            return Math.round(((float) remainingTicks / (float) COOLDOWN_DURATION_TICKS) * 13.0F);
        }
        return 0;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return MathHelper.packRgb(0.2f, 0.4f, 1.0f); // 蓝色
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(COOLDOWN_END_TICK_KEY)) {
            if (world != null && world.isClient()) {
                long currentTime = world.getTime();
                long cooldownEndTick = nbt.getLong(COOLDOWN_END_TICK_KEY);

                if (currentTime < cooldownEndTick) {
                    long remainingTicks = cooldownEndTick - currentTime;
                    double remainingSeconds = remainingTicks / 20.0;
                    tooltip.add(Text.translatable("tooltip.skam.cooldown", remainingSeconds).formatted(Formatting.GRAY)); }
            }
        }
    }

        @Override
        public UseAction getUseAction (ItemStack stack){
            return UseAction.BOW;
        }

        @Override
        public int getMaxUseTime (ItemStack stack){
            return 72000;
        }
    }

