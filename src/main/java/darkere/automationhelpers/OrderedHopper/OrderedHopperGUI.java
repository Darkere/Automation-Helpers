package darkere.automationhelpers.OrderedHopper;

import darkere.automationhelpers.automationhelpers;
import darkere.automationhelpers.network.Messages;
import darkere.automationhelpers.network.PacketSendButtonPress;
import javafx.util.Pair;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class OrderedHopperGUI extends GuiContainer{
    public static final int WIDTH = 179;
    public static final int HEIGHT = 152;
    private TileOrderedHopper tile;
    private OrderedHopperContainer container;
    HashMap<Integer, ItemStack> lockeditems;
    public ArrayList<Point> slotpositions;
    public int guiLeftFilterSlot;
    public int guiTopFilterSlot;
    GuiButtonExt rememberButton,setButton,redstoneButton;

    private  String rememberButtontext = "Set Filter";
    private int slotposX,slotposY;
    private String setButtonText = "Export as Set";
    private final int rememberButtonID = 0;
    private final int setButtonID = 1;
    private final int redstoneButtonID = 2;
    boolean set;
    private Rmode currentMode = Rmode.roff;

    private static final ResourceLocation background = new ResourceLocation(automationhelpers.MODID, "textures/gui/orderedhopper.png");
    private static final ResourceLocation Redstonecontrol = new ResourceLocation(automationhelpers.MODID, "textures/gui/redstonecontrol.png");

    public OrderedHopperGUI(TileOrderedHopper tileEntity, OrderedHopperContainer container) {
        super(container);
        this.tile = tileEntity;
        this.container = container;
        xSize = WIDTH;
        ySize = HEIGHT;
        set = tile.getSet();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

       mc.getTextureManager().bindTexture(background);

       drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
       drawCenteredString(fontRenderer,tile.getBlockType().getLocalizedName(),guiLeft+ (WIDTH/2),guiTop+5,0xFFFFFFFF);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGhostSlots();


        drawDefaultBackground();
        GlStateManager.color(1,1,1,1);

        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.color(1,1,1,1);


        mc.getTextureManager().bindTexture(Redstonecontrol);

        GlStateManager.enableBlend();
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        drawTexturedModalRect(redstoneButton.x,
                redstoneButton.y,
                currentMode.getX(),
                currentMode.getY()+2, 16, 16);

        renderHoveredToolTip(mouseX, mouseY);

    }
    private void drawGhostSlots(){
        lockeditems = tile.getLockedItems();
        if(lockeditems.isEmpty()) return;
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < tile.SIZE; i++) {
            if (lockeditems.containsKey(i)) {
                mc.getTextureManager().bindTexture(background);
                slotposX = guiLeftFilterSlot + (int) slotpositions.get(i).getX();
                slotposY = guiTopFilterSlot + (int) slotpositions.get(i).getY();

                itemRender.renderItemIntoGUI(lockeditems.get(i),slotposX,slotposY);
                GlStateManager.pushMatrix();
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.disableLighting();
                mc.getTextureManager().bindTexture(background);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.6F);
                drawTexturedModalRect( slotposX, slotposY, (int) slotpositions.get(i).getX(), (int) slotpositions.get(i).getY(), 16, 16);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();
                GlStateManager.enableDepth();
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();


            }
        }
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
    }



    @Override
    protected void actionPerformed(GuiButton button) throws IOException {

        switch (button.id) {
            case rememberButtonID: sendUpdateToServer(true); break;
            case setButtonID: switchSetButtonText(); break;
            case redstoneButtonID: updateRedstoneMode(true);break;

        }
        super.actionPerformed(button);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(mouseButton == 1){
            if(mouseX >= redstoneButton.x && mouseY >= redstoneButton.y && mouseX <= redstoneButton.x + 16&&mouseY <= redstoneButton.y+16){
                updateRedstoneMode(false);
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK,1F));
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);

    }

    private void updateRedstoneMode(boolean left) {
        currentMode = tile.getCurrentMode();
        switch (currentMode){
            case roff:{
                if(left)currentMode = Rmode.ron;
                else currentMode = Rmode.off;
                break;
            }
            case ron:{
                if(left)currentMode = Rmode.on;
                else currentMode = Rmode.roff;
                break;
            }
            case on:{
                if(left)currentMode = Rmode.off;
                else currentMode = Rmode.ron;
                break;
            }
            case off: {
                if (left) currentMode = Rmode.roff;
                else currentMode = Rmode.on;
                break;
            }
        }
        tile.updateRedstoneControl(currentMode);
        sendUpdateToServer(false);
    }

    private void sendUpdateToServer(boolean filter){
        Messages.INSTANCE.sendToServer(new PacketSendButtonPress(filter,currentMode,set));
    }

    private void switchSetButtonText() {
        tile.toggleSet();
        set = tile.getSet();
        setButton.displayString = getSetButtonText(set);
        sendUpdateToServer(false);
    }
    private String getSetButtonText(boolean set){
        if(set){
            return "Export as Set";
        }else {
            return "Export in Order";

        }

    }

    @Override
    public void initGui() {
        super.initGui();
        currentMode = tile.getCurrentMode();


        buttonList.clear();
        lockeditems = tile.getLockedItems();
        slotpositions = container.getSlotpositions();
        guiLeftFilterSlot = guiLeft;
        guiTopFilterSlot = guiTop;
        rememberButton = new GuiButtonExt(rememberButtonID, guiLeft + 9, guiTop + 48,fontRenderer.getStringWidth(rememberButtontext)+6 ,
                16, rememberButtontext);

        setButton = new GuiButtonExt(setButtonID,guiLeft+fontRenderer.getStringWidth(rememberButtontext)+18,
                guiTop+48,fontRenderer.getStringWidth("Export in Order")+6,16,getSetButtonText(tile.getSet()));
        redstoneButton = new GuiButtonExt(redstoneButtonID,guiLeft+fontRenderer.getStringWidth(rememberButtontext)+27 +fontRenderer.getStringWidth("Export in Order"),
                guiTop +48,16,16,"");
        buttonList.add(rememberButton);
        buttonList.add(setButton);
        buttonList.add(redstoneButton);


    }


}