package com.example.skam.item;

import com.example.skam.util.ModUtils;
import com.example.skam.mixin.PersistentProjectileEntityAccessor;
import com.example.skam.mixin.TridentEntityAccessor;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SwordSheathItem extends Item {
    public static final String TRIDENT_KEY = "StoredTrident";

    public SwordSheathItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack sheathStack = user.getStackInHand(hand);
        NbtCompound tridentNbt = sheathStack.getSubNbt(TRIDENT_KEY);

       if (user.isSneaking()) {
           if (tridentNbt == null) {
                if (!world.isClient) {
                    recallTrident((ServerPlayerEntity) user, sheathStack);
                }
                return TypedActionResult.success(sheathStack);
            } else {
               user.openHandledScreen(createScreenHandlerFactory(sheathStack));
                return TypedActionResult.consume(sheathStack);
            }
        }
       else {
            if (tridentNbt != null) {
                if (!world.isClient) {
                    throwTrident(world, user, sheathStack, tridentNbt);
                }
                return TypedActionResult.success(sheathStack);
            }
           else {
                user.openHandledScreen(createScreenHandlerFactory(sheathStack));
                return TypedActionResult.consume(sheathStack);
            }
        }
    }

    private void throwTrident(World world, PlayerEntity user, ItemStack sheathStack, NbtCompound tridentNbt) {
       ItemStack tridentToThrow = ItemStack.fromNbt(tridentNbt);
        if (tridentToThrow.isEmpty()) {
            return;
        }
         TridentEntity tridentEntity = new TridentEntity(world, user, tridentToThrow);
         tridentEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 2.5f, 1.0f);
         world.spawnEntity(tridentEntity);
         world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
         sheathStack.removeSubNbt(TRIDENT_KEY);
    }

    private void recallTrident(ServerPlayerEntity player, ItemStack sheathStack) {
       if (sheathStack.getSubNbt(TRIDENT_KEY) != null) {
            player.sendMessage(Text.translatable("item.skam.sword_sheath.already_full").formatted(Formatting.RED), true);
            return;
        }

        ServerWorld world = player.getServerWorld();
        String ownerKey = getOwnerNbtKey(player);

        for (TridentEntity tridentEntity : world.getEntitiesByType(EntityType.TRIDENT, (e) -> true)) {
            ItemStack tridentStack = ((TridentEntityAccessor) tridentEntity).invokeAsItemStack();
            NbtCompound nbt = tridentStack.getNbt();

            if (nbt != null && nbt.contains(ownerKey)) {
                if (((PersistentProjectileEntityAccessor) tridentEntity).isInGround()) {
                    ModUtils.setChunkForced(world, tridentEntity.getChunkPos(), false);
                }

                ItemStack retrievedStack = tridentStack.copy();
                sheathStack.setSubNbt(TRIDENT_KEY, retrievedStack.writeNbt(new NbtCompound()));
                tridentEntity.discard();
                player.sendMessage(Text.translatable("item.skam.sword_sheath.recalled").formatted(Formatting.GREEN), true);
                return;
            }
        }

        player.sendMessage(Text.translatable("item.skam.sword_sheath.not_found").formatted(Formatting.GRAY), true);
    }

    public static String getOwnerNbtKey(PlayerEntity player) {
        return Text.translatable("item.skam.owner_weapon", player.getGameProfile().getName()).getString();
    }
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (stack.getSubNbt(TRIDENT_KEY) != null) {
            tooltip.add(Text.translatable("item.skam.sword_sheath.contains_trident").formatted(Formatting.GRAY));
            tooltip.add(Text.translatable("tooltip.skam.throw_weapon").formatted(Formatting.AQUA));
        } else {
            tooltip.add(Text.translatable("item.skam.sword_sheath.empty").formatted(Formatting.GRAY));
            tooltip.add(Text.translatable("tooltip.skam.sheath_bind_instructions").formatted(Formatting.AQUA));
            tooltip.add(Text.translatable("tooltip.skam.sheath_recall").formatted(Formatting.AQUA));
        }
    }

    private ExtendedScreenHandlerFactory createScreenHandlerFactory(ItemStack stack) {
       return new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) { buf.writeItemStack(stack); }
            @Override
            public Text getDisplayName() { return Text.translatable("item.skam.sword_sheath"); }
            @Override
            public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new SwordSheathScreenHandler(syncId, playerInventory, stack);
            }
        };
    }
}
