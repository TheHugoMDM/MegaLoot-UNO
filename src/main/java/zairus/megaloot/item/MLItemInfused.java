package zairus.megaloot.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MLItemInfused extends MLItem
{
	protected MLItemInfused()
	{
		this.setMaxStackSize(1);
		this.setMaxDamage(100);
	}
	
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		int evchan=40 + itemRand.nextInt(60);
		if (!stack.getTagCompound().hasKey("evolve_chance"))
			stack.getTagCompound().setInteger("evolve_chance", evchan);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		if (!stack.hasTagCompound())
			return;
		
		int chance = stack.getTagCompound().getInteger("evolve_chance");
		
		tooltip.add("Evolution Chance " + chance + "%");
		stack.setItemDamage((chance*-1)+100);
	}
}
