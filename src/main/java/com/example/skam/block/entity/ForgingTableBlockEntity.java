// 文件: com/example/skam/block/entity/ForgingTableBlockEntity.java
package com.example.skam.block.entity;

import com.example.skam.Skam;
import com.example.skam.item.core.CoreType;
import com.example.skam.item.core.ICoreItem;
import com.example.skam.screen.ForgingTableScreenHandler;
import com.example.skam.util.CoreNbtApplicator; // 关键的Import
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class ForgingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, SidedInventory {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private static final int INPUT_SLOT = 0;
    private static final int CORE_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;

    public ForgingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FORGING_TABLE_BLOCK_ENTITY, pos, state);
    }

    /**
     * 【已重构】更新输出槽的结果。
     * 所有复杂逻辑现在都委托给 CoreNbtApplicator 处理。
     */
    public void updateResult() {
        ItemStack inputStack = this.getStack(INPUT_SLOT);
        ItemStack coreStack = this.getStack(CORE_SLOT);

        // 基础验证
        if (inputStack.isEmpty() || !isValidInput(inputStack) || coreStack.isEmpty() || !(coreStack.getItem() instanceof ICoreItem)) {
            this.setStack(OUTPUT_SLOT, ItemStack.EMPTY);
            return;
        }

        // 准备一个副本进行操作，不修改原始输入槽
        ItemStack resultStack = inputStack.copy();
        resultStack.setCount(1); // 输出永远是1个

        ICoreItem coreItem = (ICoreItem) coreStack.getItem();
        boolean success;

        // 根据核心类型，调用不同的中央API方法
        if (coreItem.getCoreType() == CoreType.PURIFICATION) {
            // <-- 核心改动：调用中央净化API
            success = CoreNbtApplicator.tryPurify(resultStack, coreStack);
        } else {
            // <-- 核心改动：调用中央应用API
            success = CoreNbtApplicator.tryApplyCore(resultStack, coreStack);
        }

        // 根据API的返回结果更新输出槽
        if (success) {
            this.setStack(OUTPUT_SLOT, resultStack);
        } else {
            this.setStack(OUTPUT_SLOT, ItemStack.EMPTY);
        }
    }

    // handleCoreApplication, handlePurification, readAppliedCores, writeAppliedCores 这些方法
    // 现在都已经被移除，因为它们的功能已由 updateResult 中的 API 调用取代。

    private boolean isValidInput(ItemStack stack) {
        // 允许所有非空物品进行升级
        return !stack.isEmpty();
    }


    // --- 以下是方块实体和物品栏的标准实现方法 (无改动) ---
    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        if (slot == INPUT_SLOT || slot == CORE_SLOT) {
            updateResult();
        }
        markDirty();
    }
    @Override public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {}
    @Override public Text getDisplayName() { return Text.translatable("block.skam.forging_table"); }
    @Nullable @Override public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        Skam.LOGGER.info("Creating ForgingTableScreenHandler menu.");
        return new ForgingTableScreenHandler(syncId, playerInventory, this);
    }
    @Override protected void writeNbt(NbtCompound nbt) { super.writeNbt(nbt); Inventories.writeNbt(nbt, inventory); }
    @Override public void readNbt(NbtCompound nbt) { super.readNbt(nbt); Inventories.readNbt(nbt, inventory); }
    @Override public int size() { return inventory.size(); }
    @Override public boolean isEmpty() { return inventory.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStack(int slot) { return inventory.get(slot); }
    @Override public ItemStack removeStack(int slot, int amount) { ItemStack stack = Inventories.splitStack(this.inventory, slot, amount); if (!stack.isEmpty()) { updateResult(); } return stack; }
    @Override public ItemStack removeStack(int slot) { ItemStack stack = Inventories.removeStack(this.inventory, slot); updateResult(); return stack; }
    @Override public boolean canPlayerUse(PlayerEntity player) { return this.world != null && this.world.getBlockEntity(this.pos) == this && player.squaredDistanceTo(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5) <= 64.0; }
    @Override public void clear() { inventory.clear(); updateResult(); }
    @Override public int[] getAvailableSlots(Direction side) { return new int[]{INPUT_SLOT, CORE_SLOT, OUTPUT_SLOT}; }
    @Override public boolean canExtract(int slot, ItemStack stack, Direction dir) { return slot == OUTPUT_SLOT; }
    @Override public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) { if (slot == CORE_SLOT) { return stack.getItem() instanceof ICoreItem; } if (slot == INPUT_SLOT) { return isValidInput(stack); } return false; }
}
