package darkere.automationhelpers.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class Messages {

    public static SimpleNetworkWrapper INSTANCE;

    private static int ID = 0;
    private static int nextID() {
        return ID++;
    }

    public static void registerMessages(String channelName) {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);

        // Server side
        INSTANCE.registerMessage(PacketSendButtonPress.Handler.class, PacketSendButtonPress.class, nextID(), Side.SERVER);
        INSTANCE.registerMessage(GuiClosedPacket.Handler.class, GuiClosedPacket.class, nextID(), Side.SERVER);

        // Client side
        INSTANCE.registerMessage(PacketFilter.Handler.class, PacketFilter.class, nextID(), Side.CLIENT);
        INSTANCE.registerMessage(TankContentPacket.Handler.class, TankContentPacket.class, nextID(), Side.CLIENT);

    }
}