package com.example.skam.item;

import com.example.skam.mixin.DispenserBlockAccessor;
import com.example.skam.screen.HandheldDispenserScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class HandheldDispenserItem extends Item {

    public HandheldDispenserItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user.isSneaking()) {
            if (!world.isClient) {
                user.openHandledScreen(createScreenHandlerFactory(stack));
            }
            return TypedActionResult.consume(stack);
        }
        if (!world.isClient) {
            dispense((ServerWorld) world, user, stack);
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    private void dispense(ServerWorld world, PlayerEntity player, ItemStack dispenserStack) {
        DefaultedList<ItemStack> inventory = getInventory(dispenserStack);
        int randomSlot = -1;
        int nonEmptySlots = 0;
        for (int k = 0; k < inventory.size(); ++k) {
            if (!inventory.get(k).isEmpty()) {
                nonEmptySlots++;
                if (Random.create().nextInt(nonEmptySlots) == 0) {
                    randomSlot = k;
                }
            }
        }

        if (randomSlot != -1) {
            ItemStack stackToDispense = inventory.get(randomSlot).copy();
            ItemStack originalStackInSlot = inventory.get(randomSlot);
            boolean handledManually = false;
            Item item = stackToDispense.getItem();

            if (item instanceof ArrowItem) {
                PersistentProjectileEntity arrowEntity = ((ArrowItem) item).createArrow(world, stackToDispense, player);
                arrowEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, 3.0f, 1.0f);
                world.spawnEntity(arrowEntity);
                handledManually = true;
            } else if (item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderPearlItem || item instanceof ExperienceBottleItem) {
                ThrownItemEntity thrownItemEntity;
                if (item instanceof SnowballItem) thrownItemEntity = new SnowballEntity(world, player);
                else if (item instanceof EggItem) thrownItemEntity = new EggEntity(world, player);
                else if (item instanceof EnderPearlItem) thrownItemEntity = new EnderPearlEntity(world, player);
                else thrownItemEntity = new ExperienceBottleEntity(world, player);
                thrownItemEntity.setItem(stackToDispense);
                thrownItemEntity.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, 1.5f, 1.0f);
                world.spawnEntity(thrownItemEntity);
                handledManually = true;
            } else if (item == Items.FIRE_CHARGE) {
                Vec3d lookVec = player.getRotationVector();
                Vec3d eyePos = player.getEyePos();
                SmallFireballEntity fireballEntity = new SmallFireballEntity(world, eyePos.getX(), eyePos.getY(), eyePos.getZ(), lookVec.x, lookVec.y, lookVec.z);
                fireballEntity.setOwner(player);
                world.spawnEntity(fireballEntity);
                world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0f, (world.random.nextFloat() - world.random.nextFloat()) * 0.2f + 1.0f);
                handledManually = true;
            } else if (Block.getBlockFromItem(item) == Blocks.TNT) {
                TntEntity tntEntity = new TntEntity(world, player.getX(), player.getEyeY(), player.getZ(), player);
                tntEntity.setFuse(80);
                tntEntity.setVelocity(player.getRotationVector().multiply(0.8));
                world.spawnEntity(tntEntity);
                world.playSound(null, tntEntity.getX(), tntEntity.getY(), tntEntity.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0f, 1.0f);
                handledManually = true;
            }

            if (handledManually) {
                originalStackInSlot.decrement(1);
                if (item != Items.FIRE_CHARGE && Block.getBlockFromItem(item) != Blocks.TNT) {
                    world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }
            } else {
                Direction facing = Direction.getFacing(player.getRotationVector().x, player.getRotationVector().y, player.getRotationVector().z);
                BlockState fakeDispenserState = Blocks.DISPENSER.getDefaultState().with(DispenserBlock.FACING, facing);
                BlockPointer pointer = createFakeBlockPointer(world, player, fakeDispenserState);
                DispenserBehavior behavior = ((DispenserBlockAccessor) Blocks.DISPENSER).callGetBehaviorForItem(originalStackInSlot);
                behavior.dispense(pointer, originalStackInSlot);
                world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1.0f, 1.2f);
            }

            saveInventory(dispenserStack, inventory);

        } else {
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.2f);
        }
    }

    private NamedScreenHandlerFactory createScreenHandlerFactory(ItemStack stack) {
        return new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                buf.writeItemStack(stack);
            }
            @Override
            public Text getDisplayName() {
                return Text.translatable(HandheldDispenserItem.this.getTranslationKey());
            }
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return new HandheldDispenserScreenHandler(syncId, playerInventory, stack);
            }
        };
    }

    private BlockPointer createFakeBlockPointer(ServerWorld world, PlayerEntity player, BlockState state) {
        return new BlockPointer() {
            @Override
            public BlockPos getPos() { return player.getBlockPos(); }
            @Override
            public double getX() { return player.getX(); }
            @Override
            public double getY() { return player.getEyeY(); }
            @Override
            public double getZ() { return player.getZ(); }
            @Override
            public BlockState getBlockState() { return state; }
            @Override
            public <T extends BlockEntity> T getBlockEntity() { return null; }
            @Override
            public ServerWorld getWorld() { return world; }
        };
    }

    public static DefaultedList<ItemStack> getInventory(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        DefaultedList<ItemStack> inventory = DefaultedList.ofSize(9, ItemStack.EMPTY);
        if (nbt != null) {
            Inventories.readNbt(nbt, inventory);
        }
        return inventory;
    }

    public static void saveInventory(ItemStack stack, DefaultedList<ItemStack> inventory) {
        boolean empty = inventory.stream().allMatch(ItemStack::isEmpty);
        if (empty) {
            stack.setNbt(null);
        } else {
            Inventories.writeNbt(stack.getOrCreateNbt(), inventory);
        }
    }
}

