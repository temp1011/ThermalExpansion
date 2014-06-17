package thermalexpansion.block.dynamo;

import cofh.api.energy.IEnergyContainerItem;
import cofh.network.CoFHPacket;
import cofh.util.EnergyHelper;
import cofh.util.ItemHelper;
import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import thermalexpansion.core.TEProps;
import thermalexpansion.gui.client.dynamo.GuiDynamoEnervation;
import thermalexpansion.gui.container.dynamo.ContainerDynamoEnervation;
import thermalexpansion.util.FuelHandler;
import thermalfoundation.fluid.TFFluids;

public class TileDynamoEnervation extends TileDynamoBase {

	static final int TYPE = BlockDynamo.Types.ENERVATION.ordinal();

	public static void initialize() {

		GameRegistry.registerTileEntity(TileDynamoEnervation.class, "thermalexpansion.DynamoEnervation");
	}

	static int redstoneRF = 64000;
	static int blockRedstoneRF = redstoneRF * 10;

	static ItemStack redstone = new ItemStack(Items.redstone);
	static ItemStack blockRedstone = new ItemStack(Blocks.redstone_block);

	static {
		String category = "fuels.enervation";
		redstoneRF = FuelHandler.configFuels.get(category, "redstone", redstoneRF);
		blockRedstoneRF = redstoneRF * 10;
	}

	public static int getItemEnergyValue(ItemStack fuel) {

		if (fuel == null) {
			return 0;
		}
		if (fuel.isItemEqual(redstone)) {
			return redstoneRF;
		}
		if (fuel.isItemEqual(blockRedstone)) {
			return blockRedstoneRF;
		}
		if (EnergyHelper.isEnergyContainerItem(fuel)) {
			IEnergyContainerItem container = (IEnergyContainerItem) fuel.getItem();
			return container.extractEnergy(fuel, container.getEnergyStored(fuel), true);
		}
		return 0;
	}

	int currentFuelRF = getItemEnergyValue(redstone);

	public TileDynamoEnervation() {

		super();
		inventory = new ItemStack[1];
	}

	@Override
	public int getType() {

		return TYPE;
	}

	@Override
	protected boolean canGenerate() {

		if (fuelRF > 0) {
			return true;
		}
		return getItemEnergyValue(inventory[0]) >= config.maxPower;
	}

	@Override
	protected void generate() {

		int energy;

		if (fuelRF <= 0) {
			if (EnergyHelper.isEnergyContainerItem(inventory[0])) {
				IEnergyContainerItem container = (IEnergyContainerItem) inventory[0].getItem();
				fuelRF += container.extractEnergy(inventory[0], container.getEnergyStored(inventory[0]), false);
				currentFuelRF = redstoneRF;
			} else {
				energy = getItemEnergyValue(inventory[0]) * fuelMod / 100;
				fuelRF += energy;
				currentFuelRF = energy;
				inventory[0] = ItemHelper.consumeItem(inventory[0]);
			}
		}
		energy = calcEnergy() * energyMod;
		energyStorage.modifyEnergyStored(energy);
		fuelRF -= energy;
	}

	@Override
	public IIcon getActiveIcon() {

		return TFFluids.fluidRedstone.getIcon();
	}

	/* NETWORK METHODS */
	@Override
	public CoFHPacket getGuiPacket() {

		CoFHPacket payload = super.getGuiPacket();
		payload.addInt(currentFuelRF);
		return payload;
	}

	@Override
	protected void handleGuiPacket(CoFHPacket payload) {

		super.handleGuiPacket(payload);
		currentFuelRF = payload.getInt();
	}

	/* GUI METHODS */
	@Override
	public GuiContainer getGuiClient(InventoryPlayer inventory) {

		return new GuiDynamoEnervation(inventory, this);
	}

	@Override
	public Container getGuiServer(InventoryPlayer inventory) {

		return new ContainerDynamoEnervation(inventory, this);
	}

	public int getScaledDuration(int scale) {

		if (currentFuelRF <= 0) {
			currentFuelRF = redstoneRF;
		} else if (EnergyHelper.isEnergyContainerItem(inventory[0])) {
			return scale;
		}
		return fuelRF * scale / currentFuelRF;
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		currentFuelRF = nbt.getInteger("FuelMax");

		if (currentFuelRF <= 0) {
			currentFuelRF = redstoneRF;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setInteger("FuelMax", currentFuelRF);
	}

	/* ISidedInventory */
	@Override
	public int[] getAccessibleSlotsFromSide(int side) {

		return side != facing ? SLOTS : TEProps.EMPTY_INVENTORY;
	}

}
