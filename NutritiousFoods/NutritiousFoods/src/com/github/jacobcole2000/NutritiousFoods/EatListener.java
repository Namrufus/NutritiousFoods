package com.github.jacobcole2000.NutritiousFoods;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class EatListener implements Listener {
	private NutritiousFoods plugin;
	
	public EatListener(NutritiousFoods plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler 
	public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
		Player player = event.getPlayer();
		
		plugin.getLogger().info(event.getItem().getType().toString());
		
		ItemStack itemStack = event.getItem();
		Material foodMaterial = itemStack.getType();
		
		// handle hunger, saturation, and nutrition level changes
		PlayerData playerData = plugin.players.get(player.getName());
		
		if (!Config.foods.containsKey(foodMaterial)) {
			playerData.setEatenFood(null);
			return;
		}
		
		playerData.setEatenFood(foodMaterial);
		
		Food nFood = Config.foods.get(foodMaterial);
		for (Food.NutrientRestore nutrientRestore : nFood.nutrientRestores) {
			playerData.addNutrientLevel(nutrientRestore.nutrient, nutrientRestore.amount);
		}

		int newLevel = player.getFoodLevel() + nFood.hunger;
		if (newLevel > 20)
			newLevel = 20;
		else if (newLevel < 0)
			newLevel = 0;
		player.setFoodLevel(newLevel);
		
		double newSat = player.getSaturation() + nFood.saturation;
		if (newSat > newLevel)
			newSat = (double)newLevel;
		else if (newSat < 0.0)
			newSat = 0.0;
		player.setSaturation((float)newSat);
		
		// remember the food so that the food level event may be canceled later
	}
	
	@EventHandler 
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {

		if (!(event.getEntity() instanceof Player))
			return;
		
		// reduce hunger depletion rate
		if (event.getFoodLevel() < ((Player)event.getEntity()).getFoodLevel()) {
			if (Math.random() < Config.hungerLevelDecreaseChance) {
				event.setCancelled(true);
				return;
			}
		}
			
		// if the player has just eaten a registered food and the food level is rising, then
		// cancel the event, as the food level has already been changed on the consume event.
		PlayerData playerData = plugin.players.get(event.getEntity().getName());
		if (playerData == null || playerData.getEatenFood() == null)
			return;
		
		playerData.setEatenFood(null);
		
		if (event.getFoodLevel() >= ((Player)event.getEntity()).getFoodLevel())
			event.setCancelled(true);
	}
}
