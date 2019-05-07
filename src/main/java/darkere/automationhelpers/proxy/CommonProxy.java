package darkere.automationhelpers.proxy;

import com.google.common.util.concurrent.ListenableFuture;
import darkere.automationhelpers.automationhelpers;
import darkere.automationhelpers.ItemFluidBuffer.ItemFluidBuffer;
import darkere.automationhelpers.ItemFluidBuffer.TileItemFluidBuffer;
import darkere.automationhelpers.ModBlocks;
import darkere.automationhelpers.OrderedHopper.OrderedHopper;
import darkere.automationhelpers.OrderedHopper.TileOrderedHopper;
import darkere.automationhelpers.network.Messages;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import static darkere.automationhelpers.automationhelpers.instance;

@Mod.EventBusSubscriber
public class CommonProxy {
    public void preInit(FMLPreInitializationEvent e) {
        Messages.registerMessages(automationhelpers.MODID);

    }

    public void init(FMLInitializationEvent e) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GUIProxy());
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new ItemFluidBuffer());
        event.getRegistry().register(new OrderedHopper());
        GameRegistry.registerTileEntity(TileOrderedHopper.class, OrderedHopper.RLOrderedHopper);
        GameRegistry.registerTileEntity(TileItemFluidBuffer.class, ItemFluidBuffer.RLITEMFLUIDBUFFER);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(ModBlocks.itemFluidBuffer).setRegistryName(ItemFluidBuffer.RLITEMFLUIDBUFFER));
        event.getRegistry().register(new ItemBlock(ModBlocks.orderedHopper).setRegistryName(OrderedHopper.RLOrderedHopper));
    }
    public ListenableFuture<Object> addScheduledTaskClient(Runnable runnableToSchedule) {
        throw new IllegalStateException("This should only be called from client side");
    }

    public EntityPlayer getClientPlayer() {
        throw new IllegalStateException("This should only be called from client side");
    }
}