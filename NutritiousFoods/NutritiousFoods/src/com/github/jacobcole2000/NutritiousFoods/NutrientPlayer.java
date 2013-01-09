package com.github.jacobcole2000.NutritiousFoods;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;

// represents the state of a player's nutrition

public class NutrientPlayer {
	// record of the player's nutrient levels
	// parallel to the plugin's nutrient arraylist
	private float[] nutrientLevels;
	// link to the player
	Player player;
	// the plugin
	NutritiousFoods plugin;
	// previous saturation level for saturation level  calculations
	float prevSatLevel;
	// "leftover" saturation
	float leftoverSatLevel;
	// store the last thing 'interacted' with in order to determine what food has been eatenized
	private Material lastInteractMaterial;
	
	// construct a player that has never logged in to the server before
	// create new nutrient data
	NutrientPlayer(Player player, NutritiousFoods plugin) {
		this.player = player;
		this.plugin = plugin;
		this.lastInteractMaterial = Material.AIR;
		
		this.prevSatLevel = player.getSaturation();
		this.leftoverSatLevel = 0.0f;
		
		
		ArrayList<Nutrient> nutrientDefs = plugin.getNutrients();
		nutrientLevels = new float[nutrientDefs.size()];
		for (int i=0; i<nutrientDefs.size(); i++)
			nutrientLevels[i] = nutrientDefs.get(i).getSpawnLevel();
	}
	
	int updateSatLevel(float newLevel) {
		// get the amount by which the saturation level has changed
		float diff = leftoverSatLevel + newLevel - prevSatLevel;

		prevSatLevel = newLevel;
		
		// if the the sat level has increased, do nothing
		if (diff>0.0)
			return 0;
		// round down to an integer and store the remainder
		int res = (int) diff;
		leftoverSatLevel = diff-(float)res;
		return res;
	}
	
	// accesors and mutators
	// ------------------------------------ //
	
	Player getPlayer() {
		return player;
	}
	
	float getNutrientLevel(int index) {
		return nutrientLevels[index];
	}
	
	void addNutrientLevel(Nutrient nutrient, float amount) {
		int index = nutrient.getIndex();
		float level = nutrientLevels[index] + amount;
		
		if (level < 0.0)
			level = 0.0f;
		if (level > nutrient.getMaxLevel())
			level = nutrient.getMaxLevel();

		nutrientLevels[index] = level;
	}
	
	void decNutrientLevel(int index, Nutrient nutrient) {
		float level = nutrientLevels[index];
		level -= nutrient.nutrientLevelDecrement(level);

		if (level < 0.0)
			level = 0.0f;
		if (level > nutrient.getMaxLevel())
			level = nutrient.getMaxLevel();
		
		nutrientLevels[index] = level;
	}
	
	public Material getLastInteractMaterial() {
		return lastInteractMaterial;
	}
	
	public void setLastInteractMaterial(Material mat) {
		lastInteractMaterial = mat;
	}
}
