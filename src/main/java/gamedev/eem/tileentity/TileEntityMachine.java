package gamedev.eem.tileentity;

import gamedev.eem.item.crafting.MachineRecipes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;

public class TileEntityMachine extends TileEntityLockable implements ITickable, ISidedInventory {
	
	public static final int[] SLOTS_TOP = {0};
	public static final int[] SLOTS_SIDES = {1};
	public static final int[] SLOTS_BOTTOM = {2, 1};
	private NonNullList<ItemStack> machineItemStacks = NonNullList.<ItemStack>withSize(3, ItemStack.EMPTY);
	private String machineCustomName;
	private static boolean isRunning = false;
	private int machineRuntime;
	private int currentRuntime;
	private static final int defaultRuntime = 200;
	private int currentPoweredTime;
	private int totalPoweredTime;
	private static int startingPoweredTime = 1000;
	
	public static boolean isRunning() {
		return isRunning;
	}
	
    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    public boolean isUsableByPlayer(EntityPlayer player)
    {
    	return this.world.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    public void openInventory(EntityPlayer player)
    {
    }

    public void closeInventory(EntityPlayer player)
    {
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
     * guis use Slot.isItemValid
     */
    public boolean isItemValidForSlot(int index, ItemStack stack) {
    	//TODO Create and implement a list of items
    	if (index == 2) {
    		return false;
    	} else if (index != 1) {
    		return true;
    	} else {
    		
    	}
    	return true;
    }

    public int[] getSlotsForFace(EnumFacing side)
    {
        return side == EnumFacing.DOWN ? SLOTS_BOTTOM : (side == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES);
    }

    /**
     * Returns true if automation can insert the given item in the given slot from the given side.
     */
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction)
    {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    /**
     * Returns true if automation can extract the given item in the given slot from the given side.
     */
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        if (direction == EnumFacing.DOWN && index == 1)
        {
                return false;
        }
        return true;
    }

	@Override
	public int getSizeInventory() {
		return this.machineItemStacks.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : machineItemStacks) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
        return (ItemStack)this.machineItemStacks.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.machineItemStacks, index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.machineItemStacks, index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack itemstack = (ItemStack)this.machineItemStacks.get(index);
        boolean flag = !stack.isEmpty() && stack.isItemEqual(itemstack) && ItemStack.areItemStackTagsEqual(stack, itemstack);
        this.machineItemStacks.set(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit())
        {
            stack.setCount(this.getInventoryStackLimit());
        }

        /*if (index == 0 && !flag)
        {
            this.totalCookTime = this.getCookTime(stack);
            this.cookTime = 0;
            this.markDirty();
        }*/
        this.markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public int getField(int id) {
		switch (id) {
			case 0:
				return currentPoweredTime;
			case 1:
				return startingPoweredTime;
			case 2:
				return currentRuntime;
			case 3:
				return totalPoweredTime;
			default:
				return 0;
		}
	}

	@Override
	public void setField(int id, int value) {
		switch (id) {
			case 0:
				currentPoweredTime = value;
				break;
			case 1:
				startingPoweredTime = value;
				break;
			case 2:
				currentRuntime = value;
				break;
			case 3:
				totalPoweredTime = value;
				break;
		}
	}

	@Override
	public int getFieldCount() {
		return 4;
	}

	@Override
	public void clear() {
		this.machineItemStacks.clear();
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.machineCustomName : "container.machine";
	}
	
	public void setCustomInventoryName(String name) {
		this.machineCustomName = name;
	}

	@Override
	public boolean hasCustomName() {
		return this.machineCustomName != null && !this.machineCustomName.isEmpty();
	}

	@Override //TODO Make Container
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
		return null;
	}

	@Override
	public String getGuiID() {
		return "eem:machine";
	}

	@Override
	public void update() {
		if (this.isRunning || !((ItemStack)this.machineItemStacks.get(1)).isEmpty() && !((ItemStack)this.machineItemStacks.get(0)).isEmpty()) {
			ItemStack itemstack = this.machineItemStacks.get(0);
			if (!this.isRunning) {
				this.machineRuntime = MachineRecipes.instance().getProcessingTime(itemstack);
				if (this.machineRuntime != 0 && (this.machineItemStacks.get(0).getCount() >= MachineRecipes.instance().getQuantity(itemstack, 0))) {
					this.currentRuntime = 0;
					this.currentPoweredTime = this.startingPoweredTime;
					this.isRunning = true;
				}
			} else { //TODO Look over this
				if (canRun()) {
					if (this.machineRuntime <= this.currentRuntime) {
						processItem();
						this.totalPoweredTime = MachineRecipes.instance().getProcessingTime(this.machineItemStacks.get(0));
					} else {
						currentRuntime++;
					}
				}
			}
			this.currentPoweredTime--;
			this.isRunning = canRun();
			if (this.currentPoweredTime <= 0) {
				if (!((ItemStack)this.machineItemStacks.get(1)).isEmpty()) {
					this.machineItemStacks.get(1).shrink(1);
					this.currentPoweredTime = this.startingPoweredTime;
				} else {
					this.isRunning = false;
				}
			}
		}
	}
	
	private boolean canRun() {
		ItemStack itemResult = MachineRecipes.instance().getProcessingResult(this.machineItemStacks.get(0));
		if (this.machineItemStacks.get(0).isEmpty()) {
			return false;
		} else if (itemResult == null) {
			return false;
		} else {
			ItemStack itemstack = (ItemStack)this.machineItemStacks.get(2);
			if (itemstack.isEmpty()) return true;
			if (!itemstack.isItemEqual(itemResult)) return false;
			int result = itemstack.getCount() + 1;
			return result <= getInventoryStackLimit() && result <= itemResult.getMaxStackSize();
		}
	}
	
	private void processItem() {
		ItemStack itemstack = (ItemStack)this.machineItemStacks.get(2);
		ItemStack itemstack1 = (ItemStack)this.machineItemStacks.get(0);
		if (canRun()) {
			if (itemstack.isEmpty()) {
				this.machineItemStacks.set(2, MachineRecipes.instance().getProcessingResult(itemstack1));
			} else if (itemstack.getItem() == MachineRecipes.instance().getProcessingResult(itemstack1).getItem()) {
				itemstack.grow(MachineRecipes.instance().getQuantity(itemstack1, 1));
			}
			itemstack1.shrink(MachineRecipes.instance().getQuantity(itemstack1, 0));
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound compound)
    {
        this.machineItemStacks = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.machineItemStacks);
        this.currentPoweredTime = compound.getInteger("PoweredTime");
        this.currentRuntime = compound.getInteger("Runtime");
        this.totalPoweredTime = compound.getInteger("RuntimeTotal");
        this.currentRuntime = MachineRecipes.instance().getProcessingTime((ItemStack)this.machineItemStacks.get(1));

        if (compound.hasKey("CustomName", 8))
        {
            this.machineCustomName = compound.getString("CustomName");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        compound.setInteger("PoweredTime", (short)this.currentPoweredTime);
        compound.setInteger("Runtime", (short)this.currentRuntime);
        compound.setInteger("RuntimeTotal", (short)this.totalPoweredTime);
        ItemStackHelper.saveAllItems(compound, this.machineItemStacks);

        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.machineCustomName);
        }

        return compound;
    }

}