package fr.redsifter.hideandseek.listener;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.entity.*;

import fr.redsifter.hideandseek.commands.Commands;
import net.md_5.bungee.api.ChatColor;

public class Listen implements Listener {
	public ArrayList<Player> players = new ArrayList<Player>();
	public ArrayList<Player> seekers = new ArrayList<Player>();
	public ArrayList<Player> hiders = new ArrayList<Player>();
	public ArrayList<Player> general = new ArrayList<Player>();
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {//evenement : une entit� en tape une autre
		Entity attacker = event.getDamager();//entit� ayant frapp�
		String name1 = event.getDamager().getName();
		Entity ent = event.getEntity();//entit� ayant �t� frapp�e
		String name2 = event.getEntity().getName();
		
		if(attacker instanceof Player && ent instanceof Player) {//si les deux entit�s sont des joeurs on v�rifie qu'elles sont dans une partie
			if(players.contains(Bukkit.getPlayerExact(name1)) && players.contains(Bukkit.getPlayerExact(name2))){//si c'est le cas on notifie au joueur frapp� qu'il a �t� trouv� et au joueur frappant qu'il l'a trouv� ainsi qu'au reste des joueurs
				Bukkit.getPlayerExact(name2).sendMessage(ChatColor.YELLOW + "You got found by " + Bukkit.getPlayerExact(name1));
				Bukkit.getPlayerExact(name1).sendMessage(ChatColor.GREEN + "You found " + Bukkit.getPlayerExact(name2));
				for(Player p : players) {
					p.sendMessage(ChatColor.RED + "" + Bukkit.getPlayerExact(name2) + "has been found by " + Bukkit.getPlayerExact(name1));
				}
				hiders.remove(ent);
				players.remove(ent);
			}
		}
		if(hiders.isEmpty()) {//si tous les hiders on �t� trouv�s on le notifie aux joueurs, on vide les listes restantes et on arrete le chronom�tre
			for (Player p : players) {
				p.sendMessage("All the hiders have been found, game over !");
			}
			seekers.clear();
			players.clear();
			//Arreter chronom�tre
		}
	}
	
	@EventHandler
	public void onCommand(PlayerCommandSendEvent event) {
		Collection<String> cmd = event.getCommands();//on r�cup�re la commande
		Iterator<String> it = cmd.iterator();
		int a = -1;
		String[] lst = new String[12];
		while(it.hasNext()) {
			if(a == 0) {
				lst[a] = (String)it.next();//on r�cup�re les arguments
			}
		a++;
		}
		String[] purged = Commands.purge(lst);//on enl�ve les "null" (ces boloss)
		if (cmd.contains("setgamelist")) {//si la commande est "setgamelist" ajoute les arguments (noms de joeurs) aux listes seekers, hiders et players
			for (int i = 0; i < purged.length;i++){
				players.add(Bukkit.getPlayerExact(purged[i]));
				if(i > purged.length/2) {
				seekers.add(Bukkit.getPlayerExact(purged[i]));
				}
				else {
				hiders.add(Bukkit.getPlayerExact(purged[i]));
				}
			}
		}
		else if (cmd.contains("startgame")) {//si la commande est "startgame" on lance le chronom�tre
			//lancer chronom�tre
		}

	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();//Joueur ayant envoy� un message dans le chat
		String msg = event.getMessage();//message ayant �t� envoy�
		if (players.contains(player)) {//test si la liste des joueurs contient ce joueur
			event.setCancelled(true);//si oui chat g�n�ral d�sactiv�
			if(hiders.contains(player)) {//si le joueur est hider on envoie son message dans le groupe hider
				for (Player p: hiders) {
					p.sendMessage(msg);
				}
			}
			else if(seekers.contains(player)) {//si le joueur est seeker on envoie son message dans le groupe seeker
				for (Player p: seekers) {
					p.sendMessage(msg);
				}
			}
		}
		else {
			event.setCancelled(false);//sinon chat g�n�ral toujours activ�
		}
	}
	
	@EventHandler
	public void onTimerRunsOut() {
		//Si timer = 0 finir la partie
	}
}
