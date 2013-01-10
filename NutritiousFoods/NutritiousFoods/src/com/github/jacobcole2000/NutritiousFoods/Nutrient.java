package com.github.jacobcole2000.NutritiousFoods;

import java.util.ArrayList;

public class Nutrient {
	// rate of nutrient depletion as a function of nutrient level
	// n' = b*(n^e)
	private float b, e;
	private float maxLevel;
	private float spawnLevel; // the initial level at first spawn
	private String name;
	int index;
	ArrayList<NutrientBuff> nutrientBuffs;
	
	public Nutrient (int index, String name, float maxLevel, float decRateMax, float exponant, float spawnLevel) {
		this.index = index;
		this.name = name;
		this.maxLevel = maxLevel;
		this.spawnLevel = spawnLevel;
		
		this.e = exponant;
		this.b = decRateMax/((float)Math.pow(maxLevel, e));
		
		this.nutrientBuffs = new ArrayList<NutrientBuff>();
	}
	
	// decrement level (this is called whenever the player loses a
	// hunger level)
	public float nutrientLevelDecrement(float level) {
		return b*(float)Math.pow(level, e);
	}
	
	public void addBuff(NutrientBuff nutrientBuff) {
		nutrientBuffs.add(nutrientBuff);
	}
	
	public ArrayList<NutrientBuff> getBuffs() {
		return nutrientBuffs;
	}
	
	// accessors/mutators
	// ***************************** //
	public float getMaxLevel() {
		return maxLevel;
	}
	
	public float getSpawnLevel() {
		return spawnLevel;
	}
	
	public String getName() {
		return name;
	}
	
	public int getIndex() {
		return index;
	}
}
	