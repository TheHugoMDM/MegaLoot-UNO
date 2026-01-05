package zairus.megaloot.world.gen;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import zairus.megaloot.block.MLBlocks;

public class MLWorldGenShardsOre implements IWorldGenerator
{
    @Override
    public void generate(Random rand, int chunkX, int chunkZ, World world,
                         IChunkGenerator generator, IChunkProvider provider)
    {
        if (world.provider.getDimension() == 0) // Overworld
        {
            runGenerator(
                MLBlocks.SHARDS_ORE.getDefaultState(),
                6,   // vetas por chunk
                3,   // tamaño de veta
                1,   // Y mínimo
                14,  // Y máximo
                world, rand, chunkX, chunkZ
            );
        }
    }

    private void runGenerator(IBlockState state, int chance, int size,
                              int minY, int maxY, World world, Random rand,
                              int chunkX, int chunkZ)
    {
        for (int i = 0; i < chance; i++)
        {
            BlockPos pos = new BlockPos(
                chunkX * 16 + rand.nextInt(16),
                minY + rand.nextInt(maxY - minY),
                chunkZ * 16 + rand.nextInt(16)
            );

            new net.minecraft.world.gen.feature.WorldGenMinable(
                state, size, block -> block.getBlock() == Blocks.STONE
            ).generate(world, rand, pos);
        }
    }
}
