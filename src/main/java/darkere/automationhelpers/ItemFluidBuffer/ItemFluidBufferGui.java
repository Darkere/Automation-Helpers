package darkere.automationhelpers.ItemFluidBuffer;

import darkere.automationhelpers.automationhelpers;
import darkere.automationhelpers.network.GuiClosedPacket;
import darkere.automationhelpers.network.Messages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class ItemFluidBufferGui extends GuiContainer {

    public static final int WIDTH = 175;
    public static final int HEIGHT = 172;
    Rectangle tankRec = new Rectangle(16,47);
    private int x;
    private int y;
    private FluidStack fluid;
    private int width;
    private int height;
    private ArrayList<String> tooltip = new ArrayList<>();
    TileItemFluidBuffer tile;
    private final ResourceLocation itemfluidbuffergui  = new ResourceLocation(automationhelpers.MODID,"textures/gui/tank.png");
    public ItemFluidBufferGui(TileItemFluidBuffer tileEntity, ItemFluidBufferContainer container){
        super(container);
        tile = tileEntity;
        xSize = WIDTH;
        ySize = HEIGHT;

    }
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        for(int i = 0;i<tile.getNumberOfTanks();i++){
            drawFluid(guiLeft+26+i*36,guiTop+65-getScaled(i),tile.getFluid(i),16,getScaled(i));
        }
        for(int i = 0;i<tile.getNumberOfTanks();i++){
            tankRec.setLocation(guiLeft+26+i*36,guiTop+18);

            if(tankRec.contains(mouseX,mouseY)&& tile.getFluid(i) != null){
                tooltip.add(tile.getFluid(i).getLocalizedName());
                tooltip.add( (tile.getFluid(i).amount) + " mb");
                drawHoveringText(tooltip,mouseX,mouseY );
                tooltip.clear();
            }
        }



        renderHoveredToolTip(mouseX,mouseY);
    }
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {


    }

    public void drawFluid(int x, int y, FluidStack fluid, int width, int height) {

        if (fluid == null) {
            return;
        }

        GL11.glPushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/atlas/blocks.png"));
        int color = fluid.getFluid().getColor(fluid);
        setGLColorFromInt(color);
        drawTiledTexture(x, y, Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getFluid().getStill(fluid).toString()), width, height);
        GL11.glPopMatrix();
    }
    public static void setGLColorFromInt(int color) {

        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        GlStateManager.color(red, green, blue, 1.0F);
    }
    public void drawTiledTexture(int x, int y, TextureAtlasSprite icon, int width, int height) {

        int i;
        int j;

        int drawHeight;
        int drawWidth;

        for (i = 0; i < width; i += 16) {
            for (j = 0; j < height; j += 16) {
                drawWidth = Math.min(width - i, 16);
                drawHeight = Math.min(height - j, 16);
                drawScaledTexturedModelRectFromIcon(x + i, y + j, icon, drawWidth, drawHeight);
            }
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
    public void drawScaledTexturedModelRectFromIcon(int x, int y, TextureAtlasSprite icon, int width, int height) {

        if (icon == null) {
            return;
        }
        double minU = icon.getMinU();
        double maxU = icon.getMaxU();
        double minV = icon.getMinV();
        double maxV = icon.getMaxV();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, this.zLevel).tex(minU, minV + (maxV - minV) * height / 16F).endVertex();
        buffer.pos(x + width, y + height, this.zLevel).tex(minU + (maxU - minU) * width / 16F, minV + (maxV - minV) * height / 16F).endVertex();
        buffer.pos(x + width, y, this.zLevel).tex(minU + (maxU - minU) * width / 16F, minV).endVertex();
        buffer.pos(x, y, this.zLevel).tex(minU, minV).endVertex();
        Tessellator.getInstance().draw();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
      drawDefaultBackground();
      mc.getTextureManager().bindTexture(itemfluidbuffergui);
      drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
      drawCenteredString(fontRenderer,tile.getBlockType().getLocalizedName(),guiLeft+ (xSize/2),guiTop+5,0xFFFFFFFF);

    }





    @Override
    public void initGui() {
        super.initGui();
    }
    protected int getScaled(int tank) {
        return  Math.max(1,(int)(47* tile.getFluidPercentage(tank)));
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Messages.INSTANCE.sendToServer(new GuiClosedPacket(tile.getPos()));
    }
}
