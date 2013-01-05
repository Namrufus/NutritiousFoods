package com.github.jacobcole2000.NutitiousFoods;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Nutrient {
	// rate of nutrient depletion as a function of nutrient level
	// n' = n*a + n*n*b
	private int a, b;
	private int maxLevel;
	private int spawnLevel; // the initial level at first spawn
	private String name;
	
	// decrement level (this is called whenever the player loses a
	// hunger level)
	public int nutrientLevelDecrement(int level) {
		return 0;
	}
	// add the specified amount of nutrients to the nutrient level
	public int addNutrient(int level, int amount) {
		return 0;
	}
	
	// accessors/mutators
	// ***************************** //
	public int getMaxLevel() {
		return maxLevel;
	}
	
	public int getSpawnLevel() {
		return spawnLevel;
	}
}
	