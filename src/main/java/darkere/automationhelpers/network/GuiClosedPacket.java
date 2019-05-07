package darkere.automationhelpers.network;

import darkere.automationhelpers.ItemFluidBuffer.ItemFluidBufferContainer;
import darkere.automationhelpers.ItemFluidBuffer.TileItemFluidBuffer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GuiClosedPacket implements  IMessage {
        private BlockPos pos;
        private int ID;
        // You need this constructor!
        public GuiClosedPacket() {
        }
        public GuiClosedPacket(BlockPos pos){
            this.pos = pos;

        }

        @Override
        public void fromBytes(ByteBuf buf) {
            pos = BlockPos.fromLong(buf.readLong());
        }
        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeLong(pos.toLong());

        }

        public static class Handler implements IMessageHandler<darkere.automationhelpers.network.GuiClosedPacket, IMessage> {
            @Override
            public IMessage onMessage(darkere.automationhelpers.network.GuiClosedPacket message, MessageContext ctx) {
                EntityPlayerMP player =  ctx.getServerHandler().player;
                player.getServerWorld().addScheduledTask(() -> handle(message, ctx));
                return null;
            }

            private void handle(darkere.automationhelpers.network.GuiClosedPacket message, MessageContext ctx) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                if(!(player.openContainer instanceof ItemFluidBufferContainer)) {
                    if(player.getEntityWorld().getTileEntity(message.pos) != null) {
                        TileEntity tile = player.getEntityWorld().getTileEntity(message.pos);
                        if(tile instanceof TileItemFluidBuffer){
                            ((TileItemFluidBuffer) tile).stopUpdating(player);
                        }
                    }


                }
            }
        }
}
