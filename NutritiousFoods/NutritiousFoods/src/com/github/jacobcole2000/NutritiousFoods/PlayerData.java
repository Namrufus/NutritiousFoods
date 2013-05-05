package com.github.jacobcole2000.NutritiousFoods;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

// represents the state of a player's nutrition

public class PlayerData {
	private HashMap<Nutrient, Integer> nutrientLevels;
	
	private Material eatenFood;
	
	// create a new nutrient level container for a player. Use default nutrient levels
	// must be called after the general config has been loaded
	// must be called after the list of nutrients has been loaded
	public PlayerData() {
		eatenFood = null;
		nutrientLevels = new HashMap<Nutrient, Integer>();
		
		for (Nutrient nutrient: Config.nutrients) {
			nutrientLevels.put(nutrient, new Integer(Config.startNutrientLevel));
		}
	}
	
	// create a new nutrient level container for a player. 
	// get nutrient levels from the given file subsection
	// must be called after the general config has been loaded
	// must be called after the list of nutrients has been loaded
	public PlayerData(ConfigurationSection config) {
		eatenFood = null;
		nutrientLevels = new HashMap<Nutrient, Integer>();
		
		for (String nutrientName: config.getKeys(false)) {
			// search for the nutrient in the set of nutrients
			for (Nutrient nutrient: Config.nutrients) {
				if (nutrient.getName().equals(nutrientName)) {
					int nutrientLevel = config.getInt(nutrientName);
					nutrientLevels.put(nutrient, nutrientLevel);
				}
			}
			// silently ignore unknown nutrient names, it simply means that the config
			// has changed, and that nutrient no longer is needed
			
			// add defaults for any nutrients that were not in the file
			for (Nutrient nutrient: Config.nutrients) {
				if (!nutrientLevels.containsKey(nutrient)) {
					nutrientLevels.put(nutrient, new Integer(Config.startNutrientLevel));
				}
			}
		}
	}
	
	public Material getEatenFood() {
		return eatenFood;
	}
	public void setEatenFood(Material eatenFood) {
		this.eatenFood = eatenFood;
	}
	
	public Map<Nutrient, Integer> getNutrientLevels() {
		return nutrientLevels;
	}
	
	public void applyDebuffs(Player player) {
		for (Nutrient nutrient : nutrientLevels.keySet()) {
			int level = nutrientLevels.get(nutrient);
			
			double nutrientFraction = (double) level / ((double)Config.maxNutrientLevel / 2.0);
			
			if (nutrientFraction > 1.0)
				return;
			
			nutrient.applyDebuffs(player, 1.0 - nutrientFraction);
		}
	}
	
	public void decrementNutrientLevels(boolean slow) {
		for (Nutrient nutrient : nutrientLevels.keySet()) {
			int level = nutrientLevels.get(nutrient);
			
			if (level > (double)Config.maxNutrientLevel / 2.0 && slow)
				return;
			else if (level <= (double)Config.maxNutrientLevel / 2.0 && !slow)
				return;
			
			addNutrientLevel(nutrient, -1);
		}
	}
	
	public void addNutrientLevel(Nutrient nutrient, int amount) {
		if (nutrientLevels.containsKey(nutrient)) {
			Integer nutrientLevel = nutrientLevels.get(nutrient);
			int newLevel = nutrientLevel.intValue() + amount;
			
			if (newLevel < 0)
				newLevel = 0;
			else if (newLevel > Config.maxNutrientLevel)
				newLevel = Config.maxNutrientLevel;
			
			nutrientLevels.put(nutrient, new Integer(newLevel));
		}
	}
	
	public void load(ConfigurationSection config) {
		ConfigurationSection nutrientConfig = config.getConfigurationSection("nutrients");
		
		for (String nutrientName : nutrientConfig.getKeys(false)) {
			for (Nutrient nutrient : Config.nutrients) {
				if (nutrient.getName().equals(nutrientName)) {
					nutrientLevels.put(nutrient,  new Integer(nutrientConfig.getInt(nutrientName)));
					break;
				}
			}
		}
	}
	
	public void save(ConfigurationSection config) {
		for (Nutrient nutrient : nutrientLevels.keySet()) {
			config.set("nutrients."+nutrient.getName(), nutrientLevels.get(nutrient));
		}
	}
}
