package zairus.megaloot.loot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import zairus.megaloot.item.MLItems;

public class LootRarity
{
	private static final Map<String, LootRarity> REGISTRY = new HashMap<String, LootRarity>();
	
	public static final LootRarity COMMON = 
			get("Common", TextFormatting.WHITE, MLItems.SHARD_COMMON)
			.setDamage(1, 3)
			.setSpeed(-3.1F, -2.399F)
			.setArmor(0, 1)
			.setToughness(0, 0)
			.setEfficiency(5.0F, 12.0F)
			.setDurability(100, 550)
			.setUpgrades(2, 4);
	
	public static final LootRarity RARE = 
			get("Rare", TextFormatting.YELLOW, MLItems.SHARD_RARE)
			.setDamage(2, 6)
			.setSpeed(-2.8F, -2.3F)
			.setArmor(1, 3)
			.setToughness(0, 2)
			.setEfficiency(10.0F, 25.0F)
			.setDurability(350, 1450)
			.setModifierCount(0, 3)
			.setUpgrades(2, 5);
	
	public static final LootRarity EPIC = 
			get("Epic", TextFormatting.LIGHT_PURPLE, MLItems.SHARD_EPIC)
			.setDamage(5, 13)
			.setSpeed(-2.69999F, -2.1F)
			.setArmor(2, 5)
			.setToughness(1, 5)
			.setEfficiency(15.0F, 50.0F)
			.setDurability(850, 3500)
			.setModifierCount(1, 4)
			.setUpgrades(3, 6);
	
	private final Item shardItem;
	
	private TextFormatting color = TextFormatting.WHITE;
	private int damageMin = 0;
	private int damageMax = 7;
	private float speedMin = 0.0F;
	private float speedMax = 1.0F;
	private float armorMin = 3.0F;
	private float armorMax = 10.0F;
	private float toughnessMin = 3.0F;
	private float tougnessMax = 10.0F;
	private float efficiencyMin = 1.0F;
	private float efficiencyMax = 1.0F;
	private int durabilityMin = 0;
	private int durabilityMax = 0;
	private int modifiersMin = 0;
	private int modifiersMax = 1;
	private int upgradesMin = 1;
	private int upgradesMax = 10;
	private String id;
	
	private LootRarity(Item shard)
	{
		this.shardItem = shard;
	}
	
	public Item getShardItem()
	{
		return this.shardItem;
	}
	
	public TextFormatting getColor()
	{
		return this.color;
	}
	
	public String getId()
	{
		return this.id;
	}
	
	@Nullable
	public static LootRarity fromId(String id)
	{
		LootRarity r = null;
		
		if (REGISTRY.containsKey(id))
		{
			r = REGISTRY.get(id);
		}
		
		return r;
	}
	
	protected LootRarity setModifierCount(int min, int max)
	{
		this.modifiersMin = min;
		this.modifiersMax = max;
		return this;
	}
	
	protected LootRarity setDurability(int min, int max)
	{
		this.durabilityMin = min;
		this.durabilityMax = max;
		return this;
	}
	
	protected LootRarity setDamage(int min, int max)
	{
		this.damageMin = min;
		this.damageMax = max;
		return this;
	}
	
	protected LootRarity setSpeed(float min, float max)
	{
		this.speedMin = min;
		this.speedMax = max;
		return this;
	}
	
	protected LootRarity setArmor(float min, float max)
	{
		this.armorMin = min;
		this.armorMax = max;
		return this;
	}
	
	protected LootRarity setToughness(float min, float max)
	{
		this.toughnessMin = min;
		this.tougnessMax = max;
		return this;
	}
	
	protected LootRarity setEfficiency(float min, float max)
	{
		this.efficiencyMin = min;
		this.efficiencyMax = max;
		return this;
	}
	
	protected LootRarity setUpgrades(int min, int max)
	{
		this.upgradesMin = min;
		this.upgradesMax = max;
		return this;
	}
	
	public int getModifierCount(Random rand)
	{
		int modifierCount = this.modifiersMin;
		
		if (modifierCount < this.modifiersMax)
			modifierCount += rand.nextInt(this.modifiersMax - modifierCount + 1);
		
		return modifierCount;
	}
	

	
	public int getDamage(Random rand)
	{
	    int base = damageMin;
	    if (base < damageMax)
	        base += rand.nextInt(damageMax - base + 1);
	    // Escala: mientras mayor, más fácil obtener valores altos
	    double scale = 1.5;
	    int extra = (int)(-Math.log(1 - rand.nextDouble()) * scale);
	    return base + extra;
	}
	
	private int rollWithSoftCapInt(Random rand, int min, int max, float decay)
	{
	    int value = min + rand.nextInt(max - min + 1);

	    float chance = decay; // ej: 0.25f
	    while (rand.nextFloat() < chance)
	    {
	        value++;
	        chance *= decay; // cada punto extra es más raro
	    }

	    return value;
	}
	private float rollWithSoftCapFloat(Random rand, float min, float max, float decay)
	{
	    float value = min + (max - min) * rand.nextFloat();

	    float chance = decay;
	    float step = (max - min) * 1f; // tamaño del incremento extra

	    while (rand.nextFloat() < chance)
	    {
	        value += step;
	        chance *= decay;
	    }

	    return value;
	}
	
	
	public int getDurability(Random rand)
	{
	    return rollWithSoftCapInt(rand, durabilityMin, durabilityMax, 0.25f);
	}
	public float getSpeed(Random rand)
	{
	    float speed = rollWithSoftCapFloat(rand, speedMin, speedMax, 0.25f);
	    return Math.round(speed * 100.0F) / 100.0F;
	}
	public float getArmor(Random rand)
	{
	    float armor = rollWithSoftCapFloat(rand, armorMin, armorMax, 0.25f);
	    return Math.round(armor * 100.0F) / 100.0F;
	}
	public float getToughness(Random rand)
	{
	    float toughness = rollWithSoftCapFloat(rand, toughnessMin, tougnessMax, 0.25f);
	    return Math.round(toughness * 100.0F) / 100.0F;
	}
	public float getEfficiency(Random rand)
	{
	    float efficiency = rollWithSoftCapFloat(rand, efficiencyMin, efficiencyMax, 0.25f);
	    return Math.round(efficiency * 100.0F) / 100.0F;
	}
	public int getUpgrades(Random rand)
	{
	    return rollWithSoftCapInt(rand, upgradesMin, upgradesMax, 0.20f);
	}
	
	protected static LootRarity get(String id, TextFormatting color, Item shard)
	{
		LootRarity r = new LootRarity(shard);
		
		r.id = id;
		r.color = color;
		
		REGISTRY.put(id, r);
		
		return r;
	}
}
