package com.github.jacobcole2000.NutritiousFoods;

import java.util.ArrayList;

public class NutrientFood {
	ArrayList<NutrientAmount> nAmounts;
	
	public NutrientFood() {
		nAmounts = new ArrayList<NutrientAmount>();
	}
	
	public void addNutrient(Nutrient nutrient, float amount) {
		nAmounts.add(new NutrientAmount(nutrient, amount));
	}
	
	public void addToNutrients(NutrientPlayer nPlayer) {
		for (NutrientAmount nAmount:nAmounts) {
			nPlayer.addNutrientLevel(nAmount.nutrient, nAmount.amount);
		}
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
