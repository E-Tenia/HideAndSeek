package fr.redsifter.hideandseek;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;

import fr.redsifter.hideandseek.commands.Commands;
import net.md_5.bungee.api.ChatColor;

public class HideAndSeek extends JavaPlugin implements Listener{
	public static int initialtime;
	public static ArrayList<Player> startplayerlist;
	public static int time;
	public static boolean cancel;
	public static boolean set;
	public static Location gamewarp;
	public static ArrayList<Player> players = new ArrayList<Player>();
	public static ArrayList<Player> seekers = new ArrayList<Player>();
	public static ArrayList<Player> hiders = new ArrayList<Player>();
	public static ArrayList<Player> general = new ArrayList<Player>();
	public static Location chest;
	public static HashMap<Location,Boolean> chestsave = new HashMap<Location,Boolean>();
	public static HashMap<Player,Location> save = new HashMap<Player,Location>();
	@Override
	public void onEnable() {
		System.out.println("Enabled HideAndSeek");
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		getCommand("hs").setExecutor(new Commands(this));
		getServer().getPluginManager().registerEvents(this, this);
		Set<String> chests = getConfig().getConfigurationSection("chests").getKeys(false);
		for(String s : chests) {
			chestsave.put(getConfig().getLocation("chests."+s), true);
		}
	}
	@Override
	public void onDisable() {
		saveConfig();
		chestsave.clear();
		System.out.println("Disabled HideAndSeek");
		
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
	
	public static void deleteTeam(String nm) {
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
						general.add(p);
						p.sendMessage("Not enough players to keep the game going, cancelling...");
					}
					players.clear();
					hiders.clear();
					seekers.clear();
					deleteTeam("hide");
					deleteTeam("seek");
					cancel = true;
				}
			}
			if(hiders.contains(player)) {
				hiders.remove(player);
				if(hiders.isEmpty()) {
					for(Player p : players) {
						general.add(p);
						p.sendMessage("Not enough players to keep the game going, cancelling...");
					}
					players.clear();
					hiders.clear();
					seekers.clear();
					deleteTeam("hide");
					deleteTeam("seek");
					cancel = true;
				}
			}
		}
	}
	
	@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
		Player player = event.getPlayer();
		Block blck = event.getClickedBlock();
		Set<Location> keys = chestsave.keySet();
		if(!(event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
			return;
		}
        if (players.contains(player) && gamewarp != null && time <= (initialtime-60)){
        	System.out.println(keys + " " + blck.getLocation());
        		if(keys.contains(blck.getLocation())) {
        			System.out.println("Test sucessful");
        			if(chestsave.get(blck.getLocation())) {
        				bonus(player);
        				chestsave.replace(blck.getLocation(),false);
        			}
        			else {
        				player.sendMessage("This bonus has already been claimed");
        			}
        		}
        	}
    }
	
	public void bonus(Player p){
		Random random = new Random();
		int r = random.nextInt(100);
		if(p.getInventory().contains(Material.BOW) && !p.getInventory().contains(Material.SPECTRAL_ARROW)) {
			p.getInventory().remove(Material.BOW);
		}
		p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.MAGIC + "[-----]" + ChatColor.GOLD + "B" + ChatColor.RED + "O" + ChatColor.YELLOW + "N" + ChatColor.GOLD + "U" + ChatColor.RED + "S"+ ChatColor.DARK_AQUA + "" + ChatColor.MAGIC + "[-----]");
		if(r > 91 ) {
			p.sendMessage(ChatColor.GOLD + "I BELIEVE I CAN FLY");
			ItemStack item = new ItemStack(Material.GOLDEN_BOOTS, 1);
			item.addEnchantment(Enchantment.PROTECTION_FALL, 200);
			p.getInventory().setBoots(item);
			p.setFlying(true);
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                	p.setFlying(false);
                }
            }, 100);
		}
		else if(r <= 91 && r > 60) {
			p.sendMessage(ChatColor.DARK_PURPLE + "ENDERPEARL");
			ItemStack item = new ItemStack(Material.ENDER_PEARL, 1);
			p.getInventory().addItem(item);
		}
		else if(r <= 60 && r > 20) {
			p.sendMessage(ChatColor.DARK_RED + "SNIPING ASSETS");
			ItemStack item = new ItemStack(Material.BOW, 1);
			ItemStack item2 = new ItemStack(Material.SPECTRAL_ARROW, 8);
			p.getInventory().addItem(item);
			p.getInventory().addItem(item2);
		}
		else if(r <= 20) {
			p.sendMessage(ChatColor.AQUA + "SUPERSPEED");
			p.addPotionEffect((new PotionEffect(PotionEffectType.SPEED, 20*10, 7)));
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
			save.clear();
			deleteTeam("seek");
			deleteTeam("hide");
			HideAndSeek.gamewarp = null;
			}
		}
	}
	
	@EventHandler
	public void onClickTarget(PlayerInteractEvent event) {
		 Player p=event.getPlayer();
	     Entity en=getNearestEntityInSight(p,20);
	     Player pe=null;
	     if(en instanceof Player) {
	    	 pe=Bukkit.getPlayerExact(en.getName());
	     }
	     else {
	    	 return;
	     }
	     if(pe == null) {
	    	 return;
	     }
	     if(players.contains(p) && time < initialtime-60 && players.contains(pe)) {
	     String name1 = p.getName();
	     String name2 = en.getName();
		    if(event.getAction()==Action.LEFT_CLICK_AIR && seekers.contains(p) && hiders.contains(pe)) {
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
				save.clear();
				deleteTeam("seek");
				deleteTeam("hide");
				HideAndSeek.gamewarp = null;
		    }
	     }
	}
	public static Entity getNearestEntityInSight(Player player, int range) {
	    ArrayList<Entity> entities = (ArrayList<Entity>) player.getNearbyEntities(range, range, range);
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
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();//Joueur ayant envoyé un message dans le chat
		String msg = event.getMessage();//message ayant été envoyé
		if (players.contains(player)) {//test si la liste des joueurs contient ce joueur
			event.setCancelled(true);//si oui chat général désactivé
			if(hiders.contains(player)) {//si le joueur est hider on envoie son message dans le groupe hider
				for (Player p: hiders) {
					p.sendMessage(ChatColor.DARK_PURPLE + "<hs>"+ ChatColor.DARK_GREEN + "[HIDERS]"  + ChatColor.DARK_PURPLE+ "[" + player.getName() +  "]" + ChatColor.WHITE + msg);
				}
			}
			else if(seekers.contains(player)) {//si le joueur est seeker on envoie son message dans le groupe seeker
				event.setCancelled(true);
				for (Player p: seekers) {
					p.sendMessage(ChatColor.DARK_PURPLE+ "<hs>" + ChatColor.DARK_RED + "[SEEKERS]" + ChatColor.DARK_PURPLE+ "[" + player.getName() +  "]" + ChatColor.WHITE + msg);
				}
			}
			else {
				for (Player p: players) {
					p.sendMessage(ChatColor.DARK_PURPLE+ "<hs>" +  "[" + player.getName() +  "]" + ChatColor.WHITE + msg);
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
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if(player != null && gamewarp != null) {
		if(players.contains(player)) {
			if(player.getLocation().distanceSquared(gamewarp) > 13800 && player.getLocation().distanceSquared(gamewarp) < 14500) {
				if(save.containsKey(player)) {
					save.remove(player);
				}
				save.put(player,player.getLocation());
			}
			if(player.getLocation().distanceSquared(gamewarp) > 15000 && player.getLocation().distanceSquared(gamewarp) < 18000) {
				player.sendMessage("You are trying to pass through the game area's limits");
				player.getPlayer().teleport(save.get(player));
			}
			else if(player.getLocation().distanceSquared(gamewarp) >= 20300) {
				player.teleport(gamewarp);
			}
		}
		}
	}
	
}
