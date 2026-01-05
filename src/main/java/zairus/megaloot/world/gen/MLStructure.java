package zairus.megaloot.world.gen;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

public class MLStructure implements IWorldGenerator
{
    private final MLWorldGen structure = new MLWorldGen();

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			net.minecraft.world.gen.IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        // Solo Overworld
        if (world.provider.getDimension() != 0) return;
        if (random.nextInt(5) != 0) return;
        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);

        int startY = 95 + random.nextInt(30); // opcional: variación
        BlockPos start = new BlockPos(x, startY, z);
        
        if(!isValidBase(world, start)) {
        	return;
        }
        int newY=start.getY()-3;
        start= new BlockPos(x, newY, z);
        structure.generate(world, random, start);
		
	}
	
	
	private boolean isValidBase(World world, BlockPos center)
	{
	    // La estructura es 5x5 > radio = 2
	    int radius = 2;

	    for (int x = -radius; x <= radius; x++)
	    {
	        for (int z = -radius; z <= radius; z++)
	        {
	            BlockPos checkPos = center.add(x, -1, z);
	            IBlockState state = world.getBlockState(checkPos);

	            // Si algún bloque no es sólido > no es válido
	            if (!state.isOpaqueCube() || !state.getMaterial().isSolid())
	            {
	                return false;
	            }
	        }
	    }

	    return true;
	}
	
	
	
}
