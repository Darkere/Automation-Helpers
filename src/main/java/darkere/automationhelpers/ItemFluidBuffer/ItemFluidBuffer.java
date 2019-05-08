package darkere.automationhelpers.ItemFluidBuffer;

import darkere.automationhelpers.automationhelpers;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemFluidBuffer extends Block implements ITileEntityProvider {

    public static final ResourceLocation RLITEMFLUIDBUFFER = new ResourceLocation(automationhelpers.MODID,"itemfluidbuffer");
    public static final int GUI_ID = 2;
    private boolean bucket = false;
    public ItemFluidBuffer() {
        super(Material.IRON);
        setRegistryName(RLITEMFLUIDBUFFER);
        setTranslationKey(automationhelpers.MODID+".ItemFluidBuffer");

    }
    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileItemFluidBuffer();
    }
    @SideOnly(Side.CLIENT)
    public void initModels(){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));

    }
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            if (FluidUtil.interactWithFluidHandler(player, hand, world, pos, side)) {
                world.notifyBlockUpdate(pos, state, state, 3);
                bucket = true;
                return true;
            }
            TileEntity te = world.getTileEntity(pos);
            if (!(te instanceof TileItemFluidBuffer)) {
                return false;
            }
            player.openGui(automationhelpers.instance, GUI_ID, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return true;


    }


}