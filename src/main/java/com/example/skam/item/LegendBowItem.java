package com.example.skam.item;

import com.example.skam.item.core.CoreType;
import com.example.skam.item.core.ICoreItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import com.example.skam.config.BowConfig;
import com.example.skam.entity.LegendArrowEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class LegendBowItem extends BowItem {

   private static final String COOLDOWN_TAG = "LegendBowCD";
    private static final String SKILL_ID = "special_attack";
    private static final String COOLDOWN_END_TIME_KEY = SKILL_ID + "_Time";      // For robust check (based on world time)
    private static final String COOLDOWN_TICKS_LEFT_KEY = SKILL_ID + "_TicksLeft"; // For visual bar

    private static class FireDomain {
        final ServerWorld world;
        final Vec3d center;
        final LivingEntity caster;
        int remainingTicks;

        FireDomain(ServerWorld world, Vec3d center, LivingEntity caster, int durationTicks) {
            this.world = world;
            this.center = center;
            this.caster = caster;
            this.remainingTicks = durationTicks;
        }
    }

    private static final List<FireDomain> activeDomains = new ArrayList<>();

    public static void createFireDomain(ServerWorld world, Vec3d center, LivingEntity caster) {
        BowConfig.FireDomain config = BowConfig.getInstance().fire_domain;
        activeDomains.add(new FireDomain(world, center, caster, config.duration_ticks));
        world.spawnParticles(ParticleTypes.LAVA, center.getX(), center.getY(), center.getZ(), 50, config.radius * 0.5f, 0.5f, config.radius * 0.5f, 0.5);
        world.playSound(null, center.getX(), center.getY(), center.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0f, 0.7f);
    }

    public static void processAllDomains(ServerWorld world) {
        activeDomains.removeIf(domain -> {
            if (domain.world == world) {
                domain.remainingTicks--;
                if (domain.remainingTicks <= 0) return true;
                processSingleDomainTick(domain);
            }
            return false;
        });
    }

    private static void processSingleDomainTick(FireDomain domain) {
        BowConfig.FireDomain config = BowConfig.getInstance().fire_domain;
        float radius = config.radius;
        if (domain.remainingTicks % config.damage_interval_ticks == 0) {
            Box damageBox = new Box(domain.center, domain.center).expand(radius);
            List<LivingEntity> targets = domain.world.getEntitiesByClass(LivingEntity.class, damageBox, e -> e.isAlive() && e != domain.caster && domain.center.isInRange(e.getPos(), radius));
            for(LivingEntity target : targets) {
                target.damage(domain.world.getDamageSources().indirectMagic(domain.caster, domain.caster), config.damage_amount);
                target.setOnFireFor(config.fire_duration_seconds);
            }
        }
        if (domain.remainingTicks % 5 == 0) {
            domain.world.spawnParticles(ParticleTypes.FLAME, domain.center.getX(), domain.center.getY() + 0.5, domain.center.getZ(), 15, radius * 0.7f, 0.2f, radius * 0.7f, 0.02f);
        }
    }

    public LegendBowItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity playerEntity)) return;

        if (playerEntity.isSneaking()) {
            if (world instanceof ServerWorld && user instanceof ServerPlayerEntity serverPlayer) {
                int cooldownTicks = BowConfig.getInstance().legend_bow.special_attack_cooldown_ticks;
                if (!checkAndSetCooldown(serverPlayer, stack, cooldownTicks)) {
                    return;
                }
            } else if (world.isClient && isCoolingDown(stack)) {
                return;
            }

           boolean creativeOrHasInfinity = playerEntity.getAbilities().creativeMode || EnchantmentHelper.getLevel(Enchantments.INFINITY, stack) > 0;
            ItemStack arrowStack = playerEntity.getProjectileType(stack);
            if (!arrowStack.isEmpty() || creativeOrHasInfinity) {
                if (arrowStack.isEmpty()) arrowStack = new ItemStack(Items.ARROW);
                int i = this.getMaxUseTime(stack) - remainingUseTicks;
                float f = getPullProgress(i);
                if ((double)f < 0.1) return;
                if (!world.isClient) {
                    LegendArrowEntity legendArrowEntity = new LegendArrowEntity(world, playerEntity);
                    legendArrowEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, f * 3.0F, 1.0F);
                    if (f == 1.0F) legendArrowEntity.setCritical(true);
                    int powerLevel = EnchantmentHelper.getLevel(Enchantments.POWER, stack);
                    if (powerLevel > 0) legendArrowEntity.setDamage(legendArrowEntity.getDamage() + (double)powerLevel * 0.5 + 0.5);
                    int punchLevel = EnchantmentHelper.getLevel(Enchantments.PUNCH, stack);
                    if (punchLevel > 0) legendArrowEntity.setPunch(punchLevel);
                    if (EnchantmentHelper.getLevel(Enchantments.FLAME, stack) > 0) legendArrowEntity.setOnFireFor(100);
                    stack.damage(1, playerEntity, (p) -> p.sendToolBreakStatus(playerEntity.getActiveHand()));
                    world.spawnEntity(legendArrowEntity);
                }
                world.playSound(null, playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (world.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                if (!creativeOrHasInfinity) {
                    arrowStack.decrement(1);
                    if (arrowStack.isEmpty()) playerEntity.getInventory().removeOne(arrowStack);
                }
                playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
            }
        } else {
            super.onStoppedUsing(stack, world, user, remainingUseTicks);
        }
    }

    private boolean checkAndSetCooldown(ServerPlayerEntity player, ItemStack stack, int cd) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound cooldowns = nbt.getCompound(COOLDOWN_TAG);
        if (!nbt.contains(COOLDOWN_TAG, NbtCompound.COMPOUND_TYPE)) {
            nbt.put(COOLDOWN_TAG, cooldowns);
        }

        long currentTime = player.getWorld().getTime();
        long endTime = cooldowns.getLong(COOLDOWN_END_TIME_KEY);

        if (endTime <= currentTime) {
            cooldowns.putLong(COOLDOWN_END_TIME_KEY, currentTime + cd);
            cooldowns.putInt(COOLDOWN_TICKS_LEFT_KEY, cd);
            return true;
        }

        long remainingTicks = endTime - currentTime;
        String remainingSeconds = String.format("%.1f", remainingTicks / 20.0);
        player.sendMessage(Text.translatable("message.skam.legendary_bow_cooldown", remainingSeconds), true);
      return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.contains(COOLDOWN_TAG, NbtCompound.COMPOUND_TYPE)) {
                NbtCompound cooldowns = nbt.getCompound(COOLDOWN_TAG);
                if (cooldowns.contains(COOLDOWN_TICKS_LEFT_KEY)) {
                    int ticksLeft = cooldowns.getInt(COOLDOWN_TICKS_LEFT_KEY);
                    if (ticksLeft > 0) {
                        cooldowns.putInt(COOLDOWN_TICKS_LEFT_KEY, ticksLeft - 1);
                    }
                }
            }
        }
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.skam.judgment_skill").formatted(Formatting.GOLD));
        tooltip.add(Text.translatable("tooltip.skam.judgment_warning").formatted(Formatting.GRAY));

    }

    private boolean isCoolingDown(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(COOLDOWN_TAG, NbtCompound.COMPOUND_TYPE)) {
            return false;
        }
        return nbt.getCompound(COOLDOWN_TAG).getInt(COOLDOWN_TICKS_LEFT_KEY) > 0;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return isCoolingDown(stack) || super.isItemBarVisible(stack);
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        if (isCoolingDown(stack)) {
            NbtCompound cooldowns = stack.getNbt().getCompound(COOLDOWN_TAG);
            int ticksLeft = cooldowns.getInt(COOLDOWN_TICKS_LEFT_KEY);
            int totalCooldown = BowConfig.getInstance().legend_bow.special_attack_cooldown_ticks;
            return Math.round((float)ticksLeft / (float)totalCooldown * 13.0F);
        }
        return super.getItemBarStep(stack);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        if (isCoolingDown(stack)) {
            return 0xFF55AAFF;
        }
        return super.getItemBarColor(stack);
    }
}
