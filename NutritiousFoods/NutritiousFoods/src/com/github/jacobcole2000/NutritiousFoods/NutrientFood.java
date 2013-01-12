package com.github.jacobcole2000.NutritiousFoods;

import java.util.ArrayList;

// represents a food that may be eaten in order to increase
// levels on nutrients for a player

public class NutrientFood {
	// specifies the if and amount of hunger added by this food
	boolean modifiesHungerLevel;
	int hungerLevel;
	// amounts that the varieties of nutrients are added by consuming this food
	ArrayList<NutrientAmount> nAmounts;
	
	public NutrientFood(int hungerLevel) {
		modifiesHungerLevel = true;
		this.hungerLevel = hungerLevel;
		nAmounts = new ArrayList<NutrientAmount>();
	}
	public NutrientFood() {
		modifiesHungerLevel = false;
		this.hungerLevel = 0;
		nAmounts = new ArrayList<NutrientAmount>();
	}
	
	public boolean doesModifyHungerLevel() {
		return modifiesHungerLevel;
	}
	
	public int getHungerLevel() {
		return hungerLevel;
	}
	
	public void addNutrient(Nutrient nutrient, float amount) {
		nAmounts.add(new NutrientAmount(nutrient, amount));
	}
	
	public void addToNutrients(NutrientPlayer nPlayer) {
		for (NutrientAmount nAmount:nAmounts) {
			nPlayer.addNutrientLevel(nAmount.nutrient, nAmount.amount);
			nPlayer.getPlayer().sendMessage("§7[nfoods] " + nAmount.nutrient.getName() + " increased by "+nAmount.amount+" to level " + Float.toString(nPlayer.getNutrientLevel(nAmount.nutrient.getIndex())));
		}
	}
	
	public float getAmount(Nutrient nutrient) {
		for (NutrientAmount nAmount: nAmounts) {
			if (nAmount.nutrient == nutrient)
				return nAmount.amount;
		}
		
		return 0;
	}
	
	class NutrientAmount {
		public Nutrient nutrient;
		public float amount;
		public NutrientAmount(Nutrient nutrient, float amount) {
			this.nutrient = nutrient;
			this.amount = amount;
		}
	}
}
