package com.github.jacobcole2000.NutritiousFoods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

// Nutritious Foods -- the Nutrients Bukkit Plugin!
// created Jan 2013 by Jacob Cole (aka Namrufus)

// Adds depth to minecraft foods by applying good or bad potion effects if the player doesn't maintain healthy
// nutrient levels by eating a variety of foods.

public final class NutritiousFoods extends JavaPlugin implements Listener {
	// the time in seconds between effect ticks
	private long effectTickPeriod;
	// int to keep track of the current 'bucket' of players that will receive a
	// effect tick the next scheduled event
	int effectTickBucketIndex;
	// number of buckets used for scheduling purposes
	int bucketCount;
	// list of nutrient types
	ArrayList<Nutrient> nutrients;
	// a task that performs effect ticks
	BukkitTask nutrientEffectTask;
	HashMap<Material,NutrientFood> foodMap;
	
	PlayerManager playerManager;
	
	// --------------------------------------------- //
	
	ArrayList<Nutrient> getNutrients() {
		return nutrients;
	}
	
	public Nutrient getNutrientByName(String name) {
		for (Nutrient nutrient:nutrients) {
			if (nutrient.getName().equals(name))
				return nutrient;
		}
		return null;
	}
	
	// --------------------------------------------- //
	
	@Override
	public void onEnable() {
		this.getConfig();
		
		// perform check for config file
		if (!this.getConfig().isSet("NutritiousFoods")) {
			this.saveDefaultConfig();
			this.getLogger().info("Config did not exist or was invalid, default config saved.");
		}
		
		int expectedPlayerCap = this.getConfig().getInt("NutritiousFoods.expectedPlayerCap");
		effectTickPeriod = this.getConfig().getLong("NutritiousFoods.effectTickPeriod");
	
		// load modified food levels from config.yml
		Set<String> foodKeys = this.getConfig().getConfigurationSection("NutritiousFoods.foods").getKeys(false);
		foodMap = new HashMap<Material,NutrientFood>();
		for (String keyStr:foodKeys) {
			String name = keyStr;
			Material material = Material.getMaterial(name);
			if (material == null) {
				continue;
			}
			keyStr = "NutritiousFoods.foods."+keyStr;
			int modifiedLevel = this.getConfig().getInt(keyStr);
			getLogger().info(name + " " + Integer.toString(modifiedLevel));
			foodMap.put(material, new NutrientFood(modifiedLevel));
		}

		
		// load nutrition list from config.yml
		Set<String> nutrientKeys = this.getConfig().getConfigurationSection("NutritiousFoods.nutrients").getKeys(false);
		nutrients = new ArrayList<Nutrient>(nutrientKeys.size());
		int index = 0;
		for (String keyStr:nutrientKeys) {
			String name = keyStr;
			keyStr = "NutritiousFoods.nutrients."+keyStr;
			float maxLevel = (float)this.getConfig().getDouble(keyStr+".maxLevel");
			float maxDecRate = (float)this.getConfig().getDouble(keyStr+".maxDecRate");
			float exponant = (float)this.getConfig().getDouble(keyStr+".exponant");
			float spawnLevel = (float)this.getConfig().getDouble(keyStr+".spawnLevel");
			Nutrient nutrient = new Nutrient(index, name, maxLevel, maxDecRate, exponant, spawnLevel);
			nutrients.add(nutrient);
			index++;
			
			// get foods map
			ConfigurationSection confSection = this.getConfig().getConfigurationSection(keyStr+".foods");
			if (confSection != null) {
				for (String keyStr2: confSection.getKeys(false)) {
					String foodName = keyStr2;
					keyStr2 = keyStr + ".foods."+keyStr2;
					
					Material material = Material.getMaterial(foodName);
					if (material == null) {
						this.getLogger().info("configuration error: 'food' "+foodName+" is not a valid material.");
						continue;
					}
					float amount = (float)this.getConfig().getDouble(keyStr2);
					NutrientFood nFood;
					if (foodMap.containsKey(material))
						nFood = foodMap.get(material);
					else {
						nFood = new NutrientFood();
						foodMap.put(material, nFood);
					}
					nFood.addNutrient(nutrient, amount);
				}
			}
			
			//get effects list for this nutrient
			confSection = this.getConfig().getConfigurationSection(keyStr+".effects");
			if (confSection != null) {
				for (String keyStr2: confSection.getKeys(false)) {
					keyStr2 = keyStr+".effects."+keyStr2;
					String effectTypeName = this.getConfig().getString(keyStr2+".effect");
					PotionEffectType effectType = PotionEffectType.getByName(effectTypeName);
					if (effectType == null) {
						this.getLogger().info("configuration error: 'effect type' "+effectTypeName+" is not a valid potion effect.");
						continue;
					}
					
					boolean moreThan = this.getConfig().getBoolean(keyStr2+".moreThan");
					float cutoff = (float)this.getConfig().getDouble(keyStr2+".cutoff");
					float chanceMax = (float)this.getConfig().getDouble(keyStr2+".chanceMax");
					int intensityMax = this.getConfig().getInt(keyStr2+".intensityMax");
					int durationMax = this.getConfig().getInt(keyStr2+".durationMax");
					
					
					
					NutrientBuff nutrientBuff = new NutrientBuff(effectType, moreThan, cutoff, chanceMax, intensityMax, durationMax, nutrient);
					nutrient.addBuff(nutrientBuff);
				}
			}
		}
		
		effectTickBucketIndex = 0;
		
		long ticksPerBucket;
		long ticksPerPeriod = effectTickPeriod;
		// if the max number of players approaches the number of ticks in the period
		// then go ahead and match the number of buckets to the number of ticks
		// if the max number of players is smaller than the number of ticks per period
		// match the number of ticks closely to the expected number of players
		if (expectedPlayerCap > ticksPerPeriod) {
			bucketCount = (int)ticksPerPeriod;
			ticksPerBucket = 1L;
		}
		else {
			bucketCount = expectedPlayerCap;
			ticksPerBucket = Math.round((float)ticksPerPeriod/(float)expectedPlayerCap);
			bucketCount = Math.round((float)ticksPerPeriod/(float)ticksPerBucket);
		}
		
		// initialize player data
		Player players[] = getServer().getOnlinePlayers();
		playerManager = new PlayerManager("playerdata.txt", bucketCount, this);
		playerManager.load();
		
		// populate player buckets and map with current players
		for (int i=0; i<players.length; i++) {
			playerManager.addPlayer(players[i].getName(), players[i]);
		}
		
		//register the potion effect event
		nutrientEffectTask = this.getServer().getScheduler().runTaskTimer(this, new Runnable() {
		    @Override  
		    public void run() {
		        NutrientEffectEvent();
		    }
		}, 0L, ticksPerBucket);
		
		// register events
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	// stop effect tick and nullify all references
	// also save player data
	@Override
	public void onDisable() {
		playerManager.save();
		
		nutrientEffectTask.cancel();
		
		nutrients = null;
		playerManager = null;
	}
	
	public void NutrientEffectEvent() {
		effectTickBucketIndex++;
		if (effectTickBucketIndex >= playerManager.getBucketCount())
			effectTickBucketIndex = 0;
		
		for (NutrientPlayer nPlayer: playerManager.getPlayersInBucket(effectTickBucketIndex)) {
			for (Nutrient nutrient: nutrients) {
				nPlayer.applyEffects(nutrient);
			}
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		// if the entity is not a player, return
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player)event.getEntity();
		
		String playerName = player.getName();
		
		if (!playerManager.contains(playerName))
			return;
		NutrientPlayer nutrientPlayer = playerManager.get(playerName);

		for (int i=0; i<nutrients.size(); i++) {
			Nutrient nutrient = nutrients.get(i);
			nutrientPlayer.decNutrientLevel(i,nutrient);
		}
		
		// if our hunger is going up and if the thing eaten is a valid material, add any nutrient amounts
		// also modifiy hunger level increase if needed.
		int hungerDifference = event.getFoodLevel() - player.getFoodLevel();
		if (hungerDifference > 0 && foodMap.containsKey(nutrientPlayer.getLastInteractMaterial())) {
			NutrientFood nFood = foodMap.get(nutrientPlayer.getLastInteractMaterial());
			nFood.addToNutrients(nutrientPlayer);
			if (nFood.doesModifyHungerLevel()) {
				int newLevel = nutrientPlayer.getPlayer().getFoodLevel() + nFood.getHungerLevel();
				if (newLevel > 20)
					newLevel = 20;
				if (newLevel < 0)
					newLevel = 0;
				event.setFoodLevel(newLevel);
			}
		}
		else {
			nutrientPlayer.verboseMessage("Food change event, 1 nutrient level lost");
		}

		// run a task next tick to handle saturation changes
		// possible collision if a exhaustion event happens at the
		// same time as an eat event, but this is rare and has minor consequences (I think)
		final String name = player.getName();
		this.getServer().getScheduler().runTaskLater(this, new Runnable() {
		    @Override  
		    public void run() {
		        saturationChange(name);
		    }
		}, 1L);
	}
	
	// update saturation levels
	public void saturationChange(String playerName) {
		if (!playerManager.contains(playerName))
			return;
		NutrientPlayer nPlayer = playerManager.get(playerName);
		int satLevelsLost = -nPlayer.updateSatLevel(nPlayer.getPlayer().getSaturation());
		
		if (satLevelsLost <= 0)
			return;
		
		nPlayer.verboseMessage("§7Saturation change event, saturation = " + Double.toString(nPlayer.getPlayer().getSaturation()));
		nPlayer.verboseMessage("§7Saturation Levels lost (nutrient levels lost) = " + Integer.toString(satLevelsLost));
		
		for (int i = 0; i < satLevelsLost; i++) {
			for (int j = 0; j<nutrients.size(); j++) {
				nPlayer.decNutrientLevel(j, nutrients.get(j));
			}
		}
	}
	
	// catch the type of food the player is eating
	// hacky workaround for the fact that there is no playerEatEvent
	// though there is a pull request for this (PlayerConsumeEvent) -- check on that later
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		// right click signifies start of eating
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// set the last interacted material to the held item
			// maybe food, maybe not, who knows
			NutrientPlayer nPlayer = playerManager.get(event.getPlayer().getName());
			nPlayer.setLastInteractMaterial(event.getPlayer().getItemInHand().getType());
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		playerManager.addPlayer(event.getPlayer().getName(), event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		playerManager.removePlayer(event.getPlayer());
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("nfcheck") || cmd.getName().equalsIgnoreCase("nfc")){
			if (! (sender instanceof Player)) {
				sender.sendMessage("This is a player command.");
				return false;
			}
			
			NutrientPlayer nutrientPlayer = playerManager.get(sender.getName());
			
			String msg = String.format("§7Nutrient                 Level              Rate");
			sender.sendMessage(msg);
			for (int i=0; i<nutrients.size(); i++) {
				Nutrient nutrient = nutrients.get(i);
				float level = nutrientPlayer.getNutrientLevel(i);
				msg = String.format("§8%-20s %5.1f/%-5.1f %15.5f", nutrient.getName(), level, nutrient.getMaxLevel(), nutrient.nutrientLevelDecrement(level));
				sender.sendMessage(msg);
			}
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("nfinfo") || cmd.getName().equalsIgnoreCase("nfi")) {
			// TODO: optimize this entire command, maybe construct description string 
			
			if (args.length < 1) {
				sender.sendMessage("§7Please include the name or index of the desired nutrient.");
				return true;
			}
			
			String nName = args[0];
			// lookup nutrients, might be too expensive
			Nutrient nutrient = null;
			for (Nutrient n : nutrients) {
				if (n.getName().equals(nName)) {
					nutrient = n;
					break;
				}
			}
			
			if (nutrient == null) {
				if (!StringUtils.isNumeric(nName))  {
					sender.sendMessage("§7"+nName+" not a valid nutrient");
					return true;
				}
				
				int index = (int)Double.parseDouble(nName);
				if (index < 0 || index >= nutrients.size()) {
					sender.sendMessage("§7"+nName+" not a valid nutrient");
					return true;
				}
				
				nutrient = nutrients.get(index);
				
				if (nutrient == null) {
					sender.sendMessage("§7"+nName+" not a valid nutrient");
					return true;
				}
			}
			
			sender.sendMessage("§7Nutrient report: "+nutrient.getName());
			sender.sendMessage("§8max level: "+Float.toString(nutrient.getMaxLevel()));
			sender.sendMessage("§7§oFoods:");
			// another inconveniently unwieldy method for getting all the foods
			for (Material mat: foodMap.keySet()) {
				NutrientFood nFood = foodMap.get(mat);
				float amount = nFood.getAmount(nutrient);
				if (amount > 0)
					sender.sendMessage("§8"+mat.name()+" restores "+Float.toString(-amount));
				else if (amount < 0)
					sender.sendMessage("§8"+mat.name()+" depletes "+Float.toString(amount));
			}

			sender.sendMessage("§7§oEffects:");
			for (NutrientBuff nBuff: nutrient.getBuffs()) {
				if (!nBuff.isMoreThan())
					sender.sendMessage("§8"+nBuff.getEffectType().getName()+" effect below level "+Float.toString(nBuff.getCutoff()));
				else
					sender.sendMessage("§8"+nBuff.getEffectType().getName()+" effect above level "+Float.toString(nBuff.getCutoff()));
			}
			
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("nfverbose") || cmd.getName().equalsIgnoreCase("nfv")) {
			if (! (sender instanceof Player)) {
				sender.sendMessage("This is a player command.");
				return false;
			}
			
			NutrientPlayer nutrientPlayer = playerManager.get(sender.getName());
			
			nutrientPlayer.toggleVerboseMode();
			
			if (nutrientPlayer.inVerboseMode())
				sender.sendMessage("§7Verbose Mode Enabled");
			else
				sender.sendMessage("§7Verbose Mode Disabled");
			
			return true;
		}
		return false; 
	}
}
