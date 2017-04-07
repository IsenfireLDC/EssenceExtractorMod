package gamedev.eem.item.crafting;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class MachineRecipes {
	
    private static final MachineRecipes SMELTING_BASE = new MachineRecipes();
    private final Map<ItemStack, ItemStack> processingList = Maps.<ItemStack, ItemStack>newHashMap();
    private final Map<ItemStack, Float> experienceList = Maps.<ItemStack, Float>newHashMap();
    
    public static MachineRecipes instance()
    {
        return SMELTING_BASE;
    }
    
    private MachineRecipes () {
    	
    }
    
    /**
     * Adds a smelting recipe, where the input item is an instance of Block.
     */
    public void addProcessingRecipeForBlock(Block input, ItemStack stack, float experience)
    {
        this.addProcessing(Item.getItemFromBlock(input), stack, experience);
    }

    /**
     * Adds a smelting recipe using an Item as the input item.
     */
    public void addProcessing(Item input, ItemStack stack, float experience)
    {
        this.addProcessingRecipe(new ItemStack(input, 1, 32767), stack, experience);
    }

    /**
     * Adds a smelting recipe using an ItemStack as the input for the recipe.
     */
    public void addProcessingRecipe(ItemStack input, ItemStack stack, float experience)
    {
        if (getProcessingResult(input) != ItemStack.EMPTY) { net.minecraftforge.fml.common.FMLLog.info("Ignored smelting recipe with conflicting input: " + input + " = " + stack); return; }
        this.processingList.put(input, stack);
        this.experienceList.put(stack, Float.valueOf(experience));
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
    
}
