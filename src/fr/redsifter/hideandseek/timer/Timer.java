package fr.redsifter.hideandseek.timer;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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
	public ArrayList<Player> lst;
	public boolean a = true;
	public boolean cancel;
	@Override
	public void run() {
		if(a) {
			for (Player p : lst) {
				setScoreBoard(p);
			}
		}
		a = false;
		HideAndSeek.time = time;
		cancel = HideAndSeek.cancel; 
		for (Player p : lst) {
			updateScoreBoard(p);
			p.addPotionEffect((new PotionEffect(PotionEffectType.HEAL, 1, 10)));
			p.setFoodLevel(20);
			 
		}
		if(time == 0) {
			for (Player p : lst) {
				p.addPotionEffect((new PotionEffect(PotionEffectType.HEAL, 1, 10)));
				p.setFoodLevel(20);
				delScoreBoard(p);
				 
			}
			cancel();
		}
		if(cancel) {
			for (Player p : lst) {
				p.addPotionEffect((new PotionEffect(PotionEffectType.HEAL, 1, 10)));
				p.setFoodLevel(20);
				delScoreBoard(p);
			}
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
	
	public void delScoreBoard(Player p) { 
        timer.unregister();
        board.clearSlot(DisplaySlot.SIDEBAR);
        p.setScoreboard(board);
	}
}
