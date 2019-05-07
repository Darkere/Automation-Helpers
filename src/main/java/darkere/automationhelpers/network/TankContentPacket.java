package darkere.automationhelpers.network;

import darkere.automationhelpers.automationhelpers;
import darkere.automationhelpers.ItemFluidBuffer.ItemFluidBufferContainer;
import darkere.automationhelpers.ItemFluidBuffer.TileItemFluidBuffer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TankContentPacket implements IMessage {
    // You need this constructor!
    public TankContentPacket(){

    }
    private FluidStack[] stacks;
    private BlockPos pos;

    public TankContentPacket (FluidStack[] stacks, BlockPos pos) {
        this.pos = pos;
        this.stacks = stacks;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        int size = buf.readInt();
        stacks = new FluidStack[size];
        for (int i = 0; i < size; i++) {
                if(buf.readBoolean()){
                    stacks[i] = FluidStack.loadFluidStackFromNBT(ByteBufUtils.readTag(buf));
                }
        }


    }
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(stacks.length);
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] != null) {
                buf.writeBoolean(true);
                NBTTagCompound tankcompound = new NBTTagCompound();
                stacks[i].writeToNBT(tankcompound);
                ByteBufUtils.writeTag(buf, tankcompound);
            } else {
                buf.writeBoolean(false);
            }
        }

    }

    public static class Handler implements IMessageHandler<TankContentPacket, IMessage> {
        @Override
        public IMessage onMessage(TankContentPacket message, MessageContext ctx) {
            automationhelpers.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(TankContentPacket message, MessageContext ctx) {
            EntityPlayer player = automationhelpers.proxy.getClientPlayer();
            if (player.openContainer instanceof ItemFluidBufferContainer) {
                ((ItemFluidBufferContainer) player.openContainer).updateTankContents(message.stacks);
            }else{
                if(player.getEntityWorld().getTileEntity(message.pos) instanceof TileItemFluidBuffer){
                    Messages.INSTANCE.sendToServer(new GuiClosedPacket(message.pos));

                }
            }
        }
    }
}