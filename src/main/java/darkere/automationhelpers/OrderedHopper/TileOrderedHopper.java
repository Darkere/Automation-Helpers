package darkere.automationhelpers.OrderedHopper;

import darkere.automationhelpers.network.Messages;
import darkere.automationhelpers.network.PacketFilter;
import javafx.util.Pair;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class TileOrderedHopper extends TileEntity implements ITickable {
    public static final int SIZE = 9;

    private ItemStack sender;
    private boolean changed;
    private int slot, foreignslot, currentslot;
    private int timer;
    private int strike = 0;
    private boolean run = false,active = true;
    private boolean set = false;
    private boolean enoughSpace, slotfound;
    private Pair<Integer, ItemStack> tempPair;
    HashMap<Integer, ItemStack> lockedItems= new HashMap<>();
    private Queue<Pair<Integer, ItemStack>> insertSlotQueue = new LinkedList<>();
    private ItemStackHandler itemStackHandler;
    private Rmode currentMode = Rmode.roff;
    private int activeTimer =0;
    int counter;

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
        if (compound.hasKey("RedStoneMode")){
            this.currentMode =Rmode.valueOf( compound.getString("RedStoneMode"));
        }
        if (compound.hasKey("CurrentSlot")){
            this.currentslot = compound.getInteger("CurrentSlot");
        }
        if(compound.hasKey("Set")){
            this.set = compound.getBoolean("Set");
        }
        if(compound.hasKey("Filter")){
            NBTTagList list = compound.getTagList("Filter", Constants.NBT.TAG_COMPOUND);
            lockedItems.clear();
            for(int i = 0; i<list.tagCount();i++){
                NBTTagCompound item = list.getCompoundTagAt(i);
                lockedItems.put(item.getInteger("Slot"),new ItemStack(item));
            }
        }






       // TileOrderedHopper.this.markDirty();



    }

    public HashMap getLockedItems() {
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
        compound.setString("RedStoneMode",currentMode.getMode());
        compound.setBoolean("Set",set);
        compound.setInteger("CurrentSlot",currentslot);
        NBTTagList list = new NBTTagList();
        for(Map.Entry<Integer, ItemStack> entry : lockedItems.entrySet()){
            NBTTagCompound comp = new NBTTagCompound();
            comp.setInteger("Slot",entry.getKey());
            entry.getValue().writeToNBT(comp);
            list.appendTag(comp);
        }
        compound.setTag("Filter",list);
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
        switch (currentMode){
            case roff: {
                run  = redstone ?  false : true;
                break;
            }
            case ron:{
                run  = redstone ?  true : false;
                break;
            }
            case on:{
                run = true;
                break;

            }
            case off:{
                run = false;
                break;
            }

        }
        currentslot = 0;
    }

    @Override
    public void update() {
        if (run && active)
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
            sender = itemStackHandler.getStackInSlot(slot);
            InventoryHelper.spawnItemStack(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), sender);
        }


    }

    public void storeFilter(HashMap lockedItems1) {
        if (world.isRemote) {

            this.lockedItems = lockedItems1;
        }
    }

    private void runItemSender() {
        if (!set) {
            if (timer <= 0 && !world.isRemote) {
                TileEntity tile = world.getTileEntity(pos.offset(EnumFacing.DOWN));
                if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN)) {
                    for (slot = currentslot; slot < SIZE; slot++) {
                        if (!itemStackHandler.getStackInSlot(slot).equals(ItemStack.EMPTY)) {
                            sender = itemStackHandler.extractItem(slot, 1, true);
                            IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
                            int foreignslots = handler.getSlots();
                            for (foreignslot = 0; foreignslot < foreignslots; foreignslot++) {
                                if (handler.insertItem(foreignslot, sender, true).equals(ItemStack.EMPTY)) {
                                    pushItems(handler, foreignslot, sender, slot);
                                    timer = 5;
                                    currentslot++;
                                    return;
                                }
                            } // NO Free foreign slots
                            savePerformance(40, 40, 100,false);
                            return;
                        }//Slot is empty
                        currentslot++;
                    }//All slots after current are empty
                    if(currentslot == 0){
                        savePerformance(20, 5, 60,true);
                    }
                    currentslot = 0;
                } else { // NO Tile Below
                    savePerformance(0, 100, 100,true);
                }
            } else
                timer--;
        } else {
            if (timer <= 0 && !world.isRemote) {
                TileEntity tile = world.getTileEntity(pos.offset(EnumFacing.DOWN));
                if (tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN)) {
                    IItemHandler handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
                    counter = 0;
                    for (slot = 0; slot < SIZE; slot++) {
                        if (!itemStackHandler.getStackInSlot(slot).equals(ItemStack.EMPTY)) {
                            sender = itemStackHandler.extractItem(slot, 1, true);
                            int foreignslots = handler.getSlots();
                            for (foreignslot = 0; foreignslot < foreignslots; foreignslot++) {
                                if (handler.insertItem(foreignslot, sender, true).equals(ItemStack.EMPTY)) {
                                    insertSlotQueue.add(new Pair<>(foreignslot, sender));
                                    slotfound = true;
                                    break;
                                }

                            }

                            if (!slotfound) {
                                enoughSpace = false;
                            }
                        }else counter++;
                    }
                    if(counter == SIZE){ //NO items to push
                        savePerformance(40,40,100,true);
                    }
                    if (enoughSpace) {
                        for (slot = 0; slot < SIZE; slot++) {
                            if (!itemStackHandler.getStackInSlot(slot).equals(ItemStack.EMPTY)) {
                                tempPair = insertSlotQueue.poll();
                                if (tempPair != null) {
                                    pushItems(handler, tempPair.getKey(), tempPair.getValue(), slot);
                                }
                            }
                        }
                        insertSlotQueue.clear();
                        savePerformance(0, 30, 0,false);
                        return;

                    } else {//Not enough Space for insertion
                        savePerformance(40, 40, 100,false);
                        enoughSpace = true;
                        return;
                    }
                    //Nothing in there
                }//NO Tile below
                savePerformance(0, 100, 100,true);
            }
            timer--;
        }
    }

    private void savePerformance(int numberOfStrikes, int timeramount, int punishamount,boolean deactivate) {
        if (strike >= 100) {
            timer = punishamount;
            strike = 0;
        } else {
            strike += numberOfStrikes;
            timer = timeramount;
        }
        if(activeTimer >= 5){
            activeTimer = 0;
            active = false;
            return;
        }
        if (deactivate){
            activeTimer++;
        }

    }

    private void pushItems(IItemHandler handler, int slotForInsertion, ItemStack sender, int slotForExtraction) {
        handler.insertItem(slotForInsertion, sender, false);
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
    public @Nonnull NBTTagCompound getUpdateTag() {
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
    public boolean isActive(){
        return run && active;
    }
}
