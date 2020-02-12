package fr.redsifter.hideandseek.timer;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class Timer extends BukkitRunnable {
	
	public int timer = 10;
	@Override
	public void run() {
		Bukkit.broadcastMessage("Temps restant : " + timer);
		if(timer == 0) {
			cancel();
		}
		timer--;
	}
}
