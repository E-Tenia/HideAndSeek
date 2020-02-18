package fr.redsifter.hideandseek;

import java.util.ArrayList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;

import fr.redsifter.hideandseek.commands.Commands;
import fr.redsifter.hideandseek.timer.*;
import net.md_5.bungee.api.ChatColor;

public class HideAndSeek extends JavaPlugin implements Listener{
	public int initialtime;
	public static String startwarpname;
	public static String startcheck;
	public static Player startplayer;
	public static ArrayList<Player> startplayerlist;
	public static int time;
	public static boolean cancel;
	public static boolean run;
	public static Location gamewarp;
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
			startwarpname = warp;
			startplayer = event.getPlayer();
			startplayerlist = players;
			for (Player p : seekers) {
				p.setGameMode(GameMode.ADVENTURE);
				p.addPotionEffect((new PotionEffect(PotionEffectType.BLINDNESS, 20*60, 1)));
				p.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 20*60, 100)));
				p.addPotionEffect((new PotionEffect(PotionEffectType.JUMP, 20*60, 200)));
				p.sendMessage(ChatColor.GOLD + "The hiders are hiding, you'll be able to move after 1 min");
			}
			for (Player p : hiders) {
				p.setGameMode(GameMode.ADVENTURE);
				p.sendMessage(ChatColor.GOLD + "You have 1 min to get as far as possible and hide");
				}
			startTimer(arg1,players);
			}
	}

	public void startTimer(int arg1,ArrayList<Player> lst) {
		Timer timer = new Timer();
		cancel = false;
		initialtime = arg1;
		timer.time = arg1;
		timer.lst = lst;
		timer.runTaskTimer(this, 0, 20);
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
		if(!player.isOp()) {
			player.setGameMode(GameMode.SURVIVAL);
		}
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
						p.sendMessage("Not enough players to keep the game going, cancelling...");
					}
					cancel = true;
				}
			}
			if(hiders.contains(player)) {
				hiders.remove(player);
				if(hiders.isEmpty()) {
					for(Player p : players) {
						p.sendMessage("Not enough players to keep the game going, cancelling...");
					}
					cancel = true;
				}
			}
		}
	}
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {//evenement : une entité en tape une autre
		Entity damager = event.getDamager();//entité ayant frappé
		String name1 = event.getDamager().getName();
		Entity ent = event.getEntity();//entité ayant été frappée
		String name2 = event.getEntity().getName();
		if(time < initialtime-60) {
		if(damager instanceof Player && ent instanceof Player) {//si les deux entités sont des joeurs on vérifie qu'elles sont dans une partie
			if(seekers.contains(Bukkit.getPlayerExact(name1)) && hiders.contains(Bukkit.getPlayerExact(name2))){//si c'est le cas on notifie au joueur frappé qu'il a été trouvé et au joueur frappant qu'il l'a trouvé ainsi qu'au reste des joueurs
				Bukkit.getPlayerExact(name2).sendMessage(ChatColor.YELLOW + "You got found by " + name1);
				Bukkit.getPlayerExact(name1).sendMessage(ChatColor.GREEN + "You found " + name2);
				for(Player p : players) {
					p.sendMessage(ChatColor.RED + "" + Bukkit.getPlayerExact(name2).getName() + " has been found by " + Bukkit.getPlayerExact(name1).getName());
				}
				hiders.remove(Bukkit.getPlayerExact(ent.getName()));
			}
		}
		if(hiders.isEmpty()) {//si tous les hiders on été trouvés on le notifie aux joueurs, on vide les listes restantes et on arrete le chronomètre
			for (Player p : players) {
				p.setGameMode(GameMode.SURVIVAL);
				p.sendMessage("All the hiders have been found, game over !");
				general.add(p);
			}
			cancel = true;
			seekers.clear();
			players.clear();
			deleteTeam("seek");
			deleteTeam("hide");
			}
		}
	}
	
	@EventHandler
	public void onClickTarget(PlayerInteractEvent event) {
		 Player p=event.getPlayer();
	     Entity en=getNearestEntityInSight(p,500);
	     String name1 = p.getName();
	     String name2 = en.getName();
		    if(event.getAction()==Action.LEFT_CLICK_AIR && en instanceof Player && hiders.contains(en) && seekers.contains(p)) {
		    	Bukkit.getPlayerExact(name2).sendMessage(ChatColor.YELLOW + "You got found by " + name1);
				Bukkit.getPlayerExact(name1).sendMessage(ChatColor.GREEN + "You found " + name2);
				for(Player p2 : players) {
					p2.sendMessage(ChatColor.RED + "" + Bukkit.getPlayerExact(name2).getName() + " has been found by " + Bukkit.getPlayerExact(name1).getName());
				}
				hiders.remove(Bukkit.getPlayerExact(en.getName()));
		    }
		    if(hiders.isEmpty()) {//si tous les hiders on été trouvés on le notifie aux joueurs, on vide les listes restantes et on arrete le chronomètre
				for (Player p2 : players) {
					p2.setGameMode(GameMode.SURVIVAL);
					p2.sendMessage("All the hiders have been found, game over !");
					general.add(p2);
				}
				cancel = true;
				seekers.clear();
				players.clear();
				deleteTeam("seek");
				deleteTeam("hide");
		    }
	}
	public static Entity getNearestEntityInSight(Player player, int range) {
	    ArrayList<Entity> entities = (ArrayList<Entity>) player.getNearbyEntities(range, range, range);
	    System.out.println(entities);
	    ArrayList<Block> sightBlock = (ArrayList<Block>) player.getLineOfSight(null, range);
	    ArrayList<Location> sight = new ArrayList<Location>();
	    for (int i = 0;i<sightBlock.size();i++)
	        sight.add(sightBlock.get(i).getLocation());
	    for (int i = 0;i<sight.size();i++) {
	        for (int k = 0;k<entities.size();k++) {
	        	
	            if (Math.abs(entities.get(k).getLocation().getX()-sight.get(i).getX())<1.3) {
	                if (Math.abs(entities.get(k).getLocation().getY()-sight.get(i).getY())<1.5) {
	                    if (Math.abs(entities.get(k).getLocation().getZ()-sight.get(i).getZ())<1.3) {
	                    	if(sightBlock.get(i).isPassable()) {
	                    		return entities.get(k);
	                    	}
	                    }
	                }
	            }
	            
	        }
	    }
	    return null;
	}
	
	@EventHandler
	public void onSet(PlayerCommandPreprocessEvent event) {
		String msg = event.getMessage();//on récupère la commande
		ArrayList<String> playerlist = cut(msg);//on récupère et trie les arguments (noms des joueurs)
		String check = "";
		int splitter = 0;
		int split = 1;
		if (playerlist.contains("setgamelist")) {
			if(playerlist.size() >= 2) {
				check = playerlist.get(0);
				playerlist.remove(0);
			}
			else {
				return;
			}
			if (check.equalsIgnoreCase("setgamelist")) {//si la commande est "setgamelist" ajoute les arguments (noms de joeurs) aux listes seekers, hiders et players
				for (String p : playerlist) {
					general.remove(Bukkit.getPlayerExact(p));
				}
				if(playerlist.size() == 2) {
					split = 0;
				}
				if(playerlist.size()%2 == 0) {
					splitter = 0;
				}
				else {
					splitter = 1;
				}
				for (int i = 0; i < playerlist.size();i++){
					players.add(Bukkit.getPlayerExact(playerlist.get(i)));
					System.out.println("size1 : "+(playerlist.size()));
					System.out.println("list1 : "+ playerlist);
					System.out.println("split1 : "+(playerlist.size()/2+split));
					if(i < ((playerlist.size()/2)+splitter)-split) {
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
				p.sendMessage(ChatColor.GOLD + "[" + p.getWorld().getName() + "]" + ChatColor.AQUA + "[" + player.getName() + "]"+ ChatColor.WHITE + msg);
			}
		}
	}
	
	@EventHandler
	public void onTimeCheck(EntityRegainHealthEvent event) {
		Entity ent = event.getEntity();
		Player player = Bukkit.getPlayerExact(ent.getName());
		Player closest = hiders.get(0);
		double current = seekers.get(0).getLocation().distanceSquared(hiders.get(0).getLocation());
		for (Player p : seekers) {
			for (Player p2 : hiders) {
				if(p.getLocation().distanceSquared(p2.getLocation()) <= current) {
					current = p.getLocation().distanceSquared(p2.getLocation());
					closest = p2;
				}
			}
		}
		player.setCompassTarget(closest.getLocation());
		if(cancel == true) {
			gamewarp = null;
		}
		if(time == 0 && player != null && players.contains(player)) {
			gamewarp = null;
			for(Player p : players) {
				p.setGameMode(GameMode.SURVIVAL);
				p.sendMessage("The hiders won !");
				general.add(p);
			}
			hiders.clear();
			seekers.clear();
			players.clear();
			deleteTeam("seek");
			deleteTeam("hide");
			cancel = true;
			
		}
		if(player != null && players.contains(player) && gamewarp != null) {
			if(player.getLocation().distanceSquared(gamewarp) >= 9500 && player.getLocation().distanceSquared(gamewarp) < 15700) {
				player.sendMessage(ChatColor.RED + "If you go further from this point you will be teleported back to the game start point");
				player.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 100, 5)));
			}
			else if(player.getLocation().distanceSquared(gamewarp) >= 15700) {
				player.sendMessage(ChatColor.RED + "You are not authorized to leave the area");
				player.teleport(gamewarp);
			}
		}
	}
	
}
