package fr.redsifter.hideandseek;

import java.util.ArrayList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.redsifter.hideandseek.commands.Commands;
import net.md_5.bungee.api.ChatColor;

public class HideAndSeek extends JavaPlugin implements Listener{
	public static String startwarpname;
	public static int startarg;
	public static String startcheck;
	public static Player startplayer;
	public static ArrayList<Player> startplayerlist;
	public static int time;
	public static boolean cancel;
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
		ArrayList<String> newlist = cut(msg);
		System.out.println(newlist);
		if(newlist.contains("startgame")) {
			if(newlist.size() >= 3) {
				check = newlist.get(0);
				newlist.remove(0);
				arg1 = Integer.parseInt(newlist.get(0).trim());
				warp = newlist.get(1);
			}
			else {
				check = msg;
			}
			startcheck = check;
			startarg = arg1;
			startwarpname = warp;
			startplayer = event.getPlayer();
			startplayerlist = players;
		}
	}
	
	public ArrayList<String> cut(String str) {
		str = str + ' ';
		ArrayList<String> temp = new ArrayList<String>();
		String tmp = "";
		boolean a = false;
		for(int i = 0;i < str.length();i++) {
			if (str.charAt(i) == ' ' && a == true) {
				if(tmp != null) {
					temp.add(tmp.trim());
				}
				tmp = "";
			}
			else if (str.charAt(i) == ' ' && a == false){
				tmp = "";
				a = true;
			}
			tmp += str.charAt(i);
		}
		if (a == false) {
			temp.add(str.trim()); 
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
			if(seekers.contains(player)) {
				seekers.remove(player);
				if(seekers.isEmpty()) {
					for(Player p : players) {
						p.sendMessage("Not enough players to keep the game going, stopping the timer...");
					}
					cancel = true;
				}
			}
			if(hiders.contains(player)) {
				hiders.remove(player);
				if(hiders.isEmpty()) {
					for(Player p : players) {
						p.sendMessage("Not enough players to keep the game going, stopping the timer...");
					}
					cancel = true;
				}
			}
		}
	}
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {//evenement : une entité en tape une autre
		Entity attacker = event.getDamager();//entité ayant frappé
		String name1 = event.getDamager().getName();
		Entity ent = event.getEntity();//entité ayant été frappée
		String name2 = event.getEntity().getName();
		if(attacker instanceof Player && ent instanceof Player) {//si les deux entités sont des joeurs on vérifie qu'elles sont dans une partie
			if(seekers.contains(Bukkit.getPlayerExact(name2)) && hiders.contains(Bukkit.getPlayerExact(name1))){//si c'est le cas on notifie au joueur frappé qu'il a été trouvé et au joueur frappant qu'il l'a trouvé ainsi qu'au reste des joueurs
				Bukkit.getPlayerExact(name2).sendMessage(ChatColor.YELLOW + "You got found by " + name1);
				Bukkit.getPlayerExact(name1).sendMessage(ChatColor.GREEN + "You found " + name2);
				for(Player p : players) {
					p.sendMessage(ChatColor.RED + "" + Bukkit.getPlayerExact(name2).getName() + " has been found by " + Bukkit.getPlayerExact(name1).getName());
				}
				hiders.remove(attacker);
			}
			}
		System.out.println(hiders);
		System.out.println(hiders.isEmpty());
			if(hiders.isEmpty()) {//si tous les hiders on été trouvés on le notifie aux joueurs, on vide les listes restantes et on arrete le chronomètre
				for (Player p : players) {
					p.sendMessage("All the hiders have been found, game over !");
					general.add(p);
				}
			seekers.clear();
			players.clear();
			deleteTeam("seek");
			deleteTeam("hide");
			cancel = true;
		}
	}
	
	@EventHandler
	public void onSet(PlayerCommandPreprocessEvent event) {
		String msg = event.getMessage();//on récupère la commande
		ArrayList<String> playerlist = cut(msg);//on récupère et trie les arguments (noms des joueurs)
		String check = "";
		if (playerlist.contains("setgamelist")) {
			if(playerlist.size() >= 1) {
				check = playerlist.get(0);
				playerlist.remove(0);
			}
			else {
				check = msg;
			}
			System.out.println(check);
			System.out.println(playerlist);
			System.out.println(playerlist.size());
			if (check.equalsIgnoreCase("setgamelist")) {//si la commande est "setgamelist" ajoute les arguments (noms de joeurs) aux listes seekers, hiders et players
				for (int i = 0; i < playerlist.size();i++){
					players.add(Bukkit.getPlayerExact(playerlist.get(i)));
					if(i >= playerlist.size()/2) {
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
	public void onTimerRunsOut(EntityRegainHealthEvent event) {
		Entity ent = event.getEntity();
		Player player = Bukkit.getPlayerExact(ent.getName());
		if(time == 0 && player != null && players.contains(player)) {
			for(Player p : players) {
				p.sendMessage("The hiders won !");
			}
			hiders.clear();
			seekers.clear();
			players.clear();
			deleteTeam("seek");
			deleteTeam("hide");
			cancel = true;
			
		}
	}
	
}
