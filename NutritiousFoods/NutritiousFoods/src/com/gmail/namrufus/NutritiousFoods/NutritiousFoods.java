package com.gmail.namrufus.NutritiousFoods;

import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

// Nutritious Foods -- the Nutrients and Farming Bukkit Plugin!
// created Jan 2013 by Jacob Cole (aka Namrufus)

// Adds depth to minecraft foods by applying good or bad potion effects if the player doesn't maintain healthy
// nutrient levels by eating a variety of foods. Also adds realism to farming by allowing crops to grow even while chunks are unloaded.
// Crop growth speeds can be dependent on conditions such as the biome, sunlight levels, soil conditions and more.
// Fully customizable. Nutrient levels and effects as well as farm growth rates and conditions are fully specifiable. 

public final class NutritiousFoods extends JavaPlugin implements Listener {
	// the time in seconds between effect ticks
	private float effectTickPeriod;
	// int to keep track of the current 'bucket' of players that will recieve a
	// effect tick the next scheduled event
	int effectTickBucketIndex;
	// number of buckets used for scheduling purposes
	int bucketCount;
	// list of nutrient types
	ArrayList<Nutrient> nutrients;
	// players divided into groups for scheduling purpose
	// spreading out computation over many ticks
	ArrayList<ArrayList<NutrientPlayer>> playerBuckets;
	// a task that performs effect ticks
	BukkitTask nutrientEffectTask;
	
	// --------------------------------------------- //
	
	ArrayList<Nutrient> getNutrients() {
		return nutrients;
	}
	
	// --------------------------------------------- //
	
	@Override
	public void onEnable() {
		// perform check for config file
		if (!this.getConfig().isSet("exists")) {
			this.saveDefaultConfig();
			this.getLogger().info("[NutritiousFoods] Config did not exist or was invalid, default config saved.");
		}
		
		int expectedPlayerCap = this.getConfig().getInt("expectedPlayerCap");
		effectTickPeriod = (float)this.getConfig().getDouble("effectTickPeriod");
		
		this.getLogger().info("effectTickPeriod: " + Float.toString(effectTickPeriod));
		this.getLogger().info("expectedPlayerCap: " + Integer.toString(expectedPlayerCap));
		
		// TODO: load nutrition list from config
		
		effectTickBucketIndex = 0;
		
		long ticksPerBucket;
		long ticksPerPeriod = (long)(effectTickPeriod*20.0);
		// if the max number of players approaches the number of ticks in the period
		// then go ahead and match the number of buckets to the number of ticks
		// if the max number of players is much smaller than the number of ticks per period
		// match the number of ticks closely to the expected number of players
		if (expectedPlayerCap*3 > ticksPerPeriod) {
			bucketCount = (int)ticksPerPeriod;
			ticksPerBucket = 1L;
		}
		else {
			bucketCount = expectedPlayerCap;
			ticksPerBucket = Math.round((float)ticksPerPeriod/(float)expectedPlayerCap);
			bucketCount = Math.round((float)ticksPerPeriod/(float)ticksPerBucket);
		}
		
		this.getLogger().info("effectTickPeriod: " + Float.toString(effectTickPeriod));
		this.getLogger().info("expectedPlayerCap: " + Integer.toString(expectedPlayerCap));
		this.getLogger().info("effectTickPeriod: " + Long.toString(ticksPerBucket));
		this.getLogger().info("effectTickPeriod: " + Integer.toString(bucketCount));
		
		// initialize player buckets
		Player players[] = getServer().getOnlinePlayers();
		int playersPerBucket = 2*players.length/bucketCount;
		playerBuckets = new ArrayList<ArrayList<NutrientPlayer>>(bucketCount);
		for (int i = 0; i < bucketCount; i++) {
			playerBuckets.add(new ArrayList<NutrientPlayer>(playersPerBucket));
		}
		
		// populate player buckets with current players
		for (int i=0; i<players.length; i++) {
			int bucketIndex = (int)(Math.random()*bucketCount);
			// TODO search for players using SQL or something
			// currently resets nutrients at login
			playerBuckets.get(bucketIndex).add(new NutrientPlayer(players[i],this));
		}
		
		//register the potion effect event
		nutrientEffectTask = this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
		    @Override  
		    public void run() {
		        NutrientEffectEvent();
		    }
		}, 0L, ticksPerBucket);
		
		// register events
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	// stop tacks and nullify all references
	@Override
	public void onDisable() {
		nutrientEffectTask.cancel();
		
		nutrients = null;
		playerBuckets = null;
		nutrientEffectTask = null;
	}
	
	public void NutrientEffectEvent() {
		
	}
	
	private void removePlayer(Player player) {
		for (ArrayList<NutrientPlayer> playerBucket:playerBuckets) {
			if (!playerBucket.contains(player))
				continue;
			//TODO save data to database
			playerBucket.remove(player);
		}
	}
	
	private void backupPlayer(Player player) {
		//TODO.... everything
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		System.out.println("player added.");
		//TODO search for nutrient data in a database
		
		int bucketIndex = (int)(Math.random()*bucketCount);
		playerBuckets.get(bucketIndex).add(new NutrientPlayer(event.getPlayer(),this));
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		removePlayer(event.getPlayer());
	}
}
