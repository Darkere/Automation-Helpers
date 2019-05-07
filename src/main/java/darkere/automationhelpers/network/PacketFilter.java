package darkere.automationhelpers.network;

import darkere.automationhelpers.OrderedHopper.OrderedHopperContainer;
import darkere.automationhelpers.automationhelpers;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class PacketFilter implements IMessage{
    HashMap<Integer,ItemStack> lockedItems;


    @Override
    public void fromBytes(ByteBuf buf) {
        lockedItems = new HashMap<>();
        int size = buf.readInt();
        for(int i = 0;i<size;i++){
            lockedItems.put(buf.readInt(),ByteBufUtils.readItemStack(buf));
        }


    }

    @Override
    public void toBytes(ByteBuf buf) {
        Set set2 = lockedItems.entrySet();
        Iterator iter2 = set2.iterator();
        buf.writeInt(lockedItems.size());
        while(iter2.hasNext()){
            Map.Entry mentry = (Map.Entry)iter2.next();
            buf.writeInt((int)mentry.getKey());
            ByteBufUtils.writeItemStack(buf,(ItemStack)mentry.getValue());
        }



    }

    public PacketFilter( HashMap lockedItems) {
        this.lockedItems = lockedItems;

    }
    public PacketFilter() {
    }

    public static class Handler implements IMessageHandler<PacketFilter, IMessage> {
            @Override
            public IMessage onMessage(PacketFilter message, MessageContext ctx) {
                automationhelpers.proxy.addScheduledTaskClient(() -> handle(message, ctx));
                return null;
            }

            private void handle(PacketFilter message, MessageContext ctx) {
                EntityPlayer player = automationhelpers.proxy.getClientPlayer();
                if (player.openContainer instanceof OrderedHopperContainer) {
                    ((OrderedHopperContainer) player.openContainer).newFilter(message.lockedItems);
                }
            }
    }
}
