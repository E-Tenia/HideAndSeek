package fr.redsifter.hideandseek.timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
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
			}
			a = false;
		}
		HideAndSeek.time = time;
		setCompass();
		assignation--;
		if(assignation == 0) {
			for(Player p : lst) {
				p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.MAGIC + "[-----]" + ChatColor.GOLD + "NEW BONUS CHEST GENERATION" + ChatColor.DARK_AQUA + "" + ChatColor.MAGIC + "[-----]");
			}
			for(Location l : HideAndSeek.chestsave.keySet()) {
				HideAndSeek.chestsave.replace(l, true);
			}
			assignation = 200;
		}
		for (Player p : lst) {
			updateScoreBoard(p);
			if(p.getHealth() <= 5) {
				p.addPotionEffect((new PotionEffect(PotionEffectType.HEAL, 1, 10)));
				p.addPotionEffect((new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 10)));
			}
			p.setFoodLevel(20);
			if(time == HideAndSeek.initialtime-60) {
				p.sendMessage(ChatColor.DARK_RED + "[" +ChatColor.GOLD + "THE SEEKERS ARE UNLEASHED" + ChatColor.DARK_RED + "]");
			}
			 
		}
		if(time == 0 || HideAndSeek.cancel == true) {
			for (Player p : lst) {
				p.getInventory().clear();
				p.addPotionEffect((new PotionEffect(PotionEffectType.HEAL, 1, 10)));
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
				/*final Collection<Entity> entities = l.getWorld().getEntities();
				for(Entity ent : entities) {
					Location loc = l; 
					loc.setY(l.getBlockY() + 1);
      				loc.setX(l.getBlockX() + 0.5D);
      				loc.setZ(l.getBlockZ() + 0.5D);
					if(ent.getLocation() == loc && ent instanceof ArmorStand) {
						ent.remove();
					}
				}*/
				
				HideAndSeek.chestsave.replace(l, true);
			}
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
		double current = 15000;//seekers.get(0).getLocation().distanceSquared(hiders.get(0).getLocation());
		
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
			current = 15000;
		}
		
		Location closestl = null;
		for (Player p : HideAndSeek.hiders) {
			for (Location l : HideAndSeek.chestsave.keySet()) {
				if(p.getLocation().distanceSquared(l) <= current && HideAndSeek.chestsave.get(l) == true) {
					current = p.getLocation().distanceSquared(l);
					closestl = l;
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
