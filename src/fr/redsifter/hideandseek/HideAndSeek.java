package fr.redsifter.hideandseek;

import java.util.ArrayList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.redsifter.hideandseek.commands.Commands;
import fr.redsifter.hideandseek.timer.Timer;
import net.md_5.bungee.api.ChatColor;

public class HideAndSeek extends JavaPlugin implements Listener{
	private HideAndSeek main;
	public Timer timer = new Timer();
	public ArrayList<Player> players = new ArrayList<Player>();
	public ArrayList<Player> seekers = new ArrayList<Player>();
	public ArrayList<Player> hiders = new ArrayList<Player>();
	public ArrayList<Player> general = new ArrayList<Player>();
	@Override
	public void onEnable() {
		System.out.println("Enabling HideAndSeek");
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		getCommand("hs").setExecutor(new Commands(this));
		getServer().getPluginManager().registerEvents(this, this);
	}
	@Override
	public void onDisable() {
		saveConfig();
		System.out.println("Disabling HideAndSeek");
		
	}
	@EventHandler
	public void onStart(PlayerCommandPreprocessEvent event) {
		String msg = event.getMessage();
		String check = "";
		int arg1 = 0;
		String warp = "";
		ArrayList<String> newlist = cut2(msg);
		if(newlist.contains("/startgame")) {
			if(newlist.size() == 2) {
				newlist.remove(0);
				check = newlist.get(0);
				arg1 = Integer.parseInt(newlist.get(1).trim());
				warp = newlist.get(2);
			}
			else {
				check = msg;
			}
			Location loc = main.getConfig().getLocation("warps."+warp+".Location");
			if(loc == null) {
				event.getPlayer().sendMessage("Invalid warp name, aborting...");
				return;
			}
			if(check.equalsIgnoreCase("/startgame")){
				for(Player p : players) {
					p.teleport(loc);
				}
				timer.time = arg1;
				timer.runTaskTimer(this , 0, 20);
			}
		}
	}
	
	public ArrayList<String> cut(String str) {
		ArrayList<String> temp = new ArrayList<String>();
		String tmp = "";
		boolean a = false;
		for(int i = 0;i < str.length();i++) {
			tmp += str.charAt(i);
			if (str.charAt(i) == ' ' && a == true) {
				if(Bukkit.getPlayerExact(tmp) != null) {
					temp.add(tmp);
				}
				tmp = "";
			}
			else if (str.charAt(i) == ' ' && a == false){
				a = true;
			}
		}
		if (a == false) {
			temp.add(str); 
		}
		return temp;
		
	}
	
	public ArrayList<String> cut2(String str) {
		ArrayList<String> temp = new ArrayList<String>();
		String tmp = "";
		boolean a = false;
		for(int i = 0;i < str.length();i++) {
			tmp += str.charAt(i);
			if (str.charAt(i) == ' ' && a == true) {
				if(tmp != null) {
					temp.add(tmp);
				}
				tmp = "";
			}
			else if (str.charAt(i) == ' ' && a == false){
				a = true;
			}
		}
		if (a == false) {
			temp.add(str); 
		}
		return temp;
		
	}
	
	private void deleteTeam(String nm) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		
		for (Team t : scoreboard.getTeams()) {
			if (t.getName().equals(nm)) {
				t.unregister();
				break;
			}
		}

	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		if(!general.contains(player)) {
			general.add(player);
		}
	}
	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		Player player = event.getPlayer();
		if(general.contains(player)) {
			general.remove(player);
		}
		if(players.contains(player)) {
			players.remove(player);
		}
		if(seekers.contains(player)) {
			seekers.remove(player);
		}
		if(hiders.contains(player)) {
			hiders.remove(player);
		}
	}
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {//evenement : une entité en tape une autre
		Entity attacker = event.getDamager();//entité ayant frappé
		String name1 = event.getDamager().getName();
		Entity ent = event.getEntity();//entité ayant été frappée
		String name2 = event.getEntity().getName();
		
		if(attacker instanceof Player && ent instanceof Player) {//si les deux entités sont des joeurs on vérifie qu'elles sont dans une partie
			if(players.contains(Bukkit.getPlayerExact(name1)) && players.contains(Bukkit.getPlayerExact(name2))){//si c'est le cas on notifie au joueur frappé qu'il a été trouvé et au joueur frappant qu'il l'a trouvé ainsi qu'au reste des joueurs
				Bukkit.getPlayerExact(name2).sendMessage(ChatColor.YELLOW + "You got found by " + Bukkit.getPlayerExact(name1));
				Bukkit.getPlayerExact(name1).sendMessage(ChatColor.GREEN + "You found " + Bukkit.getPlayerExact(name2));
				for(Player p : players) {
					p.sendMessage(ChatColor.RED + "" + Bukkit.getPlayerExact(name2).getName() + "has been found by " + Bukkit.getPlayerExact(name1).getName());
				}
				hiders.remove(ent);
				players.remove(ent);
			}
			}
			if(hiders.isEmpty()) {//si tous les hiders on été trouvés on le notifie aux joueurs, on vide les listes restantes et on arrete le chronomètre
				for (Player p : players) {
					p.sendMessage("All the hiders have been found, game over !");
					general.add(p);
				}
			seekers.clear();
			players.clear();
			deleteTeam("seek");
			deleteTeam("hide");
			//Arreter chronomètre
		}
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		String msg = event.getMessage();//on récupère la commande
		ArrayList<String> playerlist = cut(msg);//on récupère et trie les arguments (noms des joueurs)
		String check = "";
		if (playerlist.contains("/setgamelist")) {
			if(playerlist.size() > 1) {
				playerlist.remove(0);
				check = playerlist.get(0);
			}
			else {
				check = msg;
			}
			if (check.equalsIgnoreCase("/setgamelist")) {//si la commande est "setgamelist" ajoute les arguments (noms de joeurs) aux listes seekers, hiders et players
				for (int i = 0; i < playerlist.size();i++){
					players.add(Bukkit.getPlayerExact(playerlist.get(i)));
					if(i > playerlist.size()/2) {
					seekers.add(Bukkit.getPlayerExact(playerlist.get(i)));
					}
					else {
					hiders.add(Bukkit.getPlayerExact(playerlist.get(i)));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();//Joueur ayant envoyé un message dans le chat
		String msg = event.getMessage();//message ayant été envoyé
		if (players.contains(player)) {//test si la liste des joueurs contient ce joueur
			event.setCancelled(true);//si oui chat général désactivé
			if(hiders.contains(player)) {//si le joueur est hider on envoie son message dans le groupe hider
				for (Player p: hiders) {
					p.sendMessage(ChatColor.DARK_PURPLE + "<hs>" + msg);
				}
			}
			else if(seekers.contains(player)) {//si le joueur est seeker on envoie son message dans le groupe seeker
				event.setCancelled(true);
				for (Player p: seekers) {
					p.sendMessage(ChatColor.DARK_PURPLE+ "<hs>" + msg);
				}
			}
		}
		else if(general.contains(player)) {
			event.setCancelled(true);//sinon chat général toujours activé
			for (Player p: general) {
				p.sendMessage(ChatColor.DARK_AQUA + "[" + p.getWorld().getName() + "]" + ChatColor.WHITE + msg);
			}
		}
	}
	
	@EventHandler
	public void onTimerRunsOut() {
		//Si timer = 0 finir la partie
	}
	
}
