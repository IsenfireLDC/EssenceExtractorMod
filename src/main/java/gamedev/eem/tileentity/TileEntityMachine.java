package gamedev.eem.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
	private boolean isRunning = false;
	private int machineRuntime;
	private int currentRuntime;
	private static final int defaultRuntime = 200;
	private int currentPoweredTime;
	private static final int startingPoweredTime = 1000;
	
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
				return getItemRuntime(this.machineItemStacks.get(0).getUnlocalizedName());
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
				break;
			case 2:
				currentRuntime = value;
				break;
			case 3:
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
			String name = this.machineItemStacks.get(0).getUnlocalizedName();
			if (!this.isRunning) {
				this.machineRuntime = getItemRuntime(name);
				if (this.machineRuntime != 0 && (this.machineItemStacks.get(0).getCount() >= getItemQuantity(name))) {
					this.currentRuntime = 0;
					this.currentPoweredTime = this.startingPoweredTime;
					this.isRunning = true;
				}
			} else { //TODO Look over this
				if (canRun()) {
					if (this.machineRuntime <= this.currentRuntime) {
						processItem();
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
	
	/*
	 * Stores runtime and quantity, which are used by the machine
	 * Item and Block names were found using the en_US.lang file in assets
	 * 
	 * Information about the essences are in their respective classes
	 */
	private enum itemRuntime {
		//TODO Balance quantities and runtimes
		//Essence of Poison
		POISON1(defaultRuntime, "item.potatoPoisonous.name", null), //poisonous potato
		POISON2(defaultRuntime, "item.spiderEye.name", 2, null), //spider eye
		//Essence of Hunger
		HUNGER1(defaultRuntime, "item.rottenFlesh.name", null), //rotten flesh
		HUNGER2(defaultRuntime, "item.fermentedSpiderEye.name", null), //fermented spider eye
		//Essence of Fire
		FIRE1(defaultRuntime, "item.coal.name", 5, null), //coal
		FIRE2(defaultRuntime, "tile.blockCoal.name", null), //coal block
		//maybe add charcoal?
		FIRE3(defaultRuntime, "item.bucketLava.name", null), //lava bucket
		//Essence of Fuel (may or may not be added, essence of fire if not)
		FUEL1(defaultRuntime, "item.magmaCream.name", null), //magma cream
		FUEL2(defaultRuntime, "item.blazeRod.name", null), //blaze rod
		FUEL3(defaultRuntime, "item.blazePowder.name", null), //blaze powder
		//Essence of Thorns
		THORNS1(defaultRuntime, "tile.cactus.name", 3, null), //cactus
		//Essence of Light
		LIGHT1(defaultRuntime, "tile.torch.name", 3, null), //torch
		LIGHT2(defaultRuntime, "tile.litpumpkin.name", null), //jack 'o lantern
		LIGHT3(defaultRuntime, "item.yellowDust.name", 3, null), //glowstone dust
		LIGHT4(defaultRuntime, "tile.lightgem.name", null); //glowstone
		
		private final int runtime;
		private final String id;
		private final int quantity;
		private final ItemStack result;
		
		/*
		 * Three-argument constructor for itemRuntime enums
		 * 
		 * @param runtime - The amount of time it takes for the item to be processed
		 * @param id - The unlocalized name of the item
		 * @param result - The resulting ItemStack after the machine has finished
		 */
		private itemRuntime(int runtime, String id, ItemStack result) {
			this.runtime = runtime;
			this.id = id;
			this.quantity = 1;
			this.result = result;
		}
		
		/*
		 * Four-argument constructor for itemRuntime enums
		 * 
		 * @param runtime - The amount of time it takes for the item to be processed
		 * @param id - The unlocalized name of the item
		 * @param quantity - The quantity of the item necessary in order to process
		 * @param result - The resulting ItemStack after the machine has finished
		 */
		private itemRuntime(int runtime, String id, int quantity, ItemStack result) {
			this.runtime = runtime;
			this.id = id;
			this.quantity = quantity;
			this.result = result;
		}
		/*Returns id of the itemRuntime enum*/
		String getId() {
			return this.id;
		}
		/*Returns runtime of the itemRuntime enum*/
		int getRuntime() {
			return this.runtime;
		}
		/*Returns quantity of the itenRuntime enum*/
		int getQuantity() {
			return this.quantity;
		}
		
		/*Returns result of processing the itemRuntime enum*/
		ItemStack getResult() {
			return this.result;
		}
	}
	
	/*
	 * Utility method that returns the runtime of the item
	 * If the item is not valid for the machine, the return value is 0
	 * 
	 * @param id - The unlocalized name of the item
	 * @return The amount of time it takes for the given item to be processed
	 */
	private int getItemRuntime(String id) {
		for (itemRuntime run : itemRuntime.values()) {
			if (id.equals(run.getId())) {
				return run.getRuntime();
			}
		}
		return 0;
	}
	
	/*
	 * Utility method that returns the necessary quantity of the item
	 * If the item is not valid for the machine, the return value is 0
	 * 
	 * @param id - The unlocalized name of the item
	 * @return The amount of time it takes for the given item to be processed
	 */
	private int getItemQuantity(String id) {
		for (itemRuntime run : itemRuntime.values()) {
			if (id.equals(run.getId())) {
				return run.getQuantity();
			}
		}
		return 0;
	}
	
	/*
	 * Utility method that returns the resulting ItemStack of processing the item
	 * If the item is not valid for the machine, the return value is null
	 * 
	 * @param id - The unlocalized name of the item
	 * @return The ItemStack created after processing
	 */
	private ItemStack getItemResult(String id) {
		for (itemRuntime run : itemRuntime.values()) {
			if (id.equals(run.getId())) {
				return run.getResult();
			}
		}
		return null;
	}
	
	private boolean canRun() {
		ItemStack itemResult = getItemResult(this.machineItemStacks.get(0).getUnlocalizedName());
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
				this.machineItemStacks.set(2, getItemResult(itemstack1.getUnlocalizedName()));
			} else if (itemstack.getItem() == getItemResult(itemstack1.getUnlocalizedName()).getItem()) {
				itemstack.grow(1);
			}
			itemstack1.shrink(getItemQuantity(itemstack1.getUnlocalizedName()));
		}
	}

}