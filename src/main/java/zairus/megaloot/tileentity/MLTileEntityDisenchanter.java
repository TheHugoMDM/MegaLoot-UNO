package zairus.megaloot.tileentity;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zairus.megaloot.MLConstants;
import zairus.megaloot.item.MLItem;
import zairus.megaloot.item.MLItemShard;
import zairus.megaloot.item.MLItems;
import zairus.megaloot.loot.LootItemHelper;
import zairus.megaloot.loot.LootRarity;
import zairus.megaloot.loot.LootSet.LootSetType;
import zairus.megaloot.loot.LootWeaponEffect;
import zairus.megaloot.sound.MLSoundEvents;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.common.capabilities.Capability;
import javax.annotation.Nonnull;


public class MLTileEntityDisenchanter extends MLTileEntityBase implements ISidedInventory
{
	private ItemStack[] chestContents = new ItemStack[3];
	
	private final int disenchantStepDuration = 10;
	private int disenchantCounter = 0;
	private int disenchantStep = 0;
	private boolean disenchaning = false;
	
	private int tick = 0;
	
	private EntityPlayer disenchanterPlayer;
	
	
	
    private final ItemStackHandler itemHandler = new ItemStackHandler(3)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            markDirty();
        }
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            // Tolvas y automatización solo pueden sacar del slot 2
            if (slot != 2) return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if ((slot == 0 && isShard(stack)) || (slot == 1 && isArmorOrTool(stack)))
                return super.insertItem(slot, stack, simulate);

            return stack;
        }

        private boolean isShard(ItemStack stack)
        {
            return stack.getItem() instanceof MLItemShard
                    || stack.getItem() == MLItems.UPGRADECHARM_COMMON
                    || stack.getItem() == MLItems.UPGRADECHARM_RARE
                    || stack.getItem() == MLItems.UPGRADECHARM_EPIC;
        }

        private boolean isArmorOrTool(ItemStack stack)
        {
            return stack.getItem() == MLItems.ARMOR_BOOTS
                    || stack.getItem() == MLItems.ARMOR_CHESTPLATE
                    || stack.getItem() == MLItems.ARMOR_HELMET
                    || stack.getItem() == MLItems.ARMOR_LEGGINGS
                    || stack.getItem() == MLItems.BAUBLERING
                    || stack.getItem() == MLItems.TOOL_AXE
                    || stack.getItem() == MLItems.TOOL_PICKAXE
                    || stack.getItem() == MLItems.TOOL_SHOVEL
                    || stack.getItem() == MLItems.WEAPONBOW
                    || stack.getItem() == MLItems.WEAPONSWORD;
        }
    };
    
    ///aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
    
 // Dentro de MLTileEntityDisenchanter
    public boolean isShard(ItemStack stack) {
        return stack.getItem() instanceof MLItemShard
                || stack.getItem() == MLItems.UPGRADECHARM_COMMON
                || stack.getItem() == MLItems.UPGRADECHARM_RARE
                || stack.getItem() == MLItems.UPGRADECHARM_EPIC;
    }

    public boolean isArmorOrTool(ItemStack stack) {
        return stack.getItem() == MLItems.ARMOR_BOOTS
                || stack.getItem() == MLItems.ARMOR_CHESTPLATE
                || stack.getItem() == MLItems.ARMOR_HELMET
                || stack.getItem() == MLItems.ARMOR_LEGGINGS
                || stack.getItem() == MLItems.BAUBLERING
                || stack.getItem() == MLItems.TOOL_AXE
                || stack.getItem() == MLItems.TOOL_PICKAXE
                || stack.getItem() == MLItems.TOOL_SHOVEL
                || stack.getItem() == MLItems.WEAPONBOW
                || stack.getItem() == MLItems.WEAPONSWORD;
    }    
    
    
    // --- Métodos de ItemStackHandler para contenedor ---
    public ItemStack getStackInSlot(int index)
    {
        return itemHandler.getStackInSlot(index);
    }



    // --- Capabilities para tolvas y otros mods ---
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return true;

        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);

        return super.getCapability(capability, facing);
    }

    // --- ISidedInventory para tolvas ---
    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        if (side == EnumFacing.UP) return new int[] {0};       // tolvas arriba solo insertan shards
        if (side == EnumFacing.DOWN) return new int[] {2};     // tolvas abajo solo extraen slot 2
        return new int[] {1};                                  // tolvas laterales solo insertan armas/armaduras
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        if (index == 0) return direction == EnumFacing.UP && isShard(stack);
        if (index == 1) return direction != EnumFacing.DOWN && isArmorOrTool(stack);
        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        return index == 2 && direction == EnumFacing.DOWN; // solo slot 2 por abajo
    }

    // --- Otros métodos de ISidedInventory ---
    @Override
    public int getSizeInventory() { return 3; }

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
	
	
	
	
	///////////////////////////////
	
	
    private boolean toggleActive = false;

    public boolean isToggleActive() {
		IBlockState state = this.world.getBlockState(getPos());
		this.world.notifyBlockUpdate(getPos(), state, state, 0);    	
        return toggleActive;
    }
    public boolean toggle() {
        this.toggleActive = !this.toggleActive;
        markDirty(); // Marca el TileEntity para sincronización
		IBlockState state = this.world.getBlockState(getPos());
		this.world.notifyBlockUpdate(getPos(), state, state, 0);
        return this.toggleActive;
    }
	
	
	
	
	
	/////////////////////////////////
	
	public MLTileEntityDisenchanter()
	{
		;
	}
	
	
	@Override
	public void update()
	{
		
		if (this.disenchaning)
		{
			disenchant();
			
			this.tick = (this.tick + 1) % 20;
			
			if (this.tick == 0)
			{
				IBlockState state = this.world.getBlockState(getPos());
				this.world.notifyBlockUpdate(getPos(), state, state, 0);
			}
		}else {
			if (isToggleActive()) {
				disenchantStart();
			}
		}
		
		super.update();
	}
	
	public void setDisenchanterPlayer(EntityPlayer disenchanter)
	{
		this.disenchanterPlayer = disenchanter;
	}
	
	public int getDisenchantStep()
	{
		return this.disenchantStep;
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
	
	public void applyUpgrade(ItemStack upgrade, ItemStack tool)
	{
		MLItemShard upgradeItem = (MLItemShard)upgrade.getItem();
		
		LootRarity upgradeRarity = upgradeItem.getShardRariry();
		LootRarity toolRarity = LootRarity.fromId(LootItemHelper.getLootStringValue(tool, MLItem.LOOT_TAG_RARITY));
		
		if (toolRarity == null || !(toolRarity == upgradeRarity)) {
			return;
		}
		
		int upgrades = LootItemHelper.getLootIntValue(tool, MLItem.LOOT_TAG_UPGRADES);
		
		if (upgrades <= 0) 
			return;
	
		
		
		List<LootWeaponEffect> effects = LootWeaponEffect.getEffectList(tool);
		LootSetType type = MLItems.getItemType(tool.getItem());
		
		if (!effects.contains(LootWeaponEffect.ARMOR)) 
			effects.add(LootWeaponEffect.ARMOR);
		
		if (!effects.contains(LootWeaponEffect.TOUGHNESS)) 
			effects.add(LootWeaponEffect.TOUGHNESS);
		
		LootWeaponEffect me = LootWeaponEffect.getRandomExcluding(this.world.rand, type, effects);
		
		
//////////////////////
		NBTTagList effectList = LootItemHelper.getLootTagList(tool, MLItem.LOOT_TAG_EFFECTLIST);
		boolean downgrade=false;
		
		try {
		int name = Integer.parseInt(upgrade.getDisplayName())-1;
		int count= effectList.tagCount();
		//REMOVE EFFECTS USING CHARM'S NAME 
		if (name>=0 && name+1<=count) {
			effectList.removeTag(name);
			downgrade=true;
		}
		}catch(NumberFormatException e){
			downgrade=false;
		}
		
		
		if (me != null || downgrade==true)
		{
			
			

			if(downgrade==false) {
				effectList.appendTag(me.getNBT(this.world.rand));
				if (me == LootWeaponEffect.LIFE_LONG)
					tool.getTagCompound().setBoolean("Unbreakable", true);
			}
			
			LootItemHelper.setLootTagList(tool, MLItem.LOOT_TAG_EFFECTLIST, effectList);
			
			--upgrades;
			upgrade.shrink(1);
			
			if (upgrades < 0)
				upgrades = 0;
			
			LootItemHelper.setLootIntValue(tool, MLItem.LOOT_TAG_UPGRADES, upgrades);
			
			this.world.playSound(
					(EntityPlayer)null
					, this.getPos().getX()
					, this.getPos().getY()
					, this.getPos().getZ()
					, MLSoundEvents.TOOL_REPAIR
					, SoundCategory.BLOCKS
					, 0.5F
					, this.world.rand.nextFloat() * 0.1F + 0.9F);
		}else {
			//if theres no more upgrades, just upgrade a tool stat
			
			
			if (tool.getItem()==MLItems.WEAPONSWORD) {
				int newVal=LootItemHelper.getLootIntValue(tool, MLItem.LOOT_TAG_DAMAGE)+((int)(Math.random() * 2) + 1);
				LootItemHelper.setLootIntValue(tool, MLItem.LOOT_TAG_DAMAGE,newVal );
			}else if(tool.getItem()==MLItems.TOOL_AXE){
				int newVal=LootItemHelper.getLootIntValue(tool, MLItem.LOOT_TAG_EFFICIENCY)+((int)(Math.random() * 2) + 1);
				LootItemHelper.setLootIntValue(tool, MLItem.LOOT_TAG_EFFICIENCY,newVal );
			}else if(tool.getItem()==MLItems.TOOL_PICKAXE){
				int newVal=LootItemHelper.getLootIntValue(tool, MLItem.LOOT_TAG_EFFICIENCY)+((int)(Math.random() * 2) + 1);
				LootItemHelper.setLootIntValue(tool, MLItem.LOOT_TAG_EFFICIENCY,newVal );
			}else if(tool.getItem()==MLItems.TOOL_SHOVEL){
				int newVal=LootItemHelper.getLootIntValue(tool, MLItem.LOOT_TAG_EFFICIENCY)+((int)(Math.random() * 2) + 1);
				LootItemHelper.setLootIntValue(tool, MLItem.LOOT_TAG_EFFICIENCY,newVal );
			}else if(tool.getItem()==MLItems.WEAPONBOW){
				int newVal=LootItemHelper.getLootIntValue(tool, MLItem.LOOT_TAG_POWER)+((int)(Math.random() * 2) + 1);
				LootItemHelper.setLootIntValue(tool, MLItem.LOOT_TAG_POWER,newVal );
			}else if(tool.getItem()==MLItems.ARMOR_BOOTS){
				int newVal=LootItemHelper.getLootIntValue(tool, MLItem.LOOT_TAG_ARMOR)+((int)(Math.random() * 2) + 1);
				LootItemHelper.setLootIntValue(tool, MLItem.LOOT_TAG_ARMOR,newVal );
			}else if(tool.getItem()==MLItems.ARMOR_LEGGINGS){
				int newVal=LootItemHelper.getLootIntValue(tool, MLItem.LOOT_TAG_ARMOR)+((int)(Math.random() * 2) + 1);
				LootItemHelper.setLootIntValue(tool, MLItem.LOOT_TAG_ARMOR,newVal );
			}else if(tool.getItem()==MLItems.ARMOR_CHESTPLATE){
				int newVal=LootItemHelper.getLootIntValue(tool, MLItem.LOOT_TAG_ARMOR)+((int)(Math.random() * 2) + 1);
				LootItemHelper.setLootIntValue(tool, MLItem.LOOT_TAG_ARMOR,newVal );
			}else if(tool.getItem()==MLItems.ARMOR_HELMET){
				int newVal=LootItemHelper.getLootIntValue(tool, MLItem.LOOT_TAG_ARMOR)+((int)(Math.random() * 2) + 1);
				LootItemHelper.setLootIntValue(tool, MLItem.LOOT_TAG_ARMOR,newVal );
			}
			
			if(tool.getItem()!=MLItems.BAUBLERING){
				
				--upgrades;
				upgrade.shrink(1);
				
				if (upgrades < 0)
					upgrades = 0;
				
				LootItemHelper.setLootIntValue(tool, MLItem.LOOT_TAG_UPGRADES, upgrades);
				
				this.world.playSound(
						(EntityPlayer)null
						, this.getPos().getX()
						, this.getPos().getY()
						, this.getPos().getZ()
						, MLSoundEvents.TOOL_REPAIR
						, SoundCategory.BLOCKS
						, 0.5F
						, this.world.rand.nextFloat() * 0.1F + 0.9F);
			}
		}
		
		
		
		
	}
	
	public void applyRepair()
	{
		ItemStack material = this.getStackInSlot(0);
		ItemStack toRepair = this.getStackInSlot(1);
		
		if (material == null || material == ItemStack.EMPTY || material.getCount() == 0 || toRepair == null || toRepair == ItemStack.EMPTY || toRepair.getCount() == 0)
			return;
		
		if (
				material.getItem() == MLItems.UPGRADECHARM_COMMON
				|| material.getItem() == MLItems.UPGRADECHARM_RARE
				|| material.getItem() == MLItems.UPGRADECHARM_EPIC)
		{
			applyUpgrade(material, toRepair);
			return;
		}
		
		if (toRepair.getItemDamage() == 0)
			return;
		
		int newDamage = toRepair.getItemDamage() - (int)((float)toRepair.getMaxDamage() * 0.33);
		if (newDamage < 0)
			newDamage = 0;
		
		if (material.getItem() instanceof MLItemShard)
		{
			LootRarity materialRarity = ((MLItemShard)material.getItem()).getShardRariry();
			LootRarity toolRarity = LootRarity.fromId(LootItemHelper.getLootStringValue(toRepair, MLItem.LOOT_TAG_RARITY));
			
			if (materialRarity == null || toolRarity == null)
				return;
			
			if (materialRarity == toolRarity)
			{
				material.shrink(1);
				toRepair.setItemDamage(newDamage);
				
				this.world.playSound(
						(EntityPlayer)null
						, this.getPos().getX()
						, this.getPos().getY()
						, this.getPos().getZ()
						, MLSoundEvents.TOOL_REPAIR
						, SoundCategory.BLOCKS
						, 0.5F
						, this.world.rand.nextFloat() * 0.1F + 0.9F);
			}
		}
	}
	
	public void disenchantStart()
	{
		if (this.disenchaning)
			return;
		
		ItemStack tool = this.getStackInSlot(1);
		
		if (tool == null || tool == ItemStack.EMPTY || tool.getCount() == 0)
			return;
		
		this.disenchantCounter = 0;
		this.disenchantStep = 1;
		this.disenchaning = true;
	}
	
	public void disenchantStop()
	{
		this.disenchaning = false;
		this.disenchantCounter = 0;
		this.disenchantStep = 0;
	}
	
	private void disenchant()
	{
		ItemStack tool = this.getStackInSlot(1);
		
		if (tool == null || tool == ItemStack.EMPTY || tool.getCount() == 0)
		{
			disenchantStop();
			return;
		}
		
		++this.disenchantCounter;
		
		if (this.disenchantCounter >= this.disenchantStepDuration)
		{
			this.disenchantCounter = 0;
			++this.disenchantStep;
			
			this.world.playSound(
					(EntityPlayer)null
					, this.getPos().getX()
					, this.getPos().getY()
					, this.getPos().getZ()
					, MLSoundEvents.TOOL_BREAK
					, SoundCategory.BLOCKS
					, 0.5F
					, this.world.rand.nextFloat() * 0.1F + 0.9F);
			
			if (this.disenchantStep > 3)
			{
				ItemStack result = this.getStackInSlot(2);
				
				if (result == null)
					result = ItemStack.EMPTY;
				
				int model = LootItemHelper.getLootIntValue(tool, MLItem.LOOT_TAG_MODEL);
				
				LootRarity toolRarity = LootRarity.fromId(LootItemHelper.getLootStringValue(tool, MLItem.LOOT_TAG_RARITY));
				int shardCount = 3 + this.world.rand.nextInt(2);
				
				Item shardItem = MLItems.SHARD_COMMON;
				
				if (toolRarity == LootRarity.RARE)
					shardItem = MLItems.SHARD_RARE;
				
				if (toolRarity == LootRarity.EPIC)
					shardItem = MLItems.SHARD_EPIC;
				
				ItemStack shards = new ItemStack(shardItem, shardCount);
				
				if (!result.isEmpty() && !result.isItemEqual(shards))
				{
					disenchantStop();
					return;
				}
				
				if (shards.getItem() == result.getItem())
				{
					shardCount += result.getCount();
					
					if (shardCount > result.getMaxStackSize())
					{
						disenchantStop();
						return;
					}
					
					result.setCount(shardCount);
				}
				else
				{
					result = shards;
				}
				
				if (disenchanterPlayer != null)
				{
					NBTTagCompound playerTag = disenchanterPlayer.getEntityData();
					
					if (playerTag != null)
					{
						NBTTagCompound playerLootTag = (playerTag.hasKey(MLConstants.MOD_ID)) ? playerTag.getCompoundTag(MLConstants.MOD_ID) : new NBTTagCompound();
						
						NBTTagCompound skinTag = (playerLootTag.hasKey(MLItem.LOOT_TAG_SKIN_LIST)) ? playerLootTag.getCompoundTag(MLItem.LOOT_TAG_SKIN_LIST) : new NBTTagCompound();
						
						LootSetType type = MLItems.getItemType(tool.getItem());
						
						String classKey = type.getId();
						
						if (classKey == "tool")
						{
							if (tool.getItem() instanceof ItemPickaxe)
								classKey = "pickaxe";
							if (tool.getItem() instanceof ItemAxe)
								classKey = "axe";
							if (tool.getItem() instanceof ItemSpade)
								classKey = "shovel";
						}
						
						int[] modelList = skinTag.getIntArray(classKey);
						
						if (modelList == null || modelList.length == 0)
						{
							modelList = new int[] { model };
						}
						else
						{
							List<Integer> ml = new ArrayList<Integer>();
							
							for (int m : modelList)
								ml.add(m);
							
							if (!ml.contains(model))
								ml.add(model);
							
							modelList = ml.stream().mapToInt(i->i).toArray();
						}
						
						skinTag.setIntArray(classKey, modelList);
						
						playerLootTag.setTag(MLItem.LOOT_TAG_SKIN_LIST, skinTag);
						playerTag.setTag(MLConstants.MOD_ID, playerLootTag);
					}
				}
				
				this.setInventorySlotContents(1, ItemStack.EMPTY);
				this.setInventorySlotContents(2, result);
				disenchantStop();
			}
			
			this.markDirty();
			
			
			
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		NBTTagCompound c = super.writeToNBT(compound);
		
		c.setInteger("disenchantCounter", this.disenchantCounter);
		c.setInteger("disenchantStep", this.disenchantStep);
		c.setBoolean("disenchaning", this.disenchaning);
		c.setBoolean("ToggleActive", this.toggleActive);
		
		NBTTagList itemList = new NBTTagList();
	    for (int i = 0; i < this.getSizeInventory(); i++) {
	        ItemStack stack = this.getStackInSlot(i);
	        if (stack != null && !stack.isEmpty()) {
	            NBTTagCompound itemTag = new NBTTagCompound();
	            itemTag.setByte("Slot", (byte) i);
	            stack.writeToNBT(itemTag);
	            itemList.appendTag(itemTag);
	        }
	    }
	    compound.setTag("Items", itemList);
	    
		return c;
	}
	
	
	
	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		
		this.disenchantCounter = compound.getInteger("disenchantCounter");
		this.disenchantStep = compound.getInteger("disenchantStep");
		this.disenchaning = compound.getBoolean("disenchaning");
		this.toggleActive = compound.getBoolean("ToggleActive");
		
		NBTTagList itemList = compound.getTagList("Items", 10); // 10 = NBT tipo compound
	    for (int i = 0; i < itemList.tagCount(); i++) {
	        NBTTagCompound itemTag = itemList.getCompoundTagAt(i);
	        int slot = itemTag.getByte("Slot") & 255;
	        if (slot >= 0 && slot < this.getSizeInventory()) {
	            this.setInventorySlotContents(slot, new ItemStack(itemTag));
	        }
	    }
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
