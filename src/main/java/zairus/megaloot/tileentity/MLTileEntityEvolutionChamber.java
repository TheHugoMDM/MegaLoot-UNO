package zairus.megaloot.tileentity;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import zairus.megaloot.MegaLoot;
import zairus.megaloot.item.MLItems;

public class MLTileEntityEvolutionChamber extends MLTileEntityBase implements ISidedInventory
{
	public static final int TOTAL_EVOLUTION_TIME = 20;
	
	private ItemStack[] chestContents = new ItemStack[4];
	
	private int tick = 0;
	private int evolution_time = 0;
	

	
	private final ItemStackHandler itemHandler = new ItemStackHandler(4)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            markDirty();
        }
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (slot == 0 && (stack.getItem() == MLItems.INFUSED_EMERALD_COMMON
                           || stack.getItem() == MLItems.INFUSED_EMERALD_RARE)) {
            	updateInWorld();
                return super.insertItem(slot, stack, simulate);}

            if (slot == 1 && (stack.getItem() == MLItems.WEAPONCASE_COMMON
                           || stack.getItem() == MLItems.WEAPONCASE_RARE)) {
            	updateInWorld();
                return super.insertItem(slot, stack, simulate);}

            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
        	updateInWorld();
            if (slot == 2 || slot == 3) {
                return super.extractItem(slot, amount, simulate);}
            	
            return ItemStack.EMPTY;
        }
        
    };
	
	
    
    // --- Métodos de ItemStackHandler para contenedor ---
    public ItemStack getStackInSlot(int index)
    {
        return itemHandler.getStackInSlot(index);
    }



    // --- Capabilities para tolvas y otros mods ---
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        return super.getCapability(capability, facing);
    }
    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        return new int[] {0, 1, 2, 3};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        if (index == 0) {
        	if (stack.getItem()==MLItems.INFUSED_EMERALD_COMMON || stack.getItem()==MLItems.INFUSED_EMERALD_COMMON) {
        		return true;
        	}
        }
        if (index == 1) {
        	if (stack.getItem()==MLItems.WEAPONCASE_COMMON || stack.getItem()==MLItems.WEAPONCASE_RARE) {
        		return true;
        	}
        }
        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        return index == 2 || index==3; 
    }

    @Override
    public int getSizeInventory() { return 4; }

    @Override
    public boolean isEmpty()
    {
        for (int i = 0; i < getSizeInventory(); i++)
            if (!getStackInSlot(i).isEmpty()) return false;
        return true;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) { return true; }

    @Override
    public void openInventory(EntityPlayer player) {}
    @Override
    public void closeInventory(EntityPlayer player) {}
    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        ItemStack stack = getStackInSlot(index);
        if (!stack.isEmpty())
        {
            if (stack.getCount() <= count)
            {
                setInventorySlotContents(index, ItemStack.EMPTY);
                return stack;
            }
            ItemStack ret = stack.splitStack(count);
            markDirty();
            return ret;
        }
        return ItemStack.EMPTY;
    }
    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        ItemStack stack = getStackInSlot(index);
        setInventorySlotContents(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void markDirty() { super.markDirty(); }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        itemHandler.setStackInSlot(index, stack);
    }
	
	
	
	
	

	
	public MLTileEntityEvolutionChamber()
	{
		;
	}
	
	private boolean canInfuse()
	{
		ItemStack ingredient = this.getStackInSlot(0);
		ItemStack upgradable = this.getStackInSlot(1);
		
		ItemStack shard = this.getStackInSlot(2);
		ItemStack weaponcase = this.getStackInSlot(3);
		
		if (ingredient.isEmpty() || upgradable.isEmpty())
		{
			MegaLoot.logInfo("empty ingredients");
			return false;
		}
		
		if (!shard.isEmpty() && shard.getCount() >= shard.getMaxStackSize())
		{
			return false;
		}
		
		if (
				ingredient.getItem() == MLItems.INFUSED_EMERALD_COMMON 
				&& upgradable.getItem() == MLItems.WEAPONCASE_COMMON
				&& (shard.isEmpty() || shard.getItem() == MLItems.SHARD_COMMON)
				&& (weaponcase.isEmpty() || weaponcase.getItem() == MLItems.WEAPONCASE_RARE))
		{
			return true;
		}
		
		if (
				ingredient.getItem() == MLItems.INFUSED_EMERALD_RARE 
				&& upgradable.getItem() == MLItems.WEAPONCASE_RARE
				&& (shard.isEmpty() || shard.getItem() == MLItems.SHARD_RARE)
				&& (weaponcase.isEmpty() || weaponcase.getItem() == MLItems.WEAPONCASE_EPIC))
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public void update()
	{
		this.tick = (this.tick + 1) % 2;
		
		evolution_progress:
		if (this.tick == 0)
		{
			ItemStack ingredient = this.getStackInSlot(0);
			ItemStack upgradable = this.getStackInSlot(1);
			
			if (
					ingredient.isEmpty() 
					|| upgradable.isEmpty())
			{
				evolution_time = 0;
				break evolution_progress;
			}
			
			if (canInfuse())
			{
				// Continue 2 minutes to evolve
				++evolution_time;
				
				if (evolution_time > TOTAL_EVOLUTION_TIME)
				{
					int chance = 0;
					
					if (ingredient.hasTagCompound())
					{
						chance = ingredient.getTagCompound().getInteger("evolve_chance");
						
						ItemStack failed = ItemStack.EMPTY;
						ItemStack success = ItemStack.EMPTY;
						
						if (chance > this.world.rand.nextInt(100))
						{
							if (ingredient.getItem() == MLItems.INFUSED_EMERALD_COMMON && upgradable.getItem() == MLItems.WEAPONCASE_COMMON)
								success = new ItemStack(MLItems.WEAPONCASE_RARE);
							else if (ingredient.getItem() == MLItems.INFUSED_EMERALD_RARE && upgradable.getItem() == MLItems.WEAPONCASE_RARE)
								success = new ItemStack(MLItems.WEAPONCASE_EPIC);
						}
						else
						{
							int shardCount = 4 + this.world.rand.nextInt(6);
							
							if (ingredient.getItem() == MLItems.INFUSED_EMERALD_COMMON)
								failed = new ItemStack(MLItems.SHARD_COMMON, shardCount);
							else if (ingredient.getItem() == MLItems.INFUSED_EMERALD_RARE)
								failed = new ItemStack(MLItems.SHARD_RARE, shardCount);
						}
						
						if (!failed.isEmpty())
						{
							ItemStack shard = this.getStackInSlot(2);
							if (shard.isEmpty())
							{
								this.setInventorySlotContents(2, failed.copy());
								ingredient.shrink(1);
								upgradable.shrink(1);
							}
							else if (failed.isItemEqual(shard))
							{
								if (shard.getCount() + failed.getCount() <= shard.getMaxStackSize())
								{
									shard.grow(failed.getCount());
								}
								else
								{
									shard.setCount(shard.getMaxStackSize());
								}
								
								ingredient.shrink(1);
								upgradable.shrink(1);
							}
						}
						else if (!success.isEmpty())
						{
							ItemStack weaponcase = this.getStackInSlot(3);
							if (weaponcase.isEmpty())
							{
								this.setInventorySlotContents(3, success.copy());
								ingredient.shrink(1);
								upgradable.shrink(1);
							}
							else if (success.isItemEqual(weaponcase))
							{
								if (weaponcase.getCount() < weaponcase.getMaxStackSize())
								{
									weaponcase.grow(1);
									ingredient.shrink(1);
									upgradable.shrink(1);
								}
							}
						}
					}
					this.updateInWorld();
					evolution_time = 0;
					break evolution_progress;
				}
			}
			else
			{
				evolution_time = 0;
				break evolution_progress;
			}
			
			this.updateInWorld();
		}
		
		super.update();
	}
	
	private void updateInWorld()
	{
		this.markDirty();
		IBlockState state = this.world.getBlockState(getPos());
		this.world.notifyBlockUpdate(getPos(), state, state, 0);
	}
	
	public int getEvolutionTime()
	{
		return this.evolution_time;
	}
	
	@Override
	public ItemStack[] getChestContents()
	{
		return this.chestContents;
	}
	
	@Override
	public void setChestContents(ItemStack[] contents)
	{
		this.chestContents = contents;
	}
	
	@Override
	public int getSlotXOffset()
	{
		return 0;
	}
	
	@Override
	public int getSlotYOffset()
	{
		return 0;
	}
	
	@Override
	public Slot getSlot(IInventory inv, int index, int x, int y)
	{
		Slot slot = new Slot(inv, index, x, y);
		return slot;
	}
	
	@Override
	public SoundEvent getOpenSound()
	{
		return null;
	}
	
	@Override
	public SoundEvent getCloseSound()
	{
		return null;
	}
	
	@Override
	public SoundEvent getItemPlaceSound()
	{
		return null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ResourceLocation getTextures()
	{
		return null;
	}
	
	@Override
	protected NonNullList<ItemStack> getItems()
	{
		NonNullList<ItemStack> items = NonNullList.<ItemStack>create();
		
		for (ItemStack stack : chestContents)
		{
			items.add((stack == null)? ItemStack.EMPTY : stack);
		}
		
		return items;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		NBTTagCompound c = super.writeToNBT(compound);
		
		compound.setInteger("evolution_time", this.evolution_time);
		
		return c;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		this.evolution_time = compound.getInteger("evolution_time");
		
		super.readFromNBT(compound);
	}
	
	@Override
	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		return new SPacketUpdateTileEntity(this.getPos(), 1, this.getUpdateTag());
	}
	
	@Override
	public NBTTagCompound getUpdateTag()
	{
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt)
	{
		this.readFromNBT(pkt.getNbtCompound());
	}
}
