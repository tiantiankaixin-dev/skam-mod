package com.example.skam.screen;

import com.example.skam.block.entity.EnchantmentUpgraderBlockEntity;
import com.example.skam.item.ModItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import java.util.Map;

public class EnchantmentUpgraderScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public EnchantmentUpgraderScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(3));
    }

    public EnchantmentUpgraderScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.ENCHANTMENT_UPGRADER_SCREEN_HANDLER, syncId);
        checkSize(inventory, 3);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);
        this.addSlot(new Slot(inventory, EnchantmentUpgraderBlockEntity.BOOK_SLOT, 26, 26));  // 左侧附魔书
        this.addSlot(new Slot(inventory, EnchantmentUpgraderBlockEntity.ITEM_SLOT, 80, 26));  // 中间物品
        this.addSlot(new Slot(inventory, EnchantmentUpgraderBlockEntity.GEM_SLOT, 134, 26)); // 右侧宝石
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
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
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == 0) {
            ItemStack bookStack = inventory.getStack(EnchantmentUpgraderBlockEntity.BOOK_SLOT);
            ItemStack itemStack = inventory.getStack(EnchantmentUpgraderBlockEntity.ITEM_SLOT);
            ItemStack gemStack = inventory.getStack(EnchantmentUpgraderBlockEntity.GEM_SLOT);
            if (itemStack.isEmpty() || bookStack.isEmpty() || gemStack.isEmpty()) return false;
            if (!bookStack.isOf(Items.ENCHANTED_BOOK) || !gemStack.isOf(ModItems.MAGIC_ENERGY_GEM)) return false;
            Map<Enchantment, Integer> bookEnchantments = EnchantmentHelper.get(bookStack);
            if (bookEnchantments.size() != 1) return false;
            Enchantment bookEnchantment = bookEnchantments.keySet().iterator().next();
            int bookLevel = bookEnchantments.get(bookEnchantment);
            Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.get(itemStack);
            if (!itemEnchantments.containsKey(bookEnchantment)) return false;
            int itemLevel = itemEnchantments.get(bookEnchantment);
            if (itemLevel != bookLevel) return false;
            bookStack.decrement(1);
            gemStack.decrement(1);
            Map<Enchantment, Integer> newEnchantments = itemEnchantments;
            newEnchantments.put(bookEnchantment, itemLevel + 1);
            EnchantmentHelper.set(newEnchantments, itemStack);
            inventory.markDirty();

            return true;
        }
        return false;
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

