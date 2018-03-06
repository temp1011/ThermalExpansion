package cofh.thermalexpansion.block.device;

import cofh.core.fluid.FluidTankCore;
import cofh.core.util.helpers.MathHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.device.BlockDevice.Type;
import cofh.thermalexpansion.gui.client.device.GuiChunkLoader;
import cofh.thermalexpansion.gui.container.device.ContainerChunkLoader;
import cofh.thermalexpansion.init.TEProps;
import cofh.thermalfoundation.init.TFFluids;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TileChunkLoader extends TileDeviceBase implements ITickable {

	private static final int TYPE = Type.CHUNK_LOADER.getMetadata();

	public static void initialize() {

		SIDE_CONFIGS[TYPE] = new SideConfig();
		SIDE_CONFIGS[TYPE].numConfig = 5;
		SIDE_CONFIGS[TYPE].slotGroups = new int[][] { {}, { 0 }, { 0 }, {}, { 0 } };
		SIDE_CONFIGS[TYPE].sideTypes = new int[] { NONE, INPUT_ALL, INPUT_PRIMARY, INPUT_SECONDARY, OPEN };
		SIDE_CONFIGS[TYPE].defaultSides = new byte[] { 0, 1, 1, 1, 1, 1 };

		SLOT_CONFIGS[TYPE] = new SlotConfig();
		SLOT_CONFIGS[TYPE].allowInsertionSlot = new boolean[] { true };
		SLOT_CONFIGS[TYPE].allowExtractionSlot = new boolean[] { false };

		LIGHT_VALUES[TYPE] = 5;

		GameRegistry.registerTileEntity(TileChunkLoader.class, "thermalexpansion:device_chunk_loader");

		config();
	}

	private static final int TIME_CONSTANT = 18000;

	public static void config() {

		String category = "Device.ChunkLoader";
		BlockDevice.enable[TYPE] = ThermalExpansion.CONFIG.get(category, "Enable", true);
	}

	private int inputTracker;

	private FluidTankCore tank = new FluidTankCore(TEProps.MAX_FLUID_SMALL);

	private int offset;

	public TileChunkLoader() {

		super();
		inventory = new ItemStack[1];
		Arrays.fill(inventory, ItemStack.EMPTY);
		createAllSlots(inventory.length);

		offset = MathHelper.RANDOM.nextInt(TIME_CONSTANT);

		hasAutoInput = true;

		enableAutoInput = true;
	}

	@Override
	public int getType() {

		return TYPE;
	}
	
	public Collection<ChunkPos> getChunks()
	{
		Set<ChunkPos> chunks = new HashSet<ChunkPos>();
		for(int i = -1 ; i<2 ; i++) {
			for(int j = -1 ; j<2 ; j++) {
				chunks.add(new ChunkPos(this.pos.getX() << 4 + i, this.pos.getZ() << 4 + j));
			}
		}
		return chunks;
	}

	@Override
	public void update() {

		if (!timeCheckOffset()) {
			return;
		}
		transferInput();
		fillTank();

		boolean curActive = isActive;

		if (isActive) {
			useEnder();

			if (!redstoneControlOrDisable()) {
				isActive = false;
			}
		} else if (redstoneControlOrDisable()) {
			isActive = true;
		}
		updateIfChanged(curActive);

	}

	protected void transferInput() {

		if (!getTransferIn()) {
			return;
		}
		int side;
		for (int i = inputTracker + 1; i <= inputTracker + 6; i++) {
			side = i % 6;
			if (isPrimaryInput(sideConfig.sideTypes[sideCache[side]])) {
				if (extractItem(0, ITEM_TRANSFER[level], EnumFacing.VALUES[side])) {
					inputTracker = side;
					break;
				}
			}
		}
	}

	protected boolean timeCheckOffset() {

		return (world.getTotalWorldTime() + offset) % TIME_CONSTANT == 0;
	}
	
	protected void fillTank() {
		if(tank.getSpace()>100 && !inventory[0].isEmpty()) {
			tank.fill(new FluidStack(TFFluids.fluidEnder, 100), true);
			inventory[0].shrink(1);
			
		}
	}
	
	protected void useEnder() {
		if(tank.getFluidAmount()<50) {
			isActive = false;
			return;
		}
		tank.drain(new FluidStack(TFFluids.fluidEnder, 50), true);
	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiChunkLoader(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerChunkLoader(inventory, this);
	}

	@Override
	public FluidTankCore getTank() {

		return tank;
	}

	@Override
	public FluidStack getTankFluid() {

		return tank.getFluid();
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		inputTracker = nbt.getInteger("TrackIn");
		tank.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setInteger("TrackIn", inputTracker);
		tank.writeToNBT(nbt);

		return nbt;
	}

	/* IInventory */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		return stack.getItem().equals(Items.ENDER_PEARL);
	}

	/* CAPABILITIES */
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing from) {

		return super.hasCapability(capability, from) || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, final EnumFacing from) {

		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new IFluidHandler() {

				@Override
				public IFluidTankProperties[] getTankProperties() {

					FluidTankInfo info = tank.getInfo();
					return new IFluidTankProperties[] { new FluidTankProperties(info.fluid, info.capacity, true, true) };
				}

				@Override
				public int fill(FluidStack resource, boolean doFill) {

					if (from == null || allowInsertion(sideConfig.sideTypes[sideCache[from.ordinal()]])) {
						return tank.fill(resource, doFill);
					}
					return 0;
				}

				@Nullable
				@Override
				public FluidStack drain(FluidStack resource, boolean doDrain) {

					if (from == null || allowExtraction(sideConfig.sideTypes[sideCache[from.ordinal()]])) {
						return tank.drain(resource, doDrain);
					}
					return null;
				}

				@Nullable
				@Override
				public FluidStack drain(int maxDrain, boolean doDrain) {

					if (from == null || allowExtraction(sideConfig.sideTypes[sideCache[from.ordinal()]])) {
						return tank.drain(maxDrain, doDrain);
					}
					return null;
				}
			});
		}
		return super.getCapability(capability, from);
	}

}
