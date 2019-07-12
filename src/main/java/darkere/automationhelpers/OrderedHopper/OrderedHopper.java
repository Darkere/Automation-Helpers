package darkere.automationhelpers.OrderedHopper;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class OrderedHopper extends Block implements ITileEntityProvider {

    private static final int GUI_ID = 1;
    public static final ResourceLocation RLOrderedHopper = new ResourceLocation(automationhelpers.MODID, "orderedhopper");

    public OrderedHopper() {
        super(Material.IRON);
        setRegistryName(RLOrderedHopper);
        setTranslationKey(automationhelpers.MODID + ".OrderedHopper");
        setHarvestLevel("pickaxe", 1);
        setHardness(7);

    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        if (worldIn.isRemote) return super.getComparatorInputOverride(blockState, worldIn, pos);
        TileEntity tile = worldIn.getTileEntity(pos);
        return tile instanceof TileOrderedHopper ? ((TileOrderedHopper) tile).getComparatorOutput() : 0;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileOrderedHopper) {
            ((TileOrderedHopper) te).redstoneControl();
            ((TileOrderedHopper) te).setActive();
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileOrderedHopper();
    }

    @SideOnly(Side.CLIENT)
    public void initModels() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));

    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileOrderedHopper) {
            ((TileOrderedHopper) tileentity).dropAllItems();
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        super.dropBlockAsItemWithChance(worldIn, pos, state, 1, fortune);
    }

    @Override // ItemStack heldItem
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side,
                                    float hitX, float hitY, float hitZ) {
        // Only execute on the server
        if (world.isRemote) {
            return true;
        }
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileOrderedHopper)) {
            return false;
        }
        player.openGui(automationhelpers.instance, GUI_ID, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }
}

