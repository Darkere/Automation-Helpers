package darkere.automationhelpers.OrderedHopper;

import darkere.automationhelpers.network.Messages;
import darkere.automationhelpers.network.PacketFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class TileOrderedHopper extends TileEntity implements ITickable {
    static final int SIZE = 9;

    private ItemStack stackToInsert;
    private int slot, currentslot, count;
    private int timer;
    private int strike = 0;
    private boolean redstoneActive = false, active = true;
    private boolean set = false;
    private boolean slotfound;
    private ItemStack tempStack;
    private HashMap<Integer, ItemStack> lockedItems = new HashMap<>();
    private Queue<ItemStack> insertSlotQueue = new LinkedList<>();
    private ItemStackHandler itemStackHandler;
    private Rmode currentMode = Rmode.roff;
    private int activeTimer = 0;
    private int counter;

    public TileOrderedHopper() {
    }

    {
        itemStackHandler = new ItemStackHandler(SIZE) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (lockedItems.containsKey(slot)) {
                    if (lockedItems.get(slot).isItemEqual(stack)) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            }

            @Override
            protected void onContentsChanged(int slot) {
                // We need to tell the tile entity that something has changed so
                // that the chest contents is persisted
                world.updateComparatorOutputLevel(getPos(), null);
                if (!active && !world.isRemote) {
                    active = true;
                }
                TileOrderedHopper.this.markDirty();
            }
        };
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("items")) {
            itemStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("items"));
        }
        if (compound.hasKey("RedStoneMode")) {
            this.currentMode = Rmode.valueOf(compound.getString("RedStoneMode"));
        }
        if (compound.hasKey("CurrentSlot")) {
            this.currentslot = compound.getInteger("CurrentSlot");
        }
        if (compound.hasKey("Set")) {
            this.set = compound.getBoolean("Set");
        }
        if (compound.hasKey("Filter")) {
            NBTTagList list = compound.getTagList("Filter", Constants.NBT.TAG_COMPOUND);
            lockedItems.clear();
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound item = list.getCompoundTagAt(i);
                lockedItems.put(item.getInteger("Slot"), new ItemStack(item));
            }
        }


    }

    public HashMap<Integer,ItemStack> getLockedItems() {
        HashMap<Integer, ItemStack> i;
        i = (HashMap) lockedItems.clone();
        return i;
    }


    @Override
    public void onLoad() {
        super.onLoad();
        redstoneControl();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {

        compound.setTag("items", itemStackHandler.serializeNBT());
        compound.setString("RedStoneMode", currentMode.getMode());
        compound.setBoolean("Set", set);
        compound.setInteger("CurrentSlot", currentslot);
        NBTTagList list = new NBTTagList();
        for (Map.Entry<Integer, ItemStack> entry : lockedItems.entrySet()) {
            NBTTagCompound comp = new NBTTagCompound();
            comp.setInteger("Slot", entry.getKey());
            entry.getValue().writeToNBT(comp);
            list.appendTag(comp);
        }
        compound.setTag("Filter", list);
        super.writeToNBT(compound);
        return compound;
    }

    public boolean canInteractWith(EntityPlayer playerIn) {
        // If we are too far away from this tile entity you cannot use it
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
        }
        return super.getCapability(capability, facing);
    }

    public void redstoneControl() {
        boolean redstone = world.isBlockPowered(pos);
        switch (currentMode) {
            case roff: {
                redstoneActive = !redstone;
                break;
            }
            case ron: {
                redstoneActive = redstone;
                break;
            }
            case on: {
                redstoneActive = true;
                break;

            }
            case off: {
                redstoneActive = false;
                break;
            }

        }
        active = true;
        currentslot = 0;
    }

    @Override
    public void update() {
        if (world.isRemote) return;
        if (redstoneActive && active)

            runItemSender();
    }

    public Rmode getCurrentMode() {
        return currentMode;
    }


    public boolean getSet() {
        return set;
    }

    public void toggleSet() {
        set = !set;
    }

    public void dropAllItems() {
        for (slot = 0; slot < SIZE; slot++) {
            stackToInsert = itemStackHandler.getStackInSlot(slot);
            InventoryHelper.spawnItemStack(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), stackToInsert);
        }
    }

    public void storeFilter(HashMap lockedItems1) {
        if (world.isRemote) {

            this.lockedItems = lockedItems1;
        }
    }

    private void runItemSender() {
        if (!set) {
            if (timer <= 0) {
                IItemHandler handler = getOppositeItemHandler(pos, EnumFacing.DOWN);
                if (handler != null) {
                    for (slot = currentslot; slot < SIZE; slot++) {
                        if (!itemStackHandler.getStackInSlot(slot).equals(ItemStack.EMPTY)) {
                            stackToInsert = itemStackHandler.extractItem(slot, 1, true);
                            if (ItemHandlerHelper.insertItem(handler, stackToInsert, true).equals(ItemStack.EMPTY)) {
                                pushItem(handler, stackToInsert, slot);
                                timer = 10;
                                currentslot++;
                                return;

                            } // NO Free foreign slots
                            savePerformance(40, 40, 100, false);
                            return;
                        }//Slot is empty
                        currentslot++;
                    }//All slots after current are empty
                    if (currentslot == 0) {
                        savePerformance(20, 5, 60, true);
                    }
                    currentslot = 0;
                } else { // NO Tile Below
                    savePerformance(0, 100, 100, true);
                }
            } else
                timer--;
        } else {
            if (timer <= 0) {
                if (!checkForFullSet()) return;
                IItemHandler handler = getOppositeItemHandler(pos, EnumFacing.DOWN);
                if (handler != null) {
                    counter = 0;
                    for (slot = 0; slot < SIZE; slot++) {
                        if (!itemStackHandler.getStackInSlot(slot).equals(ItemStack.EMPTY)) {
                            stackToInsert = itemStackHandler.extractItem(slot, 1, true);
                            if (ItemHandlerHelper.insertItem(handler, stackToInsert, true).equals(ItemStack.EMPTY)) {
                                insertSlotQueue.add(stackToInsert);
                            } else {
                                slotfound = false;
                            }
                        } else counter++;
                    }
                    if (counter == SIZE) { //NO items to push
                        savePerformance(40, 40, 100, true);
                    }
                    if (slotfound) {
                        for (slot = 0; slot < SIZE; slot++) {
                            if (!itemStackHandler.getStackInSlot(slot).equals(ItemStack.EMPTY)) {
                                tempStack = insertSlotQueue.poll();
                                if (tempStack != null) {
                                    pushItem(handler, tempStack, slot);
                                }
                            }
                        }
                        insertSlotQueue.clear();
                        savePerformance(0, 30, 0, false);
                        return;

                    } else {//Not enough Space for insertion
                        savePerformance(40, 40, 100, false);
                        slotfound = true;
                        return;
                    }
                    //Nothing in there
                }//NO Tile below
                savePerformance(0, 100, 100, true);
            }
            timer--;
        }
    }

    private boolean checkForFullSet() {
        if (lockedItems.isEmpty()) return true;
        boolean found = true;
        for (Map.Entry<Integer, ItemStack> entry : lockedItems.entrySet()) {
            if (itemStackHandler.getStackInSlot(entry.getKey()).getCount() < 1) {
                found = false;
            }
        }
        return found;
    }

    private IItemHandler getOppositeItemHandler(BlockPos pos, EnumFacing facing) {
        TileEntity tile = world.getTileEntity(pos.offset(facing));
        if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())) {
            return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
        }
        return null;
    }

    private void savePerformance(int numberOfStrikes, int timeramount, int punishamount, boolean deactivate) {
        if (strike >= 100) {
            timer = punishamount;
            strike = 0;
        } else {
            strike += numberOfStrikes;
            timer = timeramount;
        }
        if (activeTimer >= 5) {
            activeTimer = 0;
            active = false;
            return;
        }
        if (deactivate) {
            activeTimer++;
        }

    }

    private void pushItem(IItemHandler handler, ItemStack stackToInsert, int slotForExtraction) {
        ItemHandlerHelper.insertItem(handler, stackToInsert, false);
        itemStackHandler.extractItem(slotForExtraction, 1, false);
        strike = 0;
    }

    public void updateFilter() {
        lockedItems.clear();
        for (int slots = 0; slots < SIZE; slots++) {
            if (!itemStackHandler.getStackInSlot(slots).equals(ItemStack.EMPTY)) {
                lockedItems.put(slots, itemStackHandler.getStackInSlot(slots).copy());
            }

        }
    }

    public void sendFilter(EntityPlayerMP player) {

        Messages.INSTANCE.sendTo(new PacketFilter(lockedItems), player);
    }

    public void updateRedstoneControl(Rmode mode) {
        currentMode = mode;
        redstoneControl();
    }

    public @Nonnull
    NBTTagCompound getUpdateTag() {
        return writeToNBT(super.getUpdateTag());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    public void setActive() {
        active = true;
    }

    public int getComparatorOutput() {
        count = 0;
        for (int i = 0; i < SIZE; i++) {
            if (itemStackHandler.getStackInSlot(i).isEmpty()) continue;
            count += itemStackHandler.getStackInSlot(i).getCount();
        }
        if (count == 0) return 0;
        float x = (float) count / ((float) SIZE * itemStackHandler.getSlotLimit(0));
        float y = 15 * x;
        return (int) Math.max(1, y);

    }
}
