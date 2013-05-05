package com.github.jacobcole2000.NutritiousFoods;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

// represents a food that may be eaten in order to increase
// levels on nutrients for a player

public class Food {
	public int hunger;
	public double saturation;
	public List<NutrientRestore> nutrientRestores;
	
	public class NutrientRestore {
		public NutrientRestore(Nutrient nutrient, int amount) {
			this.nutrient = nutrient;
			this.amount = amount;
		}
		public Nutrient nutrient;
		public int amount;
	}
	
	public Food(ConfigurationSection config, List<Nutrient> nutrients) {
		hunger = config.getInt("hunger");
		saturation = config.getDouble("saturation");
		
		nutrientRestores = new LinkedList<NutrientRestore>();
		
		for (String nutrientName : config.getKeys(false)) {
			boolean foundNutrient = false;
			for (Nutrient nutrient : nutrients) {
				if (nutrientName.equals(nutrient.getName())) {
					foundNutrient = true;
					nutrientRestores.add(new NutrientRestore(nutrient, config.getInt(nutrientName)));
					break;
				}
			}
			if (!foundNutrient) {
				
			}
		}
	}
}
