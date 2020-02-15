package fr.redsifter.hideandseek.timer;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import fr.redsifter.hideandseek.HideAndSeek;

public class Timer extends BukkitRunnable {
	
	public int time;
	public ArrayList<Player> lst;
	public boolean cancel;
	@Override
	public void run() {
		Bukkit.broadcastMessage("Timer :" + time);
		HideAndSeek.time = time;
		cancel = HideAndSeek.cancel; 
		if(time == 0) {
			for (Player p : lst) {
				p.addPotionEffect((new PotionEffect(PotionEffectType.HEAL, 1, 10)));
				p.setFoodLevel(20);
				 
			}
			cancel();
		}
		if(cancel) {
			cancel();
		}
		time--;

	}
}
