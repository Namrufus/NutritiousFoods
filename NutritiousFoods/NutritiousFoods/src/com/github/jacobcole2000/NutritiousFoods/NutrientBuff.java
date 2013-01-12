package com.github.jacobcole2000.NutritiousFoods;

import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// this class represents the type and frequency at which a buff (or more likely a debuff) is applied
// to a player based on their nutrient level of a particular nutrient

public class NutrientBuff {
	private Nutrient nutrient;
	private PotionEffectType effectType;
	private boolean moreThan; // if this is true, the effect will be present if the nutrient level is above the cutoff
	private float cutoff; // the cutoff level of the debuff
	// the chance that this effect will be applied each effectTick at 0 (or max) nutrient level
	private float chanceMax;
	// the intensity at which the buff is applied at 0 (or max) nutrient level
	// in effect levels
	private int intensityMax;
	// the duration at which the buff is applied at 0 (or max) nutrient level
	// in seconds
	private float durationMax;
	
	NutrientBuff(PotionEffectType effectType, boolean moreThan, float cutoff, float chanceMax, int intensityMax, float durationMax, Nutrient nutrient) {
		this.nutrient = nutrient;
		this.effectType = effectType;
		this.moreThan = moreThan;
		this.cutoff = cutoff;
		this.chanceMax = chanceMax;
		this.intensityMax = intensityMax;
		this.durationMax = durationMax;
	}
	
	// get an effect to apply to a player with the specified nutrient level. this is called every effectTick
	// if there is to be no effect this EffectTick this will return null
	public PotionEffect getEffect(float nutrientLevel) {
		float duration; // duration in seconds
		int level; // potion level
		
		// fractional amount that the nutrient level will cause an effect
		// (1.0 @ 0 nutrient level and 0.0 @ cutoff)
		// (1.0 @ maxNutrientLevel and 0.0 @ cutoff) when the moreThan flag is set
		float magnitude;
		if (moreThan)
			magnitude = (float)(nutrientLevel-nutrient.getMaxLevel())/(float)(cutoff-nutrient.getMaxLevel());
		else
			magnitude = (float)(cutoff-nutrientLevel)/(float)cutoff;
		
		// if the nutrient level is outside the cutoff, return with no effect
		if (magnitude < 0.0)
			return null;
		
		// frequency depends linearly on magnitude
		if (Math.random() > magnitude*chanceMax)
			return null;
		
		// intensity also is linear with magnitude
		level = 1 + (int)(((float)(intensityMax-1))*magnitude);
		
		// duration is also linear with magnitude
		duration = (float)durationMax*magnitude;
		
		// convert duration to millseconds and return potion effect
		return new PotionEffect(effectType, (int)duration, level);
	}
	
	public String toString() {
		String str = "[Nutrient Buff: ";
		str+= "Effect Type = " + effectType.toString() + ", ";
		str+= "moreThan = " + Boolean.toString(moreThan) + ", ";
		str+= "chancemax = " + Float.toString(chanceMax) + ", ";
		str+= "intensitymax = " + Integer.toString(intensityMax) + ", ";
		str+= "durationmax = " + Integer.toString((int) durationMax) + "]";
		return str;
	}

}
