package darkere.automationhelpers;

import darkere.automationhelpers.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = automationhelpers.MODID, name = automationhelpers.MODNAME, version = automationhelpers.MODVERSION, dependencies = "required-after:forge@[14.23.5.2784,)", useMetadata = true)
public class automationhelpers {

    public static final String MODID = "automationhelpers";
    public static final String MODNAME = "Autmomation Helpers" ;
    public static final String MODVERSION= "@version@";

    @SidedProxy(clientSide = "darkere.automationhelpers.proxy.ClientProxy", serverSide = "darkere.automationhelpers.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static automationhelpers instance;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
    }
}