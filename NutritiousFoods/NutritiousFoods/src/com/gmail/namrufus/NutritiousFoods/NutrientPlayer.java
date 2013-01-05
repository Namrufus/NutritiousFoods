package com.gmail.namrufus.NutritiousFoods;

import java.util.ArrayList;

import org.bukkit.entity.Player;

// represents the state of a player's nutrition

public class NutrientPlayer {
	// record of the player's nutrient levels
	// parallel to the plugin's nutrient arraylist
	private int[] nutrientLevels;
	// link to the player
	Player player;
	// the plugin
	NutritiousFoods plugin;
	
	// construct a player that has never logged in to the server before
	// create new nutrient data
	NutrientPlayer(Player player, NutritiousFoods plugin) {
		this.player = player;
		this.plugin = plugin;
		
		
		ArrayList<Nutrient> nutrientDefs = plugin.getNutrients();
		nutrientLevels = new int[nutrientDefs.size()];
		for (int i=0; i<nutrientDefs.size(); i++)
			nutrientLevels[i] = nutrientDefs.get(i).getSpawnLevel();
	}
	
	// accesors and mutators
	// ------------------------------------ //
	
	Player getPlayer() {
		return player;
	}
	
	int getNutrientLevel(int index) {
		return nutrientLevels[index];
	}
}
