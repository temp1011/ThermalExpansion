package cofh.thermalexpansion.gui.container.apparatus;

import cofh.core.gui.slot.ISlotValidator;
import cofh.core.gui.slot.SlotEnergy;
import cofh.core.gui.slot.SlotValidated;
import cofh.core.util.helpers.ItemHelper;
import cofh.thermalexpansion.block.apparatus.TileBreaker;
import cofh.thermalexpansion.gui.container.ContainerTEBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class ContainerBreaker extends ContainerTEBase implements ISlotValidator {

	TileBreaker myTile;

	public ContainerBreaker(InventoryPlayer inventory, TileEntity tile) {

		super(inventory, tile);

		myTile = (TileBreaker) tile;
		addSlotToContainer(new SlotEnergy(myTile, myTile.getChargeSlot(), 8, 53));
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

}
