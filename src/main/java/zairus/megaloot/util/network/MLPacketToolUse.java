package zairus.megaloot.util.network;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zairus.megaloot.item.MLItems;
import zairus.megaloot.loot.ILootEffectAction;
import zairus.megaloot.loot.LootItemHelper;
import zairus.megaloot.loot.LootWeaponEffect;

public class MLPacketToolUse extends MLPacket
{
	private final Set<Item> tools = Sets.newHashSet(MLItems.TOOL_AXE, MLItems.TOOL_PICKAXE, MLItems.TOOL_SHOVEL);
	private final Set<Item> chestplate = Sets.newHashSet(MLItems.ARMOR_CHESTPLATE);
	public MLPacketToolUse()
	{
		;
	}
	
	@Override
	public void handleServerSide(EntityPlayer player)
	{
		ItemStack tool = player.getHeldItemMainhand();
		
		if (tools.contains(tool.getItem()))
		{
			List<LootWeaponEffect> effects = LootWeaponEffect.getEffectList(tool);
			
			for (LootWeaponEffect effect : effects)
			{
				ILootEffectAction action = effect.getAction();
				
				if (action != null)
				{
					action.toggleAction(player, tool);
					player.sendMessage(action.modificationResponseMessage(player, tool));
				}
			}
		}
		
		if (chestplate.contains(tool.getItem())) {
			if (LootItemHelper.hasEffect(tool, LootWeaponEffect.JETPACK)) {
				
				NBTTagCompound tag = tool.getTagCompound();
                if (tag == null)
                    tag = new NBTTagCompound();
                boolean active = tag.getBoolean("AbilityActive");
                tag.setBoolean("AbilityActive", !active);
                tool.setTagCompound(tag);
                if(!active) {
                player.sendStatusMessage(
                	    new TextComponentString(TextFormatting.GREEN +"Jetpack ON"),
                	    true 
                );
                }else {
                    player.sendStatusMessage(
                    	    new TextComponentString(TextFormatting.RED +"Jetpack OFF"),
                    	    true 
                    );
                }
                
                
			}
		}
		
		
		
	}
}
