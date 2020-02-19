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
	final Objective hiders = board.registerNewObjective("HIDERS", "", "");
	final Objective seekers = board.registerNewObjective("SEEKERS", "", "");
	public int time;
	public ArrayList<Player> lst;
	public ArrayList<Player> hide;
	public ArrayList<Player> seek;
	public boolean a = true;
	@Override
	public void run() {
		if(a) {
			for (Player p : lst) {
				setScoreBoard(p);
			}
			a = false;
		}
		HideAndSeek.time = time;
		for (Player p : lst) {
			updateScoreBoard(p);
			p.addPotionEffect((new PotionEffect(PotionEffectType.HEAL, 1, 10)));
			p.setFoodLevel(20);
			 
		}
		System.out.println(HideAndSeek.cancel);
		if(time == 0 || HideAndSeek.cancel == true) {
			for (Player p : lst) {
				p.addPotionEffect((new PotionEffect(PotionEffectType.HEAL, 1, 10)));
				p.setFoodLevel(20);
				 
			}
			delScoreBoard();
			cancel();
		}
		time--;

	}
	public void setScoreBoard(Player p) {      
        timer.setDisplaySlot(DisplaySlot.SIDEBAR);
        timer.setDisplayName(ChatColor.DARK_PURPLE + "H&S");
        hiders.setDisplaySlot(DisplaySlot.SIDEBAR);
        hiders.setDisplayName(ChatColor.GREEN + "HIDERS");
        seekers.setDisplaySlot(DisplaySlot.SIDEBAR);
        seekers.setDisplayName(ChatColor.DARK_RED + "SEEKERS");
        Score score = timer.getScore(ChatColor.DARK_GREEN + "TIMER");
        Score score2 = hiders.getScore(ChatColor.GREEN + "HIDERS");
        Score score3 = seekers.getScore(ChatColor.DARK_RED + "SEEKERS");
        score.setScore(time);
        score2.setScore(hide.size());
        score3.setScore(seek.size()); 
        p.setScoreboard(board);
	}
	public void updateScoreBoard(Player p) {
		Score score = timer.getScore(ChatColor.DARK_GREEN + "TIMER");
		Score score2 = hiders.getScore(ChatColor.GREEN + "HIDERS");
	    Score score3 = seekers.getScore(ChatColor.DARK_RED + "SEEKERS");
		score.setScore(time);
		score2.setScore(hide.size());
	    score3.setScore(seek.size()); 
		p.setScoreboard(board);
	}
	
	public void delScoreBoard() { 
        timer.unregister();
        hiders.unregister();
        seekers.unregister();
        board.clearSlot(DisplaySlot.SIDEBAR);
	}
}
