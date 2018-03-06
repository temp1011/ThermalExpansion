package cofh.thermalexpansion.util.managers.apparatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cofh.thermalexpansion.block.device.TileChunkLoader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class ChunkLoaderManager {
	
	Map<TileChunkLoader,BlockPos> allLoaders = new HashMap<TileChunkLoader,BlockPos>();
	Map<TileChunkLoader,BlockPos> activeLoaders = new HashMap<TileChunkLoader,BlockPos>();
	
	//add remove chunkloaders, set active/inactive
	
	//save and load for world reload
	
	//handle players logging in and out
	
	//chunk load limit
	
	//tickets for chunkloading from forge
}