package com.github.jacobcole2000.NutritiousFoods;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class NutrientDrainManager {	
	NutritiousFoods plugin;
	private BukkitTask nutrientTask;
	private BukkitTask slowNutrientTask;
	
	public NutrientDrainManager(NutritiousFoods plugin) {
		this.plugin = plugin;
		
		nutrientTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
		    @Override  
		    public void run() {
				onNutrientTick(false);
		    }
		}, Config.nutrientDecrementPeriod, Config.nutrientDecrementPeriod);
		
		slowNutrientTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
		    @Override  
		    public void run() {
		    	onNutrientTick(true);
		    }
		}, Config.nutrientSlowDecrementPeriod, Config.nutrientSlowDecrementPeriod);
	}
	
	public void stop() {
		nutrientTask.cancel();
		slowNutrientTask.cancel();
	}
	
	public void onNutrientTick(boolean slow) {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			if (!plugin.players.containsKey(player.getName()))
				plugin.players.put(player.getName(), new PlayerData());
			
			PlayerData playerData = plugin.players.get(player.getName());
			
			playerData.decrementNutrientLevels(slow);
		}
	}
}