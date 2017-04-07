package gamedev.eem.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public class SlotMachineFuel extends Slot {

    public SlotMachineFuel(IInventory inventoryIn, int slotIndex, int xPosition, int yPosition)
    {
        super(inventoryIn, slotIndex, xPosition, yPosition);
    }
    
    public boolean isItemValid(ItemStack stack)
    {
        return TileEntityFurnace.isItemFuel(stack);
    }
    
    public int getItemStackLimit(ItemStack stack)
    {
        return super.getItemStackLimit(stack);
    }
    
}
