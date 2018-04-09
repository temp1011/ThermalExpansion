package cofh.thermalexpansion.util.managers.machine;

import cofh.core.inventory.ComparableItemStackValidated;
import cofh.core.util.helpers.ItemHelper;
import cofh.thermalfoundation.item.ItemBait;
import cofh.thermalfoundation.item.ItemFertilizer;
import gnu.trove.map.hash.THashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Map;
import java.util.Map.Entry;

public class ChargerManager {

	private static Map<ComparableItemStackValidated, ChargerRecipe> recipeMap = new THashMap<>();

	public static final int DEFAULT_ENERGY = 8000;

	public static ChargerRecipe getRecipe(ItemStack input) {

		if (input.isEmpty()) {
			return null;
		}
		ComparableItemStackValidated query = new ComparableItemStackValidated(input);

		ChargerRecipe recipe = recipeMap.get(query);

		if (recipe == null) {
			query.metadata = OreDictionary.WILDCARD_VALUE;
			recipe = recipeMap.get(query);
		}
		return recipe;
	}

	public static boolean recipeExists(ItemStack input) {

		return getRecipe(input) != null;
	}

	public static ChargerRecipe[] getRecipeList() {

		return recipeMap.values().toArray(new ChargerRecipe[recipeMap.size()]);
	}

	public static void initialize() {

		addRecipe(DEFAULT_ENERGY, ItemFertilizer.fertilizerRich, ItemFertilizer.fertilizerFlux);
		addRecipe(DEFAULT_ENERGY, ItemBait.baitRich, ItemBait.baitFlux);

		/* LOAD RECIPES */
		loadRecipes();
	}

	public static void loadRecipes() {

	}

	public static void refresh() {

		Map<ComparableItemStackValidated, ChargerRecipe> tempMap = new THashMap<>(recipeMap.size());
		ChargerRecipe tempRecipe;

		for (Entry<ComparableItemStackValidated, ChargerRecipe> entry : recipeMap.entrySet()) {
			tempRecipe = entry.getValue();
			tempMap.put(new ComparableItemStackValidated(tempRecipe.input), tempRecipe);
		}
		recipeMap.clear();
		recipeMap = tempMap;
	}

	/* ADD RECIPES */
	public static ChargerRecipe addRecipe(int energy, ItemStack input, ItemStack output) {

		if (input.isEmpty() || output.isEmpty() || energy <= 0 || recipeExists(input)) {
			return null;
		}
		ChargerRecipe recipe = new ChargerRecipe(input, output, energy);
		recipeMap.put(new ComparableItemStackValidated(input), recipe);
		return recipe;
	}

	/* REMOVE RECIPES */
	public static ChargerRecipe removeRecipe(ItemStack input) {

		return recipeMap.remove(new ComparableItemStackValidated(input));
	}

	/* HELPERS */
	private static void addOreDictRecipe(String oreName, ItemStack output) {

		addOreDictRecipe(DEFAULT_ENERGY, oreName, output);
	}

	private static void addOreDictRecipe(int energy, String oreName, ItemStack output) {

		if (ItemHelper.oreNameExists(oreName) && !recipeExists(OreDictionary.getOres(oreName, false).get(0))) {
			addRecipe(energy, ItemHelper.cloneStack(OreDictionary.getOres(oreName, false).get(0), 1), output);
		}
	}

	/* RECIPE CLASS */
	public static class ChargerRecipe {

		final ItemStack input;
		final ItemStack output;
		final int energy;

		public ChargerRecipe(ItemStack input, ItemStack output, int energy) {

			this.input = input;
			this.output = output;
			this.energy = energy;
		}

		public ItemStack getInput() {

			return input;
		}

		public ItemStack getOutput() {

			return output;
		}

		public int getEnergy() {

			return energy;
		}
	}

}
