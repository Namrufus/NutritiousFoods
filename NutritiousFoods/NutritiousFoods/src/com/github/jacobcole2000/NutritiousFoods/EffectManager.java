package com.github.jacobcole2000.NutritiousFoods;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class EffectManager {
	private NutritiousFoods plugin;
	private BukkitTask effectTask;
	
	public EffectManager(NutritiousFoods plugin) {
		this.plugin = plugin;
		
		//register the batchTask
		effectTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
		    @Override  
		    public void run() {
				onEffectTick();
		    }
		}, Config.effectTickPeriod, Config.effectTickPeriod);
	}
	
	public void stop() {
		effectTask.cancel();
	}
	
	private void onEffectTick() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			
			PlayerData playerData = plugin.players.get(player.getName());
			
			playerData.applyDebuffs(player);
		}
	}
}
