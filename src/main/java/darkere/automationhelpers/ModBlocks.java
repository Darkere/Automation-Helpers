package darkere.automationhelpers;

import darkere.automationhelpers.ItemFluidBuffer.ItemFluidBuffer;
import darkere.automationhelpers.OrderedHopper.OrderedHopper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModBlocks {

    @GameRegistry.ObjectHolder("automationhelpers:itemfluidbuffer")
    public static ItemFluidBuffer itemFluidBuffer;

    @GameRegistry.ObjectHolder("automationhelpers:orderedhopper")
    public static OrderedHopper orderedHopper;

    @SideOnly(Side.CLIENT)
    public static void initModels(){
        itemFluidBuffer.initModels();
        orderedHopper.initModels();

    }
}
