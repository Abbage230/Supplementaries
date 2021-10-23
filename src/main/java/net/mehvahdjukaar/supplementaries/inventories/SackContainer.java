package net.mehvahdjukaar.supplementaries.inventories;

import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;


public class SackContainer extends AbstractContainerMenu {
    public final Container inventory;

    public SackContainer(int id, Inventory playerInventory, FriendlyByteBuf packetBuffer) {
        this(id, playerInventory);
    }

    public SackContainer(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(27));
    }

    public SackContainer(int id, Inventory playerInventory, Container inventory) {

        super(ModRegistry.SACK_CONTAINER.get(), id);
        //tile inventory
        this.inventory = inventory;
        checkContainerSize(inventory, 27);
        inventory.startOpen(playerInventory.player);

        int size = ServerConfigs.cached.SACK_SLOTS;

        int[] dims = SackContainer.getRatio(size);
        if (dims[0] > 9) {
            dims[0] = 9;
            dims[1] = (int) Math.ceil(size / 9f);
        }

        int yp = 17 + (18 * 3) / 2 - (9) * dims[1];

        int dimx;
        int xp;
        for (int h = 0; h < dims[1]; ++h) {
            dimx = Math.min(dims[0], size);
            xp = 8 + (18 * 9) / 2 - (dimx * 18) / 2;
            for (int j = 0; j < dimx; ++j) {
                this.addSlot(new SackSlot(inventory, j + (h * dimx), xp + j * 18, yp + 18 * h));
            }
            size -= dims[0];
        }

        for (int si = 0; si < 3; ++si)
            for (int sj = 0; sj < 9; ++sj)
                this.addSlot(new Slot(playerInventory, sj + (si + 1) * 9, 8 + sj * 18, 84 + si * 18));
        for (int si = 0; si < 9; ++si)
            this.addSlot(new Slot(playerInventory, si, 8 + si * 18, 142));
    }


    @Override
    public boolean stillValid(Player playerIn) {
        return this.inventory.stillValid(playerIn);
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            int activeSlots = (5 + (ServerConfigs.cached.SACK_SLOTS * 2));
            if (index < activeSlots) {
                if (!this.moveItemStackTo(itemstack1, activeSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, activeSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }


    /**
     * Called when the container is closed.
     */
    public void removed(Player playerIn) {
        super.removed(playerIn);
        this.inventory.stopOpen(playerIn);
    }


    public static int[] getRatio(int size) {
        int[] dims = {Math.min(size, 23), Math.max(size / 23, 1)};
        for (int[] testAgainst : TARGET_RATIOS) {
            if (testAgainst[0] * testAgainst[1] == size) {
                dims = testAgainst;
                break;
            }
        }
        return dims;
    }


    private static final int[][] TARGET_RATIOS = new int[][]{
            {1, 1},
            {2, 2},
            {3, 3},
            {4, 2},
            {5, 2},
            {6, 2},
            {7, 2},
            {8, 2},
            {8, 2},
            {9, 2},
            {10, 2},
            {7, 3},
            {8, 3},
            {9, 3}
    };


}


