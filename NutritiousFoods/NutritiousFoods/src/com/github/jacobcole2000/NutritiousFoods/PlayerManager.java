package com.github.jacobcole2000.NutritiousFoods;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;

public class PlayerManager {
	// version of the data storage format to be placed at the top of the text file
	private final int dataVersion = 1;
	
	NutritiousFoods plugin;
	private String filePath;
	
	// player data is accessible three ways
	// the bucket data makes it easy to access a segment(relatively evenly size) of players for iteration
	// a hashmap for quick lookup of logged-in players and
	// a hash map for easy lookup of unlogged-in player data
	private ArrayList<ArrayList<NutrientPlayer>> playerBuckets;
	HashMap<String, NutrientPlayer> unloggedPlayers;
	HashMap<String, NutrientPlayer> players;
	
	public PlayerManager(String filePath, int bucketCount, NutritiousFoods plugin) {
		this.filePath = filePath;
		this.plugin = plugin;
		
		playerBuckets = new ArrayList<ArrayList<NutrientPlayer>>(bucketCount);
		for (int i =0; i < bucketCount; i++) {
			playerBuckets.add(new ArrayList<NutrientPlayer>());
		}
		
		players = new HashMap<String, NutrientPlayer>();
		unloggedPlayers = new HashMap<String, NutrientPlayer>();
	}
	
	public void load() {
		
	}
	
	public void save() {
		
	}
	
	public NutrientPlayer get(String name) {
		return players.get(name);
	}
	
	public boolean contains(String name) {
		return players.containsKey(name);
	}
	
	public void addPlayer(String name, Player player) {
		NutrientPlayer nPlayer;
		if (unloggedPlayers.containsKey(name)) // if the player has been recorded any time in the past, use that data
			nPlayer = unloggedPlayers.remove(name);
		else // else use the default for that player
			nPlayer = new NutrientPlayer(player,this.plugin);

		nPlayer.setPlayer(player);
		players.put(name, nPlayer);
		int bucketIndex = (int)(Math.random()*playerBuckets.size());
		playerBuckets.get(bucketIndex).add(nPlayer);
	}
	
	public void removePlayer(Player player) {
		for (ArrayList<NutrientPlayer> playerBucket:playerBuckets) {
			if (!playerBucket.contains(player))
				continue;
			 playerBucket.remove(player);
		}
		NutrientPlayer nPlayer = players.remove(player.getName());
		nPlayer.setPlayer(null);
		unloggedPlayers.put(player.getName(), nPlayer);
		
	}
	
	public ArrayList<NutrientPlayer> getPlayersInBucket(int index) {
		return playerBuckets.get(index);
	}
	
	public int getBucketCount() {
		return playerBuckets.size();
	}
}
