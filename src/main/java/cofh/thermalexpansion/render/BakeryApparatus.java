package cofh.thermalexpansion.render;

import codechicken.lib.model.bakery.ModelErrorStateProperty;
import codechicken.lib.model.bakery.ModelErrorStateProperty.ErrorState;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.BakingVertexBuffer;
import codechicken.lib.texture.IItemBlockTextureProvider;
import codechicken.lib.texture.IWorldBlockTextureProvider;
import cofh.thermalexpansion.block.apparatus.TileApparatusBase;
import cofh.thermalexpansion.init.TEBlocks;
import cofh.thermalexpansion.init.TEProps;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;

import java.util.ArrayList;
import java.util.List;

public class BakeryApparatus extends CubeBakeryBase {

	public static final BakeryApparatus INSTANCE = new BakeryApparatus();

	/* IBlockBakery */
	@Override
	public IExtendedBlockState handleState(IExtendedBlockState state, IBlockAccess world, BlockPos pos) {

		TileApparatusBase apparatusBase = (TileApparatusBase) world.getTileEntity(pos);

		if (apparatusBase == null) {
			return state.withProperty(ModelErrorStateProperty.ERROR_STATE, ErrorState.of("Null tile. Position: %s", pos));
		}
		state = state.withProperty(ModelErrorStateProperty.ERROR_STATE, ErrorState.OK);
		state = state.withProperty(TEProps.TILE_APPARATUS, apparatusBase);
		return state;
	}

	/* IItemBakery */
	@Override
	public List<BakedQuad> bakeItemQuads(EnumFacing face, ItemStack stack) {

		List<BakedQuad> quads = new ArrayList<>();

		if (face != null && !stack.isEmpty()) {
			BakingVertexBuffer buffer = BakingVertexBuffer.create();
			CCRenderState ccrs = CCRenderState.instance();
			buffer.begin(0x07, DefaultVertexFormats.ITEM);
			ccrs.reset();
			ccrs.bind(buffer);

			IItemBlockTextureProvider provider = TEBlocks.blockApparatus;
			renderFace(ccrs, face, provider.getTexture(face, stack), 0xFFFFFFFF);

			buffer.finishDrawing();
			quads.addAll(buffer.bake());
		}
		return quads;
	}

	/* ILayeredBlockBakery */
	@Override
	public List<BakedQuad> bakeLayerFace(EnumFacing face, BlockRenderLayer layer, IExtendedBlockState state) {

		List<BakedQuad> quads = new ArrayList<>();

		if (face != null && state != null) {
			Block block = state.getBlock();
			IWorldBlockTextureProvider provider = (IWorldBlockTextureProvider) block;
			TileApparatusBase tile = state.getValue(TEProps.TILE_APPARATUS);

			BakingVertexBuffer buffer = BakingVertexBuffer.create();
			buffer.begin(0x07, DefaultVertexFormats.ITEM);
			CCRenderState ccrs = CCRenderState.instance();
			ccrs.reset();
			ccrs.bind(buffer);

			renderFace(ccrs, face, provider.getTexture(face, state, layer, tile.getWorld(), tile.getPos()), tile.getColorMask(layer, face));

			buffer.finishDrawing();
			quads.addAll(buffer.bake());
		}
		return quads;
	}

}

