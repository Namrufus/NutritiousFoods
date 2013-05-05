package com.github.jacobcole2000.NutritiousFoods;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Nutrient {
	private String name;
	private List<Debuff> debuffs;
	
	public static Logger logger = Logger.getLogger("RealisticBiomes");
	
	public Nutrient(String name, ConfigurationSection config) {
		this.name = name;
		ConfigurationSection effectsConfig = config.getConfigurationSection("effects");
		
		debuffs = new LinkedList<Debuff>();
		
		for (String potionEffectName : effectsConfig.getKeys(false)) {
			PotionEffectType effect = PotionEffectType.getByName(potionEffectName);
			if (effect == null) {
				logger.warning("Configuration: Unknown potion effect: "+potionEffectName);
				continue;
			}
			
			debuffs.add(new Debuff(effect, effectsConfig.getConfigurationSection(potionEffectName)));
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void applyDebuffs(Player player, double debuffStrength) {
		for (Debuff debuff : debuffs) {
			PotionEffect effect = debuff.getEffect(debuffStrength, Config.effectTickPeriod);
			effect.apply(player);
		}
	}
}