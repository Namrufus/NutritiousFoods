package com.github.jacobcole2000.NutritiousFoods;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class Config {
	public static String filePath;
	
	public static long effectTickPeriod;
	
	public static int maxNutrientLevel;
	public static int startNutrientLevel;
	public static long nutrientDecrementPeriod;
	public static long nutrientSlowDecrementPeriod;
	
	public static double hungerLevelDecreaseChance;
	
	public static Map<Material, Food> foods;
	public static List<Nutrient> nutrients;
	
	public static void load(NutritiousFoods plugin, ConfigurationSection config) {
		filePath = config.getString("file-path");
		
		effectTickPeriod = config.getLong("effect-tick-period");
		
		maxNutrientLevel = config.getInt("max-nutrient-level");
		startNutrientLevel = config.getInt("start-nutrient-level");
		double nutrientLevelsPerHour = config.getDouble("nutrient-levels-per-hour");
		nutrientDecrementPeriod = (long)((20*60*60/*ticks per hour*/)/nutrientLevelsPerHour); /*ticks per nutrient level*/
		double nutrientLevelsPerHourSlow = config.getDouble("nutrient-levels-per-hour-slow");
		nutrientSlowDecrementPeriod = (long)((20*60*60/*ticks per hour*/)/nutrientLevelsPerHourSlow); /*ticks per nutrient level*/
		
		hungerLevelDecreaseChance = config.getDouble("hunger-level-decrease-chance");
		
		nutrients = new LinkedList<Nutrient>();
		for (String nutrientName : config.getConfigurationSection("nutrients").getKeys(false)) {
			nutrients.add(new Nutrient(nutrientName, config.getConfigurationSection("nutrients."+nutrientName)));
		}
		
		foods = new HashMap<Material, Food>();
		for (String foodName : config.getConfigurationSection("foods").getKeys(false)) {
			Material material = Material.getMaterial(foodName);
			
			if (material == null) {
				plugin.getLogger().warning("When loading food configs, "+foodName+" is not a valid material.");
			}
			
			ConfigurationSection foodConfig = config.getConfigurationSection("foods."+foodName);
			Food food = new Food(foodConfig, nutrients);
			foods.put(material, food);
		}
	}
}
