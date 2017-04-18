package gamedev.eem.inventory;

import gamedev.eem.item.crafting.MachineRecipes;
import gamedev.eem.tileentity.TileEntityMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerMachine extends Container {
    private final IInventory tileMachine;
    private int cookTime;
    private int totalCookTime;
    private int furnaceBurnTime;
    private int currentItemBurnTime;

    public ContainerMachine(InventoryPlayer playerInventory, IInventory machineInventory)
    {
        this.tileMachine = machineInventory;
        this.addSlotToContainer(new Slot(machineInventory, 0, 56, 17));
        this.addSlotToContainer(new SlotMachineFuel(machineInventory, 1, 56, 53));
        this.addSlotToContainer(new SlotMachineOutput(playerInventory.player, machineInventory, 2, 116, 35));

        for (int i = 0; i < 3; ++i)
        {
            for (int j = 0; j < 9; ++j)
            {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k)
        {
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public void addListener(IContainerListener listener)
    {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, this.tileMachine);
    }

    /**
     * Looks for changes made in the container, sends them to every listener.
     */
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for (int i = 0; i < this.listeners.size(); ++i)
        {
            IContainerListener icontainerlistener = (IContainerListener)this.listeners.get(i);

            if (this.cookTime != this.tileMachine.getField(2))
            {
                icontainerlistener.sendProgressBarUpdate(this, 2, this.tileMachine.getField(2));
            }

            if (this.furnaceBurnTime != this.tileMachine.getField(0))
            {
                icontainerlistener.sendProgressBarUpdate(this, 0, this.tileMachine.getField(0));
            }

            if (this.currentItemBurnTime != this.tileMachine.getField(1))
            {
                icontainerlistener.sendProgressBarUpdate(this, 1, this.tileMachine.getField(1));
            }

            if (this.totalCookTime != this.tileMachine.getField(3))
            {
                icontainerlistener.sendProgressBarUpdate(this, 3, this.tileMachine.getField(3));
            }
        }

        this.cookTime = this.tileMachine.getField(2);
        this.furnaceBurnTime = this.tileMachine.getField(0);
        this.currentItemBurnTime = this.tileMachine.getField(1);
        this.totalCookTime = this.tileMachine.getField(3);
    }

    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data)
    {
        this.tileMachine.setField(id, data);
    }

    /**
     * Determines whether supplied player can use this container
     */
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return this.tileMachine.isUsableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 2)
            {
                if (!this.mergeItemStack(itemstack1, 3, 39, true))
                {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            }
            else if (index != 1 && index != 0)
            {
                if (!MachineRecipes.instance().getProcessingResult(itemstack1).isEmpty())
                {
                    if (!this.mergeItemStack(itemstack1, 0, 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (TileEntityFurnace.isItemFuel(itemstack1))
                {
                    if (!this.mergeItemStack(itemstack1, 1, 2, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (index >= 3 && index < 30)
                {
                    if (!this.mergeItemStack(itemstack1, 30, 39, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
                else if (index >= 30 && index < 39 && !this.mergeItemStack(itemstack1, 3, 30, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 3, 39, false))
            {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }
}