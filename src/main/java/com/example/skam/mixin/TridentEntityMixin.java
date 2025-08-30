package com.example.skam.mixin;

import com.example.skam.util.SkamEnchantConfig;
import com.example.skam.nbt.CoreBonusData;
import com.example.skam.nbt.NbtInjector;
import com.example.skam.util.CoreNbtApplicator;
import com.example.skam.util.IEntityDataSaver;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Map;
import com.example.skam.item.core.CoreType;
import com.example.skam.client.CursedTridentAccess;
import com.example.skam.enchantment.ModEnchantments;
import com.example.skam.interfaces.IStasisEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin extends PersistentProjectileEntity implements IStasisEntity, CursedTridentAccess {

    // ... (所有字段定义保持不变) ...
    @Shadow private ItemStack tridentStack;
    @Shadow private boolean dealtDamage;
    @Unique private static final TrackedData<Integer> STASIS_TICKS = DataTracker.registerData(TridentEntity.class, TrackedDataHandlerRegistry.INTEGER);
    @Unique private int stasisLevel = 0;
    @Unique private int stasisTimeoutTicks = 0;
   // @Unique private static final int STASIS_TIMEOUT_DURATION = 30;
    @Unique private boolean isReleasedForLoyalty = false;
    @Unique private int initialFlyTimer = -1;
  //  @Unique private static final int INITIAL_FLY_DURATION = 5;
    @Unique private static final TrackedData<Boolean> SKAM_CURSED = DataTracker.registerData(TridentEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Unique private static final TrackedData<Integer> SKAM_CURSED_LEVEL = DataTracker.registerData(TridentEntity.class, TrackedDataHandlerRegistry.INTEGER);
    @Unique private boolean skamHasDealtAoeDamage = false;


    protected TridentEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Accessor("dealtDamage")
    abstract void setDealtDamage(boolean dealtDamage);

    // =========================================================================
    // ==                    【核心 Bug 修复】 - handleFrozenState           ==
    // =========================================================================
    @Unique
    private void handleFrozenState() {
        this.setNoGravity(true);
        this.setVelocity(Vec3d.ZERO);
        this.inGround = false;

        // --- 【终极安全网】 ---
        // 如果三叉戟因为任何原因卡在了墙里，立刻释放它，防止永久卡住。
        // 这是最可靠的检查。
        if (this.isInsideWall()) {
            releaseToNormalState();
            return; // 立即退出，避免执行下面的超时逻辑
        }

        if (this.stasisTimeoutTicks > 0) {
            this.stasisTimeoutTicks--;
        } else {
            // 悬浮超时，释放它
            releaseToNormalState();
            this.setDealtDamage(false); // 允许忠诚返回
        }
    }


    // --- 其他所有方法都保持不变，为了方便你复制，我把它们都列在下面 ---

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTickStasisLogic(CallbackInfo ci) {
        this.calculateDimensions();

        if (this.getWorld().isClient() || this.stasisLevel == 0) return;

        // 无论何时，只要落地就应该能被捡起来
        if (this.inGround && !this.isReleasedForLoyalty) {
            releaseToNormalState();
            return;
        }

        if (this.isReleasedForLoyalty) return;

        // 初始飞行逻辑
        if (this.initialFlyTimer > 0) {
            this.initialFlyTimer--;
            if (this.initialFlyTimer == 0) { enterFrozenState(); handleFrozenState(); ci.cancel(); }
            return;
        }

        // 重新瞄准后的飞行逻辑
        if (this.getStasisTicks() > 0 && this.getStasisTicks() < Integer.MAX_VALUE) {
            this.setStasisTicks(this.getStasisTicks() - 1);
            if (this.getStasisTicks() == 0) { enterFrozenState(); handleFrozenState(); ci.cancel(); }
            return;
        }

        // 冻结状态逻辑
        if (this.getStasisTicks() == Integer.MAX_VALUE) {
            handleFrozenState();
            ci.cancel();
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        int cursedLevel = this.skam$getCursedLevel();
        if (cursedLevel > 0) {
            float baseScale = 1.0f;
            // --- 修改: 从配置读取缩放值 ---
            float scalePerLevel = SkamEnchantConfig.get().cursed_trident.scale_per_level;
            float scale = baseScale + (cursedLevel - 1) * scalePerLevel;
            return super.getDimensions(pose).scaled(scale);
        }
        return super.getDimensions(pose);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickAreaDamageCheck(CallbackInfo ci) {
        if (this.getWorld().isClient() || this.skam$getCursedLevel() <= 0 || this.skamHasDealtAoeDamage) { return; }
        List<Entity> potentialTargets = this.getWorld().getOtherEntities(this, this.getBoundingBox(), this::canHit);
        if (potentialTargets.isEmpty()) { return; }
        Entity owner = this.getOwner();
        DamageSource damageSource = this.getDamageSources().trident(this, owner == null ? this : owner);
        boolean hitOccurred = false;
        for (Entity target : potentialTargets) {
            if (target.isAlive() && target instanceof LivingEntity livingTarget) {
                hitOccurred = true;
                float totalDamage = 8.0F;
                totalDamage += EnchantmentHelper.getAttackDamage(this.tridentStack, livingTarget.getGroup());
                livingTarget.damage(damageSource, totalDamage);
            }
        }
        if (hitOccurred) {
            this.skamHasDealtAoeDamage = true;
            this.setDealtDamage(true);
            this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_TRIDENT_HIT, this.getSoundCategory(), 1.0F, 1.0F);
            this.setVelocity(this.getVelocity().multiply(0.75));
        }
    }

    @Override
    public void resetDealtDamage() {
        this.setDealtDamage(false);
        this.skamHasDealtAoeDamage = false;
    }

    @Unique
    private void enterFrozenState() {
        this.setStasisTicks(Integer.MAX_VALUE);
        // --- 修改: 从配置读取超时 ---
        this.stasisTimeoutTicks = SkamEnchantConfig.get().time_lag_thorn.stasis_timeout_ticks;
    } @Unique private void releaseToNormalState() { this.setStasisTicks(0); this.setNoGravity(false); this.isReleasedForLoyalty = true; }
    @ModifyVariable(method = "tick", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private int suppressLoyalty(int loyaltyLevel) { if (this.stasisLevel > 0 && !this.isReleasedForLoyalty) return 0; return loyaltyLevel; }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V", at = @At("TAIL"))
    private void onInit(World world, LivingEntity owner, ItemStack stack, CallbackInfo ci) {
        this.stasisLevel = EnchantmentHelper.getLevel(ModEnchantments.TIME_LAG_THORN, stack);
        if (this.stasisLevel > 0) {this.initialFlyTimer = SkamEnchantConfig.get().time_lag_thorn.initial_fly_ticks;
        }
        if (!stack.isEmpty()) {
            int enchantLevel = EnchantmentHelper.getLevel(ModEnchantments.CURSED_TRIDENT, stack);
            boolean isCursed = enchantLevel > 0;
            this.getDataTracker().set(SKAM_CURSED, isCursed);
            this.getDataTracker().set(SKAM_CURSED_LEVEL, enchantLevel);
            if (isCursed) { this.setGlowing(true); }
        }
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void onInitDataTracker(CallbackInfo ci) {
        this.getDataTracker().startTracking(STASIS_TICKS, 0);
        this.getDataTracker().startTracking(SKAM_CURSED, false);
        this.getDataTracker().startTracking(SKAM_CURSED_LEVEL, 0);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("StasisLevelOnThrow", this.stasisLevel);
        nbt.putInt("StasisTicks", this.getStasisTicks());
        nbt.putInt("InitialFlyTimer", this.initialFlyTimer);
        nbt.putInt("StasisTimeoutTicks", this.stasisTimeoutTicks);
        nbt.putBoolean("IsReleasedForLoyalty", this.isReleasedForLoyalty);
        nbt.putBoolean("skam_cursed", this.skam$isCursed());
        nbt.putInt("skam_cursed_level", this.skam$getCursedLevel());
        nbt.putBoolean("SkamAoeDealt", this.skamHasDealtAoeDamage);
    }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.stasisLevel = nbt.getInt("StasisLevelOnThrow");
        this.setStasisTicks(nbt.getInt("StasisTicks"));
        this.initialFlyTimer = nbt.getInt("InitialFlyTimer");
        this.stasisTimeoutTicks = nbt.getInt("StasisTimeoutTicks");
        this.isReleasedForLoyalty = nbt.getBoolean("IsReleasedForLoyalty");
        boolean isCursed = nbt.getBoolean("skam_cursed");
        int level = nbt.getInt("skam_cursed_level");
        this.getDataTracker().set(SKAM_CURSED, isCursed);
        this.getDataTracker().set(SKAM_CURSED_LEVEL, level);
        if (isCursed) { this.setGlowing(true); }
        this.skamHasDealtAoeDamage = nbt.getBoolean("SkamAoeDealt");
    }

    @Override public int getStasisTicks() { return this.getDataTracker().get(STASIS_TICKS); }
    @Override public void setStasisTicks(int ticks) { this.getDataTracker().set(STASIS_TICKS, ticks); }
    @Override public void setReleasedForLoyalty(boolean released) { this.isReleasedForLoyalty = released; }
    @Override public int getStasisLevel() { return this.stasisLevel; }
    @Override public void setAirborne() { this.inGround = false; }
    @Override public boolean skam$isCursed() { return this.getDataTracker().get(SKAM_CURSED); }
    @Override public void skam$setCursed(boolean cursed) { this.getDataTracker().set(SKAM_CURSED, cursed); if (!cursed) { this.getDataTracker().set(SKAM_CURSED_LEVEL, 0); } }
    @Override public int skam$getCursedLevel() { return this.getDataTracker().get(SKAM_CURSED_LEVEL); }
}
