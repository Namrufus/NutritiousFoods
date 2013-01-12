package com.github.jacobcole2000.NutritiousFoods;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

// represents the state of a player's nutrition

public class NutrientPlayer {
	// record of the player's nutrient levels
	// parallel to the plugin's nutrient arraylist
	private float[] nutrientLevels;
	// link to the player -- THIS MAY BE NULL BY DESIGN
	// probably need to change that ^^^
	Player player;
	// the plugin
	NutritiousFoods plugin;
	// previous saturation level for saturation level  calculations
	float prevSatLevel;
	// "leftover" saturation
	float leftoverSatLevel;
	// store the last thing 'interacted' with in order to determine what food has been eatenized
	private Material lastInteractMaterial;
	private boolean verboseMode;
	
	// construct a player that has never logged in to the server before
	// create new nutrient data
	NutrientPlayer(Player player, NutritiousFoods plugin) {
		this.player = player;
		this.plugin = plugin;
		this.lastInteractMaterial = Material.AIR;
		
		if (player != null)
			this.prevSatLevel = player.getSaturation();
		else
			this.prevSatLevel = 0.0f;
		this.leftoverSatLevel = 0.0f;
		
		verboseMode = false;
		
		ArrayList<Nutrient> nutrientDefs = plugin.getNutrients();
		nutrientLevels = new float[nutrientDefs.size()];
		for (int i=0; i<nutrientDefs.size(); i++)
			nutrientLevels[i] = nutrientDefs.get(i).getSpawnLevel();
	}
	
	// update the stored saturation level
	// this allows for the nutrition levels to go down with saturation decreases as well
	// return value is the amount that the saturation decreased.
	public int updateSatLevel(float newLevel) {
		// get the amount by which the saturation level has changed
		float diff = leftoverSatLevel + newLevel - prevSatLevel;

		prevSatLevel = newLevel;
		
		// if the the sat level has increased, do nothing
		if (diff>0.0)
			return 0;
		// round down to an integer and store the remainder
		int res = (int) diff;
		leftoverSatLevel = diff-(float)res;
		return res;
	}
	
	public void applyEffects(Nutrient nutrient) {
		int index = nutrient.getIndex();
		ArrayList<NutrientBuff> buffs = nutrient.getBuffs();
		for (NutrientBuff buff:buffs) {
			PotionEffect effect = buff.getEffect(nutrientLevels[index]);
			if (effect != null) {
				effect.apply(player);
				if (!buff.isMoreThan())
					verboseMessage(effect.getType().getName() + " applied because of " + nutrient.getName() + " deficiency.");
				else
					verboseMessage(effect.getType().getName() + " applied because of " + nutrient.getName() + " excess.");
			}
		}
	}
	
	// ---------------------------------------//
	// verbose mode
	
	public boolean inVerboseMode() {
		return verboseMode;
	}
	
	public void toggleVerboseMode() {
		verboseMode = !verboseMode;
	}
	
	public void verboseMessage(String msg) {
		if (verboseMode)
			player.sendMessage("§7[nfoods] "+msg);
	}
	
	
	// accessors and mutators
	// ------------------------------------ //
	

	public Player getPlayer() {
		return player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
		if (player != null)
			this.prevSatLevel = player.getSaturation();
	}
	
	float getNutrientLevel(int index) {
		return nutrientLevels[index];
	}
	
	void setNutrientLevel(int index, float value) {
		nutrientLevels[index] = value;
	}
	
	void addNutrientLevel(Nutrient nutrient, float amount) {
		int index = nutrient.getIndex();
		float level = nutrientLevels[index] + amount;
		
		if (level < 0.0)
			level = 0.0f;
		if (level > nutrient.getMaxLevel())
			level = nutrient.getMaxLevel();

		nutrientLevels[index] = level;
	}
	
	void decNutrientLevel(int index, Nutrient nutrient) {
		float level = nutrientLevels[index];
		level -= nutrient.nutrientLevelDecrement(level);

		if (level < 0.0)
			level = 0.0f;
		if (level > nutrient.getMaxLevel())
			level = nutrient.getMaxLevel();
		
		nutrientLevels[index] = level;
	}
	
	public Material getLastInteractMaterial() {
		return lastInteractMaterial;
	}
	
	public void setLastInteractMaterial(Material mat) {
		lastInteractMaterial = mat;
	}
	
	public String dehydrate(String name) {
		String line = "";
		line += name + "=";
		for (int i=0; i<nutrientLevels.length; i++) {
			line += plugin.getNutrients().get(i).getName();
			line += ":";
			line += Float.toString(nutrientLevels[i]);
			if (i!=nutrientLevels.length-1)
				line += ",";
		}
		line += "\n";
		return line;
	}
}
