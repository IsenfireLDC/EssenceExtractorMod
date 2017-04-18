package gamedev.eem.item.crafting;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MachineRecipes {
	
    private static final MachineRecipes SMELTING_BASE = new MachineRecipes();
    private final Map<ItemStack, ItemStack> processingList = Maps.<ItemStack, ItemStack>newHashMap();
    private final Map<ItemStack, Float> experienceList = Maps.<ItemStack, Float>newHashMap();
    private final Table<ItemStack, Integer, Integer> quantitiesList = HashBasedTable.<ItemStack, Integer, Integer>create();
    private final Map<ItemStack, Integer> timeList = Maps.<ItemStack, Integer>newHashMap();
    private int quantityIn;
    private int quantityOut;
    private final int defaultRuntime = 250;
    
    public static MachineRecipes instance()
    {
        return SMELTING_BASE;
    }
    
    private MachineRecipes () {
    	//Essence of Poison
    	this.addProcessing(Items.POISONOUS_POTATO, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1); 
    	this.addProcessing(Items.SPIDER_EYE, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	//Essence of Hunger
    	this.addProcessing(Items.FERMENTED_SPIDER_EYE, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	this.addProcessing(Items.ROTTEN_FLESH, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	//Essence of Fire
    	this.addProcessingRecipeForBlock(Blocks.COAL_BLOCK, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 2, 5);
    	this.addProcessing(Items.COAL, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	this.addProcessing(Items.LAVA_BUCKET, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	//Essence of Nether Fire
    	this.addProcessing(Items.MAGMA_CREAM, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	this.addProcessing(Items.BLAZE_ROD, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	this.addProcessing(Items.BLAZE_POWDER, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	//Essence of Thorns
    	this.addProcessingRecipeForBlock(Blocks.CACTUS, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	//Essence of Light
    	this.addProcessingRecipeForBlock(Blocks.TORCH, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	this.addProcessingRecipeForBlock(Blocks.LIT_PUMPKIN, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	this.addProcessing(Items.GLOWSTONE_DUST, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	this.addProcessingRecipeForBlock(Blocks.GLOWSTONE, new ItemStack(Items.EXPERIENCE_BOTTLE), 1.0F, 1, 1);
    	
    }
    
    /**
     * Adds a smelting recipe, where the input item is an instance of Block.
     */
    public void addProcessingRecipeForBlock(Block input, ItemStack stack, float experience, int quantityIn, int quantityOut, int runtime)
    {
        this.addProcessing(Item.getItemFromBlock(input), stack, experience, quantityIn, quantityOut, runtime);
    }
    
    public void addProcessingRecipeForBlock(Block input, ItemStack stack, float experience, int quantityIn, int quantityOut) {
    	
    	this.addProcessing(Item.getItemFromBlock(input), stack, experience, quantityIn, quantityOut, defaultRuntime);
    	
    }

    /**
     * Adds a smelting recipe using an Item as the input item.
     */
    public void addProcessing(Item input, ItemStack stack, float experience, int quantityIn, int quantityOut, int runtime)
    {
        this.addProcessingRecipe(new ItemStack(input, 1, 32767), stack, experience, quantityIn, quantityOut, runtime);
    }
    
    public void addProcessing(Item input, ItemStack stack, float experience, int quantityIn, int quantityOut) {
    	
    	this.addProcessingRecipe(new ItemStack(input, 1, 32767), stack, experience, quantityIn, quantityOut, defaultRuntime);
    	
    }

    /**
     * Adds a smelting recipe using an ItemStack as the input for the recipe.
     */
    public void addProcessingRecipe(ItemStack input, ItemStack stack, float experience, int quantityIn, int quantityOut, int runtime)
    {
        if (getProcessingResult(input) != ItemStack.EMPTY) { net.minecraftforge.fml.common.FMLLog.info("Ignored smelting recipe with conflicting input: " + input + " = " + stack); return; }
        this.processingList.put(input, stack);
        this.experienceList.put(stack, Float.valueOf(experience));
        this.quantitiesList.put(stack, quantityIn, quantityOut);
        this.timeList.put(stack, runtime);
    }

    /**
     * Returns the smelting result of an item.
     */
    public ItemStack getProcessingResult(ItemStack stack)
    {
        for (Entry<ItemStack, ItemStack> entry : this.processingList.entrySet())
        {
            if (this.compareItemStacks(stack, (ItemStack)entry.getKey()))
            {
                return (ItemStack)entry.getValue();
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Compares two itemstacks to ensure that they are the same. This checks both the item and the metadata of the item.
     */
    private boolean compareItemStacks(ItemStack stack1, ItemStack stack2)
    {
        return stack2.getItem() == stack1.getItem() && (stack2.getMetadata() == 32767 || stack2.getMetadata() == stack1.getMetadata());
    }

    public Map<ItemStack, ItemStack> getProcessingList()
    {
        return this.processingList;
    }

    public float getProcessingExperience(ItemStack stack)
    {
        float ret = stack.getItem().getSmeltingExperience(stack);
        if (ret != -1) return ret;

        for (Entry<ItemStack, Float> entry : this.experienceList.entrySet())
        {
            if (this.compareItemStacks(stack, (ItemStack)entry.getKey()))
            {
                return ((Float)entry.getValue()).floatValue();
            }
        }

        return 0.0F;
    }
    
    private float getSmeltingExperience(Item item) {
    	return 0.0F;
    }
    
    public int getQuantity(ItemStack stack, int key) { //TODO This method might be backwards
    	if (key == 0) {
        	for (Entry<Integer, Map<ItemStack, Integer>> entry : this.quantitiesList.columnMap().entrySet()) {
        		for (Entry<ItemStack, Integer> ent : entry.getValue().entrySet()) {
        			if (this.compareItemStacks(stack, (ItemStack)ent.getKey())) {
        				return ent.getValue();
        			}
        		}
        	}
    	} else if (key == 1) {
    		for (Entry<Integer, Map<ItemStack, Integer>> entry : this.quantitiesList.columnMap().entrySet()) {
    			for (Entry<ItemStack, Integer> ent : entry.getValue().entrySet()) {
    				if (this.compareItemStacks(stack, (ItemStack)ent.getKey())) {
    					return entry.getKey();
    				}
    			}
    		}
    	}
        return 0;
    }
    
    public int getProcessingTime(ItemStack stack) {
    	for (Entry<ItemStack, Integer> entry : this.timeList.entrySet()) {
    		if (this.compareItemStacks(stack, (ItemStack)entry.getKey())) {
    			return entry.getValue();
    		}
    	}
    	return 0;
    }
    
}
