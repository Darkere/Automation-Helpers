package darkere.automationhelpers.network;

import darkere.automationhelpers.OrderedHopper.OrderedHopperContainer;
import darkere.automationhelpers.OrderedHopper.Rmode;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSendButtonPress implements IMessage {
    private boolean filter;
    private Rmode mode;
    // You need this constructor!
    public PacketSendButtonPress() {
    }
    public PacketSendButtonPress(boolean filter, Rmode currentMode, boolean set){

        this.filter = filter;
        mode = currentMode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        filter = buf.readBoolean();
        mode = Rmode.valueOf(ByteBufUtils.readUTF8String(buf));
    }
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(filter);
        ByteBufUtils.writeUTF8String(buf,mode.getMode());
    }

    public static class Handler implements IMessageHandler<PacketSendButtonPress, IMessage> {
        @Override
        public IMessage onMessage(PacketSendButtonPress message, MessageContext ctx) {
           EntityPlayerMP player =  ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSendButtonPress message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if(player.openContainer instanceof OrderedHopperContainer) {
                OrderedHopperContainer p =(OrderedHopperContainer) player.openContainer;
                p.setCurrentMode(message.mode);

                if(message.filter){
                    p.updateFilter();

                    p.sendFilterToClient(player);
                }else{
                    p.setSet();
                }



            }
        }
    }
}