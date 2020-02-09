package fr.redsifter.hideandseek.timer;

import org.bukkit.scheduler.BukkitRunnable;

public class Timer extends BukkitRunnable {
	
	//timer
	//Oui oui
	
	private int timer;
	@Override
	public void run() {
		if(timer == 0) {
			cancel();
		}

	}
	
	private void enculay() {
		System.out.println("lal");
	}

}
