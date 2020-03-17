package fr.redsifter.hideandseek.timer;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import fr.redsifter.hideandseek.HideAndSeek;

public class Timer extends BukkitRunnable {
	private ScoreboardManager manager = Bukkit.getScoreboardManager();
	final Scoreboard board = manager.getMainScoreboard();
	final Objective timer = board.registerNewObjective("TIMER", "", "");
	public int time;
	public int move = 100;
	public int assignation = 200;
	public ArrayList<Player> lst;
	public boolean a = true;
	
	@Override
	public void run() {
		if(a) {
			for (Player p : lst) {
				p.getInventory().clear();
				ItemStack compass = new ItemStack(Material.COMPASS, 1);
				p.getInventory().addItem(compass);
				setScoreBoard(p);
				p.addPotionEffect((new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 3)));
				if(HideAndSeek.seekers.contains(p)) {
					HideAndSeek.notify.put(p,false);
				}
				if(HideAndSeek.hiders.contains(p)) {
					Location loc = p.getLocation();
					loc.setPitch(0);
					loc.setYaw(0);
					HideAndSeek.havemoved.put(p,false);
					HideAndSeek.hidermoves.put(p,loc);
				}
			}
			a = false;
		}
		HideAndSeek.time = time;
		setCompass();
		assignation--;
		move--;
		if(assignation == 0) {
			for(Player p : lst) {
				p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.MAGIC + "[-----]" + ChatColor.GOLD + "NEW BONUS CHEST GENERATION" + ChatColor.DARK_AQUA + "" + ChatColor.MAGIC + "[-----]");
			}
			for(Location l : HideAndSeek.chestsave.keySet()) {
				for(Entity ent : HideAndSeek.chestnames) {
					ent.setCustomName(ChatColor.GOLD + "LUCKY CHEST");
					ent.setCustomNameVisible(true);
				}
				HideAndSeek.chestsave.replace(l, true);
			}
			assignation = 200;
		}
		
		if(move == 0) {
			for(Player p : lst) {
				if(HideAndSeek.hiders.contains(p)) {
					Location loc = p.getLocation();
					loc.setYaw(0);
					loc.setPitch(0);
					if(HideAndSeek.hidermoves.get(p).distanceSquared(loc) < 50 && HideAndSeek.havemoved.get(p) == false) {
						p.sendMessage(ChatColor.RED + "You are exposed, move !");
						p.setGlowing(true);
					}
					else {
						HideAndSeek.hidermoves.replace(p,loc);
						HideAndSeek.havemoved.replace(p,false);
					}
				}
			}
			move = 100;
		}
		else if(move == 20) {
			for(Player p : lst) {
				if(HideAndSeek.hiders.contains(p)) {
					Location loc = p.getLocation();
					loc.setYaw(0);
					loc.setPitch(0);
					if(HideAndSeek.hidermoves.get(p).distanceSquared(loc) < 50 && HideAndSeek.havemoved.get(p) == false) {
						p.sendMessage(ChatColor.DARK_GRAY + "If you don't move further than 7 blocks within 20 seconds you will be exposed !");
					}
				}
			}
		}
		
		for (Player p : lst) {
			updateScoreBoard(p);
			if(p.getHealth() <= 5) {
				p.addPotionEffect((new PotionEffect(PotionEffectType.HEAL, 1, 10)));
			}
			p.setFoodLevel(20);
			if(time == HideAndSeek.initialtime-60) {
				p.sendMessage(ChatColor.DARK_RED + "[" +ChatColor.GOLD + "THE SEEKERS ARE UNLEASHED" + ChatColor.DARK_RED + "]");
			}
			 
		}
		if(time == 0 || HideAndSeek.cancel == true) {
			for (Player p : HideAndSeek.players) {
				p.setGlowing(false);
				p.getInventory().clear();
				p.addPotionEffect((new PotionEffect(PotionEffectType.HEAL, 1, 10)));
				p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
				p.setFoodLevel(20);
			}
			 if(!HideAndSeek.hiders.isEmpty()){
				 for(Player p : HideAndSeek.players) {
						p.setGameMode(GameMode.SURVIVAL);
						p.sendMessage("The hiders won !");
						HideAndSeek.general.add(p);
					}
			 }
			for(Location l : HideAndSeek.chestsave.keySet()) {
				for(Entity ent : HideAndSeek.chestnames) {
					ent.setInvulnerable(false);
					ent.remove();
				}
				
				HideAndSeek.chestsave.replace(l, true);
			}
			HideAndSeek.notify.clear();
			HideAndSeek.trappers.clear();
			HideAndSeek.traps.clear();
			HideAndSeek.chestnames.clear();
			HideAndSeek.havemoved.clear();
			HideAndSeek.hidermoves.clear();
			HideAndSeek.hiders.clear();
			HideAndSeek.seekers.clear();
			HideAndSeek.players.clear();
			HideAndSeek.save.clear();
			HideAndSeek.deleteTeam("seek");
			HideAndSeek.deleteTeam("hide");
			HideAndSeek.gamewarp = null;
			delScoreBoard();
			cancel();
		}
		time--;
	}
	public void setScoreBoard(Player p) {      
        timer.setDisplaySlot(DisplaySlot.SIDEBAR);
        timer.setDisplayName(ChatColor.DARK_PURPLE + "H&S");
        Score score = timer.getScore(ChatColor.DARK_GREEN + "TIMER");
        score.setScore(time);
        p.setScoreboard(board);
	}
	public void updateScoreBoard(Player p) {
		Score score = timer.getScore(ChatColor.DARK_GREEN + "TIMER");
		score.setScore(time);
		p.setScoreboard(board);
	}
	
	public void delScoreBoard() { 
        timer.unregister();
        board.clearSlot(DisplaySlot.SIDEBAR);
	}
	
	public void setCompass() {
		Player closest = null;//hiders.get(0);
		double current = 25000;//seekers.get(0).getLocation().distanceSquared(hiders.get(0).getLocation());
		
		//assignation cibles boussoles
		for (Player p : HideAndSeek.seekers) {
			for (Player p2 : HideAndSeek.hiders) {
				if(p.getLocation().distanceSquared(p2.getLocation()) <= current) {
					current = p.getLocation().distanceSquared(p2.getLocation());
					closest = p2;
				}
				
			}
			
			if(closest != null) {
				Location loc = new Location(closest.getLocation().getWorld(),closest.getLocation().getX(),closest.getLocation().getY(),closest.getLocation().getZ());
				p.setCompassTarget(loc);
			}
			current = 25000;
		}
		
		Location closestl = null;
		for (Player p : HideAndSeek.hiders) {
			for (Player p2 : HideAndSeek.seekers) {
				if(p.getLocation().distanceSquared(p2.getLocation()) <= current) {
					current = p.getLocation().distanceSquared(p2.getLocation());
					closestl = p2.getLocation();
				}
				
			}
			
			if(closestl != null) {
				Location loc = new Location(closestl.getWorld(),closestl.getX(),closestl.getY(),closestl.getZ());
				p.setCompassTarget(loc);
			}
			current = 15000;
		}
	}
	
	/*public void randomChest() {
		Random random = new Random();
		int r = random.nextInt(HideAndSeek.chestsave.size());
		while(r < (HideAndSeek.chestsave.size()/2)+2) {
			r = random.nextInt(HideAndSeek.chestsave.size());
		}
		
		int r2 = random.nextInt(HideAndSeek.chestsave.size());
		ArrayList<Location> tab = new ArrayList<Location>();
		ArrayList<Integer> blcklst = new ArrayList<Integer>();
		Set<Location> set = HideAndSeek.chestsave.keySet();
		Object[] chests = set.toArray();
		for(int i = 0;i < r;i++) {
			while(!tab.contains((Location)chests[r2])) {
				if(!blcklst.contains(r2)) {
					tab.add((Location)chests[r2]);
					blcklst.add(r2);
				}
			r2 = random.nextInt(HideAndSeek.chestsave.size());
			}
		}
		
		for(Location l : tab) {
			HideAndSeek.chestsave.replace(l,true);	
		}
	}*/
}
