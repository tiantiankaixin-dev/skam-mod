package com.example.skam.screen;

import com.example.skam.SkamMod;
import com.example.skam.item.HandheldDispenserItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

public class HandheldDispenserScreenHandler extends ScreenHandler {
    private final SimpleInventory inventory;
    private final ItemStack dispenserStack;
    public HandheldDispenserScreenHandler(int syncId, PlayerInventory playerInventory, ItemStack dispenserStack) {
        super(SkamMod.HANDHELD_DISPENSER_SCREEN_HANDLER, syncId);
        this.dispenserStack = dispenserStack;
        this.inventory = new SimpleInventory(9);

        DefaultedList<ItemStack> items = HandheldDispenserItem.getInventory(dispenserStack);
        for (int i = 0; i < items.size(); i++) {
            this.inventory.setStack(i, items.get(i));
        }

        this.inventory.onOpen(playerInventory.player);

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.inventory, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
    public HandheldDispenserScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, buf.readItemStack());
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (slotIndex < 9) {
                if (!this.insertItem(originalStack, 9, 45, true)) return ItemStack.EMPTY;
            } else {
                if (!this.insertItem(originalStack, 0, 9, false)) return ItemStack.EMPTY;
            }
            if (originalStack.isEmpty()) slot.setStack(ItemStack.EMPTY);
            else slot.markDirty();
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return player.getMainHandStack() == this.dispenserStack || player.getOffHandStack() == this.dispenserStack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        DefaultedList<ItemStack> items = DefaultedList.ofSize(9, ItemStack.EMPTY);
        for(int i = 0; i < 9; i++) {
            items.set(i, this.inventory.getStack(i));
        }
        HandheldDispenserItem.saveInventory(this.dispenserStack, items);
    }
}
