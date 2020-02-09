package fr.redsifter.hideandseek.timer;

import org.bukkit.scheduler.BukkitRunnable;

public class Timer extends BukkitRunnable {

	//test webhook
	
	private int timer;
	@Override
	public void run() {
		if(timer == 0) {
			cancel();
		}

	}

}
