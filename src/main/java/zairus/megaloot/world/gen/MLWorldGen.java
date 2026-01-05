package zairus.megaloot.world.gen;

import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import zairus.megaloot.item.MLItems;

public class MLWorldGen extends WorldGenerator
{
	
    private static final ResourceLocation STRUCTURE =
            new ResourceLocation("megaloot", "treasures");

    @Override
    public boolean generate(World world, Random rand, BlockPos pos)
    {
        if (world.isRemote) return false;
        //BlockPos placementPos = world.getTopSolidOrLiquidBlock(pos);
       
        TemplateManager manager = world.getSaveHandler().getStructureTemplateManager();
        Template template = manager.getTemplate(world.getMinecraftServer(), STRUCTURE);
        if (template == null) return false;
        PlacementSettings settings = new PlacementSettings()
                .setIgnoreEntities(false)
                .setReplacedBlock(null)
                ;

        template.addBlocksToWorld(world, pos, settings);
        applyLoot(world,pos,rand);
        
        
        return true;
    }
    
    
    
    private void applyLoot(World world, BlockPos origin, Random rand)
    {
        for (BlockPos pos : BlockPos.getAllInBox(origin, origin.add(4, 9, 4)))
        {
            if (world.getTileEntity(pos) instanceof TileEntityChest)
            {
                TileEntityChest chest = (TileEntityChest) world.getTileEntity(pos);
                
                
                chest.setLootTable(
                    new ResourceLocation("minecraft", "chests/simple_dungeon"),
                    rand.nextLong()
                );
                ItemStack special = new ItemStack(MLItems.WEAPONCASE_RARE);
                chest.setInventorySlotContents(13, special);
            }
        }
    }
    
    
}

