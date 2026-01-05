package zairus.megaloot.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zairus.megaloot.MegaLoot;
import zairus.megaloot.item.MLItems;
public class MLBlockShardOre extends Block
{
    public MLBlockShardOre()
    {
        super(Material.ROCK);
        setHardness(3.0F);
        setResistance(5.0F);
        setCreativeTab(MegaLoot.creativeTabMain);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 2); // hierro+
    }

    /** Ítem que suelta */
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return MLItems.SHARD_COMMON; // ítem existente
        
    }
    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world,
                          BlockPos pos, int fortune)
    {
        return 7;
    }
    
    /** Cantidad base */
    @Override
    public int quantityDropped(Random rand)
    {
        return 1;
    }

    /** Soporte para Fortune */
    @Override
    public int quantityDroppedWithBonus(int fortune, Random rand)
    {
        if (fortune > 0)
        {
            int bonus = rand.nextInt(fortune + 1);
            return Math.max(1, quantityDropped(rand) + bonus);
        }
        return quantityDropped(rand);
    }

    /** Si quieres NBT en el drop */
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        ItemStack stack = new ItemStack(MLBlocks.SHARDS_ORE);
        return stack;
    }
}