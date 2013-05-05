package com.github.jacobcole2000.NutritiousFoods;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

// Nutritious Foods -- the Nutrients Bukkit Plugin!

// Adds depth to minecraft foods by applying good or bad potion effects if the player doesn't maintain healthy
// nutrient levels by eating a variety of foods.

public final class NutritiousFoods extends JavaPlugin implements Listener {
	public HashMap<String, PlayerData> players;
	
	NutrientDrainManager nutrientDrainManager;
	EffectManager effectManager;
	EatListener eatListener;
	
	// --------------------------------------------- //
	
	@Override
	public void onEnable() {
		this.getConfig();
		
		// perform check for config file
		if (!this.getConfig().isSet("nutritious-foods")) {
			this.saveDefaultConfig();
			this.getLogger().info("Config did not exist or was invalid, default config saved.");
		}
		this.reloadConfig();
	
		// load configuration data
		Config.load(this, this.getConfig().getConfigurationSection("nutritious-foods"));
		
		players = new HashMap<String, PlayerData>();
		
		// load player data
        File file = new File(getDataFolder(), "PlayerData.yml");
	    FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(file);
	    for (String playerName: playerDataConfig.getKeys(false)) {
	    	PlayerData playerData = new PlayerData();
	    	
	    	playerData.load(playerDataConfig.getConfigurationSection(playerName));
	    	
	    	players.put(playerName, playerData);
	    }
		
	    effectManager = new EffectManager(this);
	    nutrientDrainManager = new NutrientDrainManager(this);
	    eatListener = new EatListener(this);
	    
		// register events
		getServer().getPluginManager().registerEvents(eatListener, this);
	}
	
	// stop effect tick and nullify all references
	// also save player data
	@Override
	public void onDisable() {
		getLogger().info("Saving player nutrition data...");
		// stop tasks
		nutrientDrainManager.stop();
		effectManager.stop();
		
		// save player data
		try {
	        File file = new File(getDataFolder(), "PlayerData.yml");
	        file.delete();
	        file = new File(getDataFolder(), "PlayerData.yml");
		    FileConfiguration playerDataConfig = YamlConfiguration.loadConfiguration(file);
		    for (String playerName: players.keySet()) {
		    	PlayerData data = players.get(playerName);
		    	
	    		data.save(playerDataConfig.createSection(playerName));
		    }
		    playerDataConfig.save(file);
		} catch(IOException e) {
	    	getLogger().warning("Can't save player data!");
	    	e.printStackTrace();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("nfcheck") || cmd.getName().equalsIgnoreCase("nfc")){
			if (! (sender instanceof Player)) {
				sender.sendMessage("This is a player command.");
				return false;
			}
			
			String msg = String.format("§7Nutrients");
			sender.sendMessage(msg);
			
			PlayerData pData = players.get(sender.getName());
			
			Map<Nutrient, Integer> nutrientLevels = pData.getNutrientLevels();
			for (Nutrient nutrient: nutrientLevels.keySet()) {
				int level = nutrientLevels.get(nutrient);
				msg = String.format("§7%s :", nutrient.getName());
				sender.sendMessage(msg);
				msg = "§c"/*red*/;
				for (int i=0; i<Config.maxNutrientLevel; i++) {
					if (i < level+1) {
						if (i > Config.maxNutrientLevel/2)
							msg+="§7"/*light grey*/;
						else if (i > Config.maxNutrientLevel/4)
							msg+="§e"/*yellow*/;
						else
							msg+="§c"/*light red*/;
					} else {
						if (i > Config.maxNutrientLevel/2)
							msg+="§8"/*dark grey*/;
						else if (i > Config.maxNutrientLevel/4)
							msg+="§6"/*gold*/;
						else
							msg+="§4"/*dark red*/;
					}
					
					int nutrientsPerHour;
					if (level > Config.maxNutrientLevel / 2)
						nutrientsPerHour = (int)((20.0 * 60.0 * 60.0/*ticks per hour*/) / (double)Config.nutrientDecrementPeriod);
					else
						nutrientsPerHour = (int)((20.0 * 60.0 * 60.0/*ticks per hour*/) / (double)Config.nutrientSlowDecrementPeriod);
					if (i==0 || i==Config.maxNutrientLevel-1 || i==Config.maxNutrientLevel/2) {
						if (i <= level && i >= level - nutrientsPerHour)
							msg += ";";
						else
							msg+=":";
					}
					else {
						if (i <= level && i >= level - nutrientsPerHour)
							msg += ",";
						else
							msg+=".";
					}
				}
				sender.sendMessage(msg);
			}
		}
		else if (cmd.getName().equalsIgnoreCase("nfinfo") || cmd.getName().equalsIgnoreCase("nfi")) {
			// TODO: optimize this entire command, maybe construct description string 
			
			sender.sendMessage("§7Nutrient Info: ");
			
			return true;
		}
		return false; 
	}
}
