package com.github.jacobcole2000.NutritiousFoods;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// this class represents the type and frequency at which a buff (or more likely a debuff) is applied
// to a player based on their nutrient level of a particular nutrient

public class Debuff {
	private PotionEffectType effectType;
	private int levelMinimum;
	private int levelVariance;
	private double durationMinimum;
	private double durationVariance;
	
	public Debuff(PotionEffectType effectType, ConfigurationSection config) {
		this.effectType = effectType;
		
		levelMinimum = config.getInt("level-minimum");
		levelVariance = config.getInt("level-variance");
		durationMinimum = config.getDouble("duration-minimum");
		durationVariance = config.getDouble("duration-variance");
	}
	
	public PotionEffect getEffect(double debuffStrength, long effectPeriod) {
		int duration = (int)((double)effectPeriod * (durationMinimum + durationVariance * debuffStrength));
		int level = (int)(levelMinimum + levelVariance * debuffStrength);
		return new PotionEffect(effectType, duration, level); /* try the ambient flag here too */
	}
}
