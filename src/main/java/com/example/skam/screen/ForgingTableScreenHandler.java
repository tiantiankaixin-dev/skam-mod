// 文件: com/example/skam/screen/ForgingTableScreenHandler.java

package com.example.skam.screen;

import com.example.skam.item.core.ICoreItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class ForgingTableScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    private static final int INPUT_SLOT = 0;
    private static final int CORE_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;
    private static final int INVENTORY_START_INDEX = 0;
    private static final int INVENTORY_END_INDEX = 3;
    private static final int PLAYER_INVENTORY_START_INDEX = 3;
    private static final int PLAYER_INVENTORY_END_INDEX = 39;


    public ForgingTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(3));
    }

    public ForgingTableScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.FORGING_TABLE_SCREEN_HANDLER, syncId);
        checkSize(inventory, 3);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        // --- 改动开始 ---
        // 根据图片中的槽位位置，恢复为正确的坐标

        // 输入槽 (与下方第3个物品栏对齐)
        this.addSlot(new Slot(inventory, INPUT_SLOT, 27, 47) { // <-- 已修改为精确坐标
            @Override
            public boolean canInsert(ItemStack stack) {
                return isInputItem(stack);
            }
        });
        // 核心槽 (Core Slot)
        this.addSlot(new Slot(inventory, CORE_SLOT, 76, 47) { // <-- 已修改为精确坐标
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() instanceof ICoreItem;
            }
        });
        // 输出槽 (Output Slot)
        this.addSlot(new ForgingResultSlot(inventory, OUTPUT_SLOT, 134, 47));
        // --- 改动结束 ---

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private boolean isInputItem(ItemStack stack) {
        // 允许所有物品进行升级
        return !stack.isEmpty();
    }


    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot < INVENTORY_END_INDEX) {
                if (!this.insertItem(originalStack, PLAYER_INVENTORY_START_INDEX, PLAYER_INVENTORY_END_INDEX, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(originalStack, newStack);
            }
            else {
                if (originalStack.getItem() instanceof ICoreItem) {
                    if (!this.insertItem(originalStack, CORE_SLOT, CORE_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (isInputItem(originalStack)) {
                    if (!this.insertItem(originalStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (invSlot >= PLAYER_INVENTORY_START_INDEX && invSlot < PLAYER_INVENTORY_START_INDEX + 27) {
                    if (!this.insertItem(originalStack, PLAYER_INVENTORY_START_INDEX + 27, PLAYER_INVENTORY_END_INDEX, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (invSlot >= PLAYER_INVENTORY_START_INDEX + 27 && invSlot < PLAYER_INVENTORY_END_INDEX) {
                    if (!this.insertItem(originalStack, PLAYER_INVENTORY_START_INDEX, PLAYER_INVENTORY_START_INDEX + 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, originalStack);
        }
        return newStack;
    }

    private static class ForgingResultSlot extends Slot {
        public ForgingResultSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            this.inventory.removeStack(INPUT_SLOT, 1);
            this.inventory.removeStack(CORE_SLOT, 1);
            super.onTakeItem(player, stack);
        }
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
