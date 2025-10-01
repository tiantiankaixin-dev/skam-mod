package com.example.skam.item;

import com.example.skam.screen.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SwordSheathScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final ItemStack sheathStack;
    private static final String TRIDENT_KEY = "StoredTrident";

   private static final int SHEATH_SLOT_INDEX = 0;
    private static final int PLAYER_INVENTORY_START_INDEX = 1;
    private static final int PLAYER_HOTBAR_START_INDEX = 28;
    private static final int PLAYER_INVENTORY_END_INDEX = 37;

    public SwordSheathScreenHandler(int syncId, PlayerInventory playerInventory, ItemStack sheathStack) {
        super(ModScreenHandlers.SWORD_SHEATH_SCREEN_HANDLER, syncId);
        this.sheathStack = sheathStack;
        this.inventory = new SimpleInventory(1);

        NbtCompound tridentNbt = sheathStack.getSubNbt(TRIDENT_KEY);
        if (tridentNbt != null) {
            inventory.setStack(0, ItemStack.fromNbt(tridentNbt));
        }

        this.addSlot(new Slot(inventory, 0, 80, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() == Items.TRIDENT;
            }
        });

       addPlayerInventory(playerInventory);
       addPlayerHotbar(playerInventory);
    }

    public SwordSheathScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, buf.readItemStack());
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        ItemStack tridentStackInSlot = this.inventory.getStack(0);

        if (!tridentStackInSlot.isEmpty() && tridentStackInSlot.getItem() == Items.TRIDENT) {
            NbtCompound nbt = tridentStackInSlot.getOrCreateNbt();
            String ownerKey = SwordSheathItem.getOwnerNbtKey(player);
            nbt.putBoolean(ownerKey, true);
            tridentStackInSlot.setCustomName(Text.literal(ownerKey).formatted(Formatting.YELLOW));
            sheathStack.setSubNbt(TRIDENT_KEY, tridentStackInSlot.writeNbt(new NbtCompound()));
        } else {
            sheathStack.removeSubNbt(TRIDENT_KEY);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

   @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

           if (index == SHEATH_SLOT_INDEX) {
               if (!this.insertItem(originalStack, PLAYER_INVENTORY_START_INDEX, PLAYER_INVENTORY_END_INDEX, true)) {
                    return ItemStack.EMPTY;
                }
            }
           else {
                if (originalStack.getItem() == Items.TRIDENT) {
                   if (!this.insertItem(originalStack, SHEATH_SLOT_INDEX, PLAYER_INVENTORY_START_INDEX, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (index >= PLAYER_HOTBAR_START_INDEX && index < PLAYER_INVENTORY_END_INDEX) {
                    if (!this.insertItem(originalStack, PLAYER_INVENTORY_START_INDEX, PLAYER_HOTBAR_START_INDEX, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (index >= PLAYER_INVENTORY_START_INDEX && index < PLAYER_HOTBAR_START_INDEX) {
                    if (!this.insertItem(originalStack, PLAYER_HOTBAR_START_INDEX, PLAYER_INVENTORY_END_INDEX, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
