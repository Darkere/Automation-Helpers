package darkere.automationhelpers.proxy;

import darkere.automationhelpers.ItemFluidBuffer.ItemFluidBufferContainer;
import darkere.automationhelpers.ItemFluidBuffer.ItemFluidBufferGui;
import darkere.automationhelpers.ItemFluidBuffer.TileItemFluidBuffer;
import darkere.automationhelpers.OrderedHopper.OrderedHopperContainer;
import darkere.automationhelpers.OrderedHopper.OrderedHopperGUI;
import darkere.automationhelpers.OrderedHopper.TileOrderedHopper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GUIProxy implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileOrderedHopper) {
            return new OrderedHopperContainer(player.inventory, (TileOrderedHopper) te);
        }
        if(te instanceof  TileItemFluidBuffer){
            return new ItemFluidBufferContainer(player.inventory,(TileItemFluidBuffer)te,true,player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileOrderedHopper) {
            TileOrderedHopper containerTileEntity = (TileOrderedHopper) te;
            return new OrderedHopperGUI(containerTileEntity, new OrderedHopperContainer(player.inventory, containerTileEntity));
        }
        if (te instanceof TileItemFluidBuffer) {
            TileItemFluidBuffer containerTileEntity = (TileItemFluidBuffer) te;
            return new ItemFluidBufferGui(containerTileEntity, new ItemFluidBufferContainer(player.inventory, containerTileEntity,false,player));
        }
        return null;
    }
}