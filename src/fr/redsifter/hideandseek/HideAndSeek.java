package fr.redsifter.hideandseek;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
	public static ArrayList<Entity> chestnames = new ArrayList<Entity>();
	public static HashMap<Player,Boolean> notify = new HashMap<Player,Boolean>();
	public static HashMap<Location,Player> traps = new HashMap<Location,Player>();
	public static HashMap<Player,Integer> trappers = new HashMap<Player,Integer>();
	public static HashMap<Location,Boolean> chestsave = new HashMap<Location,Boolean>();
	public static HashMap<Player,Location> save = new HashMap<Player,Location>();
	public static HashMap<Player,Boolean> havemoved = new HashMap<Player,Boolean>();
	public static HashMap<Player,Location> hidermoves = new HashMap<Player,Location>();
	
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
					hiders.clear();
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
					hiders.clear();
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
        if ((seekers.contains(player) || hiders.contains(player)) && gamewarp != null && time <= (initialtime-60)){
        	if(keys.contains(blck.getLocation())) {
        		if(chestsave.get(blck.getLocation())) {
        			bonus(player);
        			chestsave.replace(blck.getLocation(),false);
        				for(Entity ent : chestnames) {
        					Location loc = new Location(ent.getWorld(),(ent.getLocation().getX() - 0.5D),ent.getLocation().getY(),(ent.getLocation().getZ() - 0.5D));
        					if(loc.equals(blck.getLocation())) {
        						ent.setCustomName(ChatColor.DARK_RED + "[CLAIMED]");
        						ent.setCustomNameVisible(true);
        					}
        				}
        			event.setCancelled(true);
        		}
        		else {
        			player.sendMessage("This bonus has already been claimed");
        			event.setCancelled(true);
        		}
        	}
        	if (trappers.containsKey(player) && trappers.get(player)  > 0 && player.getInventory().getItemInMainHand().equals(new ItemStack(Material.BLAZE_ROD))) {
    			if(blck.getType().equals(Material.CHEST)) {
    				player.sendMessage("You can't set a trap on a chest");
    				return;
    			}
        		if(!traps.containsKey(blck.getLocation())) {
    				player.sendMessage(ChatColor.GRAY + "You successfuly set your bear trap");
    				Location loc = new Location(blck.getLocation().getWorld(),blck.getLocation().getX(),blck.getLocation().getY() + 1,blck.getLocation().getZ());
        			trappers.replace(player,trappers.get(player)-1);
        			traps.put(loc,player);
    			}
    			else {
    				player.sendMessage("A trap is already set at this location");
    			}
    		}
        }
        else if(players.contains(player) && gamewarp != null && time > (initialtime-60) && blck.getType().equals(Material.CHEST)){
        player.sendMessage("Bonus chests are not available yet");
        event.setCancelled(true);
        }
    }
	
	public void bonus(Player p){
		Random random = new Random();
		int r = random.nextInt(120);
		if(p.getInventory().contains(Material.CROSSBOW) && !p.getInventory().contains(Material.SPECTRAL_ARROW)) {
			p.getInventory().remove(Material.CROSSBOW);
		}
		if(p.getInventory().contains(Material.GLASS_BOTTLE)) {
			p.getInventory().remove(Material.GLASS_BOTTLE);
		}
		if(p.getInventory().contains(Material.BLAZE_ROD) && trappers.get(p) == 0) {
			p.getInventory().remove(Material.BLAZE_ROD);
		}
		
		p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.MAGIC + "[-----]" + ChatColor.GOLD + "B" + ChatColor.RED + "O" + ChatColor.YELLOW + "N" + ChatColor.GOLD + "U" + ChatColor.RED + "S"+ ChatColor.DARK_AQUA + "" + ChatColor.MAGIC + "[-----]");
		if(r > 100) {
			p.sendMessage(ChatColor.DARK_GRAY + "BEAR TRAP");
			p.sendMessage(ChatColor.GRAY + "Set your trap with the stick by right-clicking on a block with it");
			if(!trappers.containsKey(p)) {
				ItemStack item = new ItemStack(Material.BLAZE_ROD, 1);
				p.getInventory().addItem(item);
				trappers.put(p,1);
			}
			else if(trappers.get(p) == 0) {
				ItemStack item = new ItemStack(Material.BLAZE_ROD, 1);
				p.getInventory().addItem(item);
				trappers.replace(p,1);
			}
			else {
				trappers.replace(p,trappers.get(p)+1);
			}
		}
		else if(r <= 100 && r > 91) {
			p.sendMessage(ChatColor.GOLD + "I BELIEVE I CAN FLY");
			p.addPotionEffect((new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20*10, 50)));
			p.setAllowFlight(true);
			p.setFlying(true);
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
            scheduler.scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                	p.setAllowFlight(false);
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
			ItemStack item2 = new ItemStack(Material.SPECTRAL_ARROW, 8);
			if(!p.getInventory().contains(Material.CROSSBOW)) {
				ItemStack item = new ItemStack(Material.CROSSBOW, 1);
				item.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 5);
				p.getInventory().addItem(item);
			}
			p.getInventory().addItem(item2);
		}
		else if(r <= 20) {
			p.sendMessage(ChatColor.AQUA + "SUPERSPEED");
			p.getInventory().addItem(getPotionItemStack(PotionType.INSTANT_HEAL,1,false,false,"SUPERSPEED"));
		}
	}
	
	public ItemStack getPotionItemStack(PotionType type, int level, boolean extend, boolean upgraded, String displayName){
        ItemStack potion = new ItemStack(Material.POTION, 1);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();      
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 20*25, 7), true);
        meta.setDisplayName(displayName);
        potion.setItemMeta(meta);
        return potion;
    }

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {//evenement : une entité en tape une autre
		Entity damager = event.getDamager();//entité ayant frappé
		String name1 = event.getDamager().getName();
		Entity ent = event.getEntity();//entité ayant été frappée
		String name2 = event.getEntity().getName();
		if(time < initialtime-60 && time != -1) {
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
	     if(players.contains(p) && time < initialtime-60 && players.contains(pe) && time != -1) {
	     String name1 = p.getName();
	     String name2 = en.getName();
		    if(event.getAction()==Action.LEFT_CLICK_AIR && seekers.contains(p) && hiders.contains(pe) && pe.getNoDamageTicks() == 0) {
		    	Bukkit.getPlayerExact(name2).sendMessage(ChatColor.YELLOW + "You got found by " + name1);
				Bukkit.getPlayerExact(name1).sendMessage(ChatColor.GREEN + "You found " + name2);
				for(Player p2 : players) {
					p2.sendMessage(ChatColor.RED + "" + Bukkit.getPlayerExact(name2).getName() + " has been found by " + Bukkit.getPlayerExact(name1).getName());
				}
				hiders.remove(Bukkit.getPlayerExact(en.getName()));
		    if(hiders.isEmpty()) {//si tous les hiders on été trouvés on le notifie aux joueurs, on vide les listes restantes et on arrete le chronomètre
				for (Player p2 : players) {
					p2.setGameMode(GameMode.SURVIVAL);
					p2.sendMessage("All the hiders have been found, game over !");
					general.add(p2);
				}
				cancel = true;
		    }
	     }
	     }
	}
	public static Player getNearestEntityInSight(Player player, int range) {
	    ArrayList<Entity> entities = (ArrayList<Entity>) player.getNearbyEntities(range, range, range);
	    ArrayList<Block> sightBlock = (ArrayList<Block>) player.getLineOfSight(null, range);
	    ArrayList<Location> sight = new ArrayList<Location>();
	    for (int i = 0;i<sightBlock.size();i++) {
	        sight.add(sightBlock.get(i).getLocation());
	    }
	    for (int i = 0;i<sight.size();i++) {
	        for (int k = 0;k<entities.size();k++) {
	        	
	            if (Math.abs(entities.get(k).getLocation().getX()-sight.get(i).getX())<1.3) {
	                if (Math.abs(entities.get(k).getLocation().getY()-sight.get(i).getY())<1.5) {
	                    if (Math.abs(entities.get(k).getLocation().getZ()-sight.get(i).getZ())<1.3) {
	                    	if(sightBlock.get(i).isPassable()) {
	                    		if(entities.get(k) instanceof Player) {
	                    		return Bukkit.getPlayerExact(entities.get(k).getName());
	                    		}
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
					p.sendMessage(ChatColor.DARK_PURPLE + "[H&S]"+ ChatColor.DARK_GREEN + "[HIDERS]"  + ChatColor.DARK_PURPLE+ "[" + player.getName() +  "]" + ChatColor.WHITE + msg);
				}
			}
			else if(seekers.contains(player)) {//si le joueur est seeker on envoie son message dans le groupe seeker
				event.setCancelled(true);
				for (Player p: seekers) {
					p.sendMessage(ChatColor.DARK_PURPLE+ "[H&S]" + ChatColor.DARK_RED + "[SEEKERS]" + ChatColor.DARK_PURPLE+ "[" + player.getName() +  "]" + ChatColor.WHITE + msg);
				}
			}
			else {
				for (Player p: players) {
					p.sendMessage(ChatColor.DARK_PURPLE+ "[H&S]" +  "[" + player.getName() +  "]" + ChatColor.WHITE + msg);
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
			if(player.getLocation().distanceSquared(gamewarp) > 15800 && player.getLocation().distanceSquared(gamewarp) < 18500) {
				if(save.containsKey(player)) {
					save.remove(player);
				}
				save.put(player,player.getLocation());
			}
			if(player.getLocation().distanceSquared(gamewarp) > 20000 && player.getLocation().distanceSquared(gamewarp) < 25000) {
				player.sendMessage("You are trying to pass through the game area's limits");
				player.getPlayer().teleport(save.get(player));
			}
			else if(player.getLocation().distanceSquared(gamewarp) >= 25300) {
				player.teleport(gamewarp);
			}
			
			if(seekers.contains(player)) {
				if(player.getLocation().distanceSquared(player.getCompassTarget()) < 400) {
					for(Player p : hiders) {
						Location loc = new Location(p.getWorld(),Math.round(p.getLocation().getX()),Math.round(p.getLocation().getY()),Math.round(p.getLocation().getZ()));
						if(loc.equals(player.getCompassTarget()) && notify.get(player) == false) {
							p.sendMessage(ChatColor.RED + "" + player.getName() + " is near you");
							notify.replace(player,true);
						}
					}
				}
				else {
					for(Player p : hiders) {
						Location loc = new Location(p.getWorld(),Math.round(p.getLocation().getX()),Math.round(p.getLocation().getY()),Math.round(p.getLocation().getZ()));
						if(loc.equals(player.getCompassTarget()) && notify.get(player) == true) {
							p.sendMessage(ChatColor.DARK_GREEN + "" + player.getName() + " is getting further from you");
							notify.replace(player,false);
						}
					}
				}
			}
			else if(hiders.contains(player)) {
				if(player.getLocation().distanceSquared(hidermoves.get(player)) >= 50) {
					havemoved.replace(player, true);
				}
			}
			if(hiders.contains(player) && player.isGlowing() && hidermoves.get(player).distanceSquared(player.getLocation()) >= 50) {
				player.sendMessage(ChatColor.DARK_GREEN + "You are no longer exposed");
				player.setGlowing(false);
				Location loc = player.getLocation();
				loc.setPitch(0);
				loc.setYaw(0);
				hidermoves.replace(player,loc);
			}
			if (!traps.isEmpty()) {
				Block blck = player.getLocation().getBlock();
				Location loc = new Location(blck.getLocation().getWorld(),blck.getLocation().getX(),blck.getLocation().getY(),blck.getLocation().getZ());
					for(Location l : traps.keySet()) {
						if(player.getLocation().distanceSquared(l) < 50) {
							blck.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,l, 1);
						}
					}
				if(traps.containsKey(loc)) {
					System.out.println("Trap location " + blck.getLocation());
					if(hiders.contains(traps.get(loc)) && !hiders.contains(player)) {
						player.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 20*20, 3)));
						traps.get(loc).sendMessage(ChatColor.DARK_GRAY + "" + player.getName() + " has been surprised by your trap at " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
						traps.remove(loc);
					}
					else if(seekers.contains(traps.get(loc)) && !seekers.contains(player)) {
						player.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 20*20, 3)));
						traps.get(loc).sendMessage(ChatColor.DARK_GRAY + "" + player.getName() + " has been surprised by your trap at " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
						traps.remove(loc);
					}
				}
			}
			if(player.getLocation().getBlock().getType() == Material.LAVA) {
				player.teleport(gamewarp);
				if(hiders.contains(player)) {
					player.addPotionEffect((new PotionEffect(PotionEffectType.INVISIBILITY, 20*15, 1)));
					player.setNoDamageTicks(20*15);
				}
				player.setFireTicks(20);
			}
		}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Entity ent = event.getEntity();
		Player player = null;
		Player killer = null;
		if(ent instanceof Player) {
			player = Bukkit.getPlayerExact(ent.getName());
			killer = player.getKiller();
		}
		
		if(players.contains(player)) {
			if((killer != null && (seekers.contains(killer) || hiders.contains(killer)) ) && (hiders.contains(player) || seekers.contains(player))) {
				event.setDeathMessage("");
				player.sendMessage(ChatColor.DARK_PURPLE + "YOU HAVE BEEN ELIMINATED BY " + ChatColor.RED + killer.getName());
				for(Player p : players) {
					p.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.DARK_PURPLE + " HAS BEEN ELIMINATED BY "  + ChatColor.RED + killer.getName());			
				}
			}
			else {
				return;
			}
			if(seekers.contains(player)) {
				boolean a = teamRemove(player.getName(),"seek");
				if(a) {
					System.out.println("Removing player from playerlist");
					seekers.remove(player);
					players.remove(player);
					general.add(player);
					if(seekers.isEmpty()) {
						for(Player p : players) {
							p.sendMessage(ChatColor.GOLD + "ALL SEEKERS HAVE BEEN ELIMINATED, THE HIDERS WON !");
						}
						cancel = true;
					}
				}
			}
			else if(hiders.contains(player)) {
				boolean a = teamRemove(player.getName(),"hide");
				if(a) {
					System.out.println("Removing player from playerlist");
					hiders.remove(player);
					players.remove(player);
					general.add(player);
					if(hiders.isEmpty()) {
						for(Player p : players) {
							p.sendMessage(ChatColor.GOLD + "ALL HIDERS HAVE BEEN ELIMINATED, THE SEEKERS WON !");
						}
						cancel = true;
					}
				}
			}
		}
	}
	
	private boolean teamRemove(String p,String nm) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = null;
		for (Team t : scoreboard.getTeams()) {
			if (t.getName().equals(nm)) {
				team = t;
				break;
			}
		}
		if (team != null) {
			team.removeEntry(p);
			return true;
		}
		else {
			return false;
		}
	}
	
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		System.out.println(player.getName() + " respawned");
		if(players.contains(player) && gamewarp != null) {
			player.teleport(gamewarp);
			if(hiders.contains(player)) {
				player.setNoDamageTicks(20*60);
				player.addPotionEffect((new PotionEffect(PotionEffectType.INVISIBILITY, 20*60, 1)));
			}
		}
	}
	
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(hiders.contains(event.getPlayer()) || seekers.contains(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(EntityPickupItemEvent event) {
		Player player = null;
		if(event.getEntity() instanceof Player) {
			player = Bukkit.getPlayerExact(event.getEntity().getName());
		}
			if(hiders.contains(player) || seekers.contains(player)) {
					event.setCancelled(true);
			}
		}
	
}
