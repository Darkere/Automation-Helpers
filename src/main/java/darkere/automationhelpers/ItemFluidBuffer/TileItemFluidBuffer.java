package darkere.automationhelpers.ItemFluidBuffer;

import darkere.automationhelpers.network.Messages;
import darkere.automationhelpers.network.TankContentPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;

public class TileItemFluidBuffer extends TileEntity implements ITickable {
    static final int SIZE = 9;
    private final int CAPACITY = 10000;
    private final int NUMBEROFTANKS = 4;
    private HashSet<EntityPlayer> playersToUpdate = new HashSet<>();
    private FluidTank[] tanks;
    private FluidHandlerConcatenate fluidhandler;
    private ItemStackHandler itemStackHandler;

    {
        createFluidTanks();
        fluidhandler = new FluidHandlerConcatenate(tanks);
        itemStackHandler = new ItemStackHandler(SIZE) {

            @Override
            protected void onContentsChanged(int slot) {
                // We need to tell the tile entity that something has changed so
                // that the chest contents is persisted
                TileItemFluidBuffer.this.markDirty();
            }
        };
    }

    private void createFluidTanks() {
        tanks = new FluidTank[NUMBEROFTANKS];
        for (int i = 0; i < tanks.length; i++) {
            tanks[i] = new FluidTank(CAPACITY) {
                @Override
                public boolean canFillFluidType(FluidStack fluid) {
                    for (FluidTank tank : tanks) {
                        if (tank != null && tank.getFluid() != null && !tank.equals(this) && tank.getFluid().isFluidEqual(fluid)) { // wenn in einem anderen tank bereist vorhanden
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                protected void onContentsChanged() {
                    TileItemFluidBuffer.this.markDirty();
                }
            };

        }
    }
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }

        return super.hasCapability(capability, facing);
    }
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemStackHandler);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidhandler);
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < tanks.length; i++) {
            NBTTagCompound tankcompound = new NBTTagCompound();
            tanks[i].writeToNBT(tankcompound);
            list.appendTag(tankcompound);
        }
        compound.setTag("Tanks", list);
        compound.setTag("items", itemStackHandler.serializeNBT());


        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("items")) {
            itemStackHandler.deserializeNBT((NBTTagCompound) compound.getTag("items"));
        }
        if (compound.hasKey("Tanks")) {
            NBTTagList list = compound.getTagList("Tanks", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                tanks[i].readFromNBT(list.getCompoundTagAt(i));
            }
        }
        super.readFromNBT(compound);
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

    public FluidStack getFluid(int tanknumber) {
        return tanks[tanknumber].getFluid();
    }

    public int getNumberOfTanks() {
        return tanks.length;
    }

    public double getFluidPercentage(int tanknumber) {
        return (double) tanks[tanknumber].getFluidAmount() / tanks[tanknumber].getCapacity();
    }

    public void setUpdating(boolean b, EntityPlayer player) {
        playersToUpdate.add(player);

    }

    public FluidStack[] getStacks() {
        FluidStack[] stacks = new FluidStack[NUMBEROFTANKS];
        for (int i = 0; i < tanks.length; i++) {
            stacks[i] = tanks[i].getFluid();


        }

        return stacks;
    }

    @Override
    public void update() {
        if (!playersToUpdate.isEmpty() && !world.isRemote) {
            FluidStack[] stacks = getStacks();
                for (EntityPlayer player : playersToUpdate) {
                    if(world.getPlayerEntityByName(player.getName())!= null){
                        Messages.INSTANCE.sendTo(new TankContentPacket(stacks, this.getPos()), (EntityPlayerMP) player);
                    }
                    else
                        playersToUpdate.remove(player);
                }



        }

    }

    public void updateTank(FluidStack[] stacks) {
        for (int i = 0; i < tanks.length; i++) {
            this.tanks[i].setFluid(stacks[i]);
        }

    }

    public void stopUpdating(EntityPlayer player) {
        playersToUpdate.remove(player);
    }
    public boolean canInteractWith(EntityPlayer playerIn) {
        // If we are too far away from this tile entity you cannot use it
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }
}
