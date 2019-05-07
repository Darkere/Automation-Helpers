package darkere.automationhelpers.OrderedHopper;

import javafx.util.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderedHopperContainer extends Container {

    private TileOrderedHopper te;
    public ArrayList<Pair> slotpositions = new ArrayList<>(te.SIZE);

    public OrderedHopperContainer(IInventory playerInventory, TileOrderedHopper te) {
        this.te = te;
        // This container references items out of our own inventory (the 9 slots we hold ourselves)
        // as well as the slots from the player inventory so that the user can transfer items between
        // both inventories. The two calls below make sure that slots are defined for both inventories.
        addOwnSlots();
        addPlayerSlots(playerInventory);
    }


    private void addPlayerSlots(IInventory playerInventory) {
        // Slots for the main inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 10 + col * 18;
                int y = row * 18 + 71;
                this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 10, x, y));
            }
        }

        // Slots for the hotbar
        for (int row = 0; row < 9; ++row) {
            int x = 10 + row * 18;
            int y = 58 + 71;
            this.addSlotToContainer(new Slot(playerInventory, row, x, y));
        }
    }

    private void addOwnSlots() {
        IItemHandler itemHandler = this.te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        int x = 10;
        int y = 18;
        // Add our own slots
        int slotIndex = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            addSlotToContainer(new SlotItemHandler(itemHandler, slotIndex, x, y));
            addSlottoList(slotIndex,new Pair<Integer,Integer>(x,y));
            slotIndex++;
            x += 18;
        }
    }

    private void addSlottoList(int slotIndex, Pair<Integer, Integer> xy) {
        slotpositions.add(xy);
    }
     public List<IContainerListener> getListeners(){
        return listeners;
     }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < TileOrderedHopper.SIZE) {
                if (!this.mergeItemStack(itemstack1, TileOrderedHopper.SIZE, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, TileOrderedHopper.SIZE, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return te.canInteractWith(playerIn);
    }
    public ArrayList<Pair> getSlotpositions(){
        return slotpositions;
    }

    public void sendFilterToClient(EntityPlayerMP player) {
        te.sendFilter(player);
    }
    public void updateFilter(){
        te.updateFilter();
    }

    public void newFilter(HashMap lockedItems) {
        te.storeFilter(lockedItems);
    }
    public void setSet(){
      te.toggleSet();
    }

    public void setCurrentMode(Rmode mode) {
        te.updateRedstoneControl(mode);
    }
}