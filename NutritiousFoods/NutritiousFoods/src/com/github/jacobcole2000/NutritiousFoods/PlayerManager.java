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
	
	// saving and loading just use flat files that are saved and loaded on enable and disable
	// It could be changed to use a database and access as players login
	// but that is might be overkill for this, (though the dataset might get large (to include all players that have ever logged in))
	// which might be bad to keep in memory
	// so a database might be necessary.
	public void load() {
		// TODO: refactor this into the NutrientPlayer dehydrate method or something.
		BufferedReader inputStream = null;
		try {
			inputStream = new BufferedReader(new FileReader(filePath));
			
			String line;
			line = inputStream.readLine();
			// get the version
			int fileVersion = Integer.parseInt(line);
			if (fileVersion != dataVersion)
				plugin.getLogger().info("player data file format is old.");
			
			while (true) {
				line = inputStream.readLine();
				if (line == null)
					break;
				// ignore whitespace
				line = line.replaceAll(" *", "");
				
				String lineSplit[] = line.split("=");
				String name = lineSplit[0];
				line = lineSplit[1];
				
				NutrientPlayer nPlayer = new NutrientPlayer(null, plugin);
				
				String entries[] = line.split(",");
				for (String entry: entries) {
					lineSplit = entry.split(":");
					Nutrient nutrient = plugin.getNutrientByName(lineSplit[0]);
					if (nutrient == null)
						continue;
					Float value = Float.parseFloat(lineSplit[1]);
					nPlayer.setNutrientLevel(nutrient.getIndex(), value);
					
					unloggedPlayers.put(name, nPlayer);
				}
			}
			
		} 
		catch (FileNotFoundException e) {
			plugin.getLogger().info("no persistant nutrition data found at " + filePath);
			plugin.getLogger().info("file will be created automatically");
			// do nothing if the file does not exist
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void save() {
		BufferedWriter outputStream =null;
		try {
			outputStream = new BufferedWriter(new FileWriter(filePath));
			
			outputStream.write(Integer.toString(dataVersion)+"\n");
			
			for (String name: unloggedPlayers.keySet())
				outputStream.write(unloggedPlayers.get(name).dehydrate(name));
			for (String name: players.keySet())
				outputStream.write(players.get(name).dehydrate(name));
			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public NutrientPlayer get(String name) {
		return players.get(name);
	}
	
	public boolean contains(String name) {
		return players.containsKey(name);
	}
	
	public void addPlayer(String name, Player player) {
		NutrientPlayer nPlayer;
		if (unloggedPlayers.containsKey(name)) { // if the player has been recorded any time in the past, use that data
			nPlayer = unloggedPlayers.remove(name);
		}
		else { // else use the default for that player
			nPlayer = new NutrientPlayer(player,this.plugin);
		}

		nPlayer.setPlayer(player);
		players.put(name, nPlayer);
		int bucketIndex = (int)(Math.random()*playerBuckets.size());
		playerBuckets.get(bucketIndex).add(nPlayer);
	}
	
	public void removePlayer(Player player) {		
		for (ArrayList<NutrientPlayer> playerBucket:playerBuckets) {
			int toDelete = -1;
			for (int i =0; i<playerBucket.size(); i++) {
				NutrientPlayer nPlayer = playerBucket.get(i);
				if (nPlayer.getPlayer().getName().equals(player.getName())) {
					toDelete = i;
					break;
				}
			}
			
			if (toDelete != -1) {
				playerBucket.remove(toDelete);
				break;
			}
		}
		
		NutrientPlayer nPlayer = players.get(player.getName());
		// finalize player before deletion
		if (nPlayer!=null) {
			// saturation data is not persisted, so take care of this now
			plugin.saturationChange(nPlayer.getPlayer().getName());
		}
		
		players.remove(player.getName());
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
