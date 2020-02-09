package fr.redsifter.hideandseek;

import org.bukkit.plugin.java.JavaPlugin;
import fr.redsifter.hideandseek.commands.Commands;

public class HideAndSeek extends JavaPlugin {
	@Override
	public void onEnable() {
		System.out.println("Enabling HideAndSeek version 1.0");
		getCommand("test").setExecutor(new Commands());
		getCommand("has").setExecutor(new Commands());
	}
	@Override
	public void onDisable() {
		System.out.println("Disabling HideAndSeek");
		
	}
}