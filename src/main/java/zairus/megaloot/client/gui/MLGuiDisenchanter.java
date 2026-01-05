package zairus.megaloot.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zairus.megaloot.MLConstants;
import zairus.megaloot.MegaLoot;
import zairus.megaloot.inventory.MLContainerDisenchanter;
import zairus.megaloot.tileentity.MLTileEntityDisenchanter;
import zairus.megaloot.util.network.MLPacketToolRepair;

@SideOnly(Side.CLIENT)
public class MLGuiDisenchanter extends GuiContainer
{
	public static final ResourceLocation GUI_BACKGROUND = new ResourceLocation(MLConstants.MOD_ID, "textures/gui/container/disenchanter.png");
	
	private final int BUTTON_REPAIR_ID = 0;
	private final int BUTTON_DISENCHANT_ID = 1;
	private final int BUTTON_MODE = 2;
	
	private MLGuiButton buttonRepair;
	private MLGuiButton buttonDisenchant;
	private MLGuiButton buttonMode;
	
	private IInventory inventory;
	

	public MLGuiDisenchanter(IInventory playerInv, IInventory inventorySlots, EntityPlayer player)
	{
		super(new MLContainerDisenchanter(playerInv, inventorySlots, player));
		this.inventory = inventorySlots;
	}
	
	public boolean toggleState=false;
	
	public void getMode() {
		if (this.inventory instanceof MLTileEntityDisenchanter) {
		    MLTileEntityDisenchanter tile = (MLTileEntityDisenchanter) this.inventory;
		    tile.markDirty();
		    tile = (MLTileEntityDisenchanter) this.inventory;
		    toggleState = tile.isToggleActive();
		}
	}
	
	
	
	@Override
	public void initGui()
	{
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		
		this.buttonList.add(buttonRepair = new MLGuiButton(this.BUTTON_REPAIR_ID, 0, i + 25, j + 15, 18, 18, "", GUI_BACKGROUND));
		this.buttonList.add(buttonDisenchant = new MLGuiButton(this.BUTTON_DISENCHANT_ID, 1, i + 133, j + 62, 18, 18, "", GUI_BACKGROUND));
		
		
		this.buttonRepair.tooltip.add("Apply repair / upgrade");
		
		this.buttonRepair.tooltipExtended.add("Use shards of same type to repair.");
		this.buttonRepair.tooltipExtended.add("Use upgrade charm of same type to");
		this.buttonRepair.tooltipExtended.add("add an extra attribute.");
		
		this.buttonDisenchant.tooltip.add("Disenchant/Recycle.");
		
		this.buttonDisenchant.tooltipExtended.add("Will destory the tool");
		this.buttonDisenchant.tooltipExtended.add("to shards.");
		
		
		
		getMode();
        boolean state = toggleState;
		int st = 0;
		if (state) {st=1;
			this.drawTexturedModalRect(i+90, j+40, 0, 203, 30, 8);
		
		}else {st=0;}
		
	    // Botón con id 0, posición X=10, Y=10, ancho=20, alto=20, texto inicial "OFF"
		this.buttonList.add(buttonMode= new MLGuiButton(this.BUTTON_MODE, 2+st,  i+133, j+15, 18, 18, "", GUI_BACKGROUND));
		this.buttonMode.tooltip.add("Auto-recycle");
		
		
		
		super.initGui();
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
	{
		switch (button.id)
		{
		case BUTTON_REPAIR_ID:
			if (this.inventory instanceof MLTileEntityDisenchanter)
			{
				MLTileEntityDisenchanter d = (MLTileEntityDisenchanter)this.inventory;
				
				MegaLoot.packetPipeline.sendToServer(new MLPacketToolRepair(d.getPos().getX(), d.getPos().getY(), d.getPos().getZ(), 0));
			}
			break;
		case BUTTON_DISENCHANT_ID:
			if (this.inventory instanceof MLTileEntityDisenchanter)
			{
				MLTileEntityDisenchanter d = (MLTileEntityDisenchanter)this.inventory;
				
				MegaLoot.packetPipeline.sendToServer(new MLPacketToolRepair(d.getPos().getX(), d.getPos().getY(), d.getPos().getZ(), 1));
			}
			break;
	    case BUTTON_MODE:
	        if (this.inventory instanceof MLTileEntityDisenchanter) {
				MLTileEntityDisenchanter d = (MLTileEntityDisenchanter)this.inventory;

				getMode();
		        boolean state = toggleState;
				int st = 0;
				if (state) {st=1;}else {st=0;}
				this.buttonMode.icon=2+st;
				
				MegaLoot.packetPipeline.sendToServer(new MLPacketToolRepair(d.getPos().getX(), d.getPos().getY(), d.getPos().getZ(), 2));
	        }
	        break;
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}
	
	@Override
	public void updateScreen()
	{
		getMode();
        boolean state = toggleState;
		int st = 0;
		if (state) {st=1;}else {st=0;}
		this.buttonMode.icon=2+st;
		
		
		super.updateScreen();
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		this.fontRenderer.drawString(I18n.format("container.disenchanter.title"), 7, 4, 4210752);
		
		int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        
        for (GuiButton b : this.buttonList)
        {
        	if (b instanceof MLGuiButton)
        	{
        		MLGuiButton db = (MLGuiButton)b;
        		
        		if (mouseX > db.x && mouseX < db.x + db.width && mouseY > db.y && mouseY < db.y + db.height)
        		{
        			this.drawHoveringText(db.getToolTip(isShiftKeyDown()), mouseX - i, mouseY - j +10);
        		}
        	}
        }
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(GUI_BACKGROUND);
		this.ySize = 166;
		
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		
		this.setButtonPositions(i, j);
		
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
		
		
		getMode();
        boolean state = toggleState;
		if (state) {
			this.drawTexturedModalRect(i+72, j+29, 0, 203, 30, 8);
		}
		
		
		if (this.inventory instanceof MLTileEntityDisenchanter)
		{
			MLTileEntityDisenchanter d = (MLTileEntityDisenchanter)this.inventory;
			
			this.drawTexturedModalRect(i + 82, j + 58, 176, (20 * d.getDisenchantStep()), 29, 20);
		}
	}
	
	private void setButtonPositions(int left, int top)
	{
		;
	}
}
