package fr.redsifter.hideandseek.listener;

import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.entity.*;

import fr.redsifter.hideandseek.commands.Commands;

public class Listen implements Listener {
	public HashMap<Player,String> players = new HashMap<Player,String>();
	public HashMap<Player,String> general = new HashMap<Player,String>();
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		Entity attacker = event.getDamager();
		String name1 = event.getDamager().getName();
		Entity ent = event.getEntity();
		String name2 = event.getEntity().getName();
		
		if(attacker instanceof Player && ent instanceof Player) {
			if(players.containsKey(Bukkit.getPlayerExact(name1)) && players.containsKey(Bukkit.getPlayerExact(name2))){
				System.out.println("Test successful");
				Bukkit.getPlayerExact(name2).sendMessage("You got found by " + Bukkit.getPlayerExact(name1));
				players.remove(ent);
			}
		}
		if(players.isEmpty()) {
			
		}
	}
	@EventHandler
	public void onChannelJoin(PlayerRegisterChannelEvent event) {
		Player player = event.getPlayer();
		String channel = event.getChannel();
		if (channel == "hiders" || channel == "seekers") {
			players.put(player,channel);
		}
		else {
			if (players.containsKey(player)) {
			players.remove(player);
			}
		}
	}
	
	public void onCommand(PlayerCommandSendEvent event) {
		Collection<String> cmd = event.getCommands();
		Iterator<String> it = cmd.iterator();
		int a = -1;
		String[] lst = new String[12];
		if (cmd.contains("setgamelist")) {
			while(it.hasNext()) {
				if(a == 0) {
					lst[a] = (String)it.next();
				}
			a++;
			}
			String[] purged = Commands.purge(lst);
			for (int i = 0; i < purged.length;i++){
				if(i > purged.length/2) {
				players.put(Bukkit.getPlayerExact(purged[i]),"seekers");
				}
				else {
				players.put(Bukkit.getPlayerExact(purged[i]),"hiders");
				}
			}
		}

	}
}
