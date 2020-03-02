package fr.redsifter.hideandseek.timer;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import fr.redsifter.hideandseek.HideAndSeek;

public class Wait extends BukkitRunnable {
	private ScoreboardManager manager = Bukkit.getScoreboardManager();
	final Scoreboard board = manager.getMainScoreboard();
	Objective starter = null;
	public int time;
	public boolean cancel = false;
	public boolean startable;
	public Player newplayer;
	private ArrayList<Player> currentplayers = new ArrayList<Player>();
	private boolean a = true;
	@Override
	public void run() {
		System.out.println(HideAndSeek.players);
		if(HideAndSeek.players != null) {
		if(a) {
			starter = board.registerNewObjective("START", "", "");
			a = false;
		}
		if ((!currentplayers.contains(newplayer) && newplayer != null) || (currentplayers == null && newplayer != null)) {
			setScoreBoard(newplayer);
			currentplayers.add(newplayer);
			newplayer = null;
		}
		if(time < 10) {
			Bukkit.broadcastMessage(ChatColor.GOLD + "Registering unavailable in "+time);
		}
		for (Player p: HideAndSeek.players) {
			updateScoreBoard(p);
		}
		if(cancel) {
			Bukkit.broadcastMessage("Game cancelled");
			for(Player p : HideAndSeek.players) {
				HideAndSeek.general.add(p);
			}
			HideAndSeek.players.clear();
			delScoreBoard();
			cancel = false;
			cancel();
		}
		if(time == 0) {
			Bukkit.broadcastMessage(ChatColor.GOLD + "Registering unavailable");
			if(HideAndSeek.players.isEmpty() || HideAndSeek.players.size() < 2) {
				Bukkit.broadcastMessage("Not enough players to start the game, cancelling");
				if(!HideAndSeek.players.isEmpty()) {
					for(Player p : HideAndSeek.players) {
						if(!HideAndSeek.general.contains(p)) {
							HideAndSeek.general.add(p);
						}
					}
				}
				HideAndSeek.players.clear();
				HideAndSeek.set = false;
				cancel();
			}
			else {
				startable = true;
				Bukkit.broadcastMessage("Waiting for the start of the game");
			}
			ArrayList<Player> lst = random(HideAndSeek.players);
			for(Player p : lst) {
				if (HideAndSeek.seekers.size() < HideAndSeek.hiders.size() || HideAndSeek.seekers.isEmpty()) {
					Bukkit.broadcastMessage("Inviting " + p.getName() + " as Seeker !");
					a = teamAdd(p.getName(),"seek");
					if(a) {
						HideAndSeek.seekers.add(p);
					}
					else {
						HideAndSeek.general.add(p);
						HideAndSeek.players.remove(p);
						p.sendMessage("Couldn't join game");
					}
				}
				else{
					Bukkit.broadcastMessage("Inviting " + p.getName() + " as Hider !");
					 a = teamAdd(p.getName(),"hide");
					 if(a) {
						 HideAndSeek.hiders.add(p);
					 }
					 else {
						HideAndSeek.general.add(p);
						HideAndSeek.players.remove(p);
						p.sendMessage("Couldn't join game");
					}
				}
			}
			delScoreBoard();
			cancel();
		}
		time--;
		}
	}
	
	public ArrayList<Player> random(ArrayList<Player> list){
		int min = 0, max = list.size();
        int r = min + (int)(Math.random() * ((max - min) + 1));
        for(int i = 0; i < r; i++) {
            min = 0; 
            max = list.size() - 1;
            int nombreAleat = min + (int)(Math.random() * ((max - min) + 1));


            System.out.println(nombreAleat);
            System.out.println("Liste A : "+list);

            list.add(list.get(nombreAleat));
            list.remove(nombreAleat);

            System.out.println("Liste B : "+list);
            System.out.println("---------");
        }
       return list;
	}
	
	private boolean teamAdd(String p,String nm) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		Team team = null;
		for (Team t : scoreboard.getTeams()) {
			if (t.getName().equals(nm)) {
				team = t;
				break;
			}
		}
		if (team != null) {
			team.addEntry(p);
			return true;
		}
		else {
			return false;
		}
	}
	
	public void setScoreBoard(Player p) {      
        starter.setDisplaySlot(DisplaySlot.SIDEBAR);
        starter.setDisplayName(ChatColor.DARK_PURPLE + "H&S");
        Score score = starter.getScore(ChatColor.GOLD + "START");
        score.setScore(time);
        p.setScoreboard(board);
	}
	public void updateScoreBoard(Player p) {
		Score score = starter.getScore(ChatColor.GOLD + "START");
		score.setScore(time);
		p.setScoreboard(board);
	}
	
	public void delScoreBoard() { 
        starter.unregister();
        board.clearSlot(DisplaySlot.SIDEBAR);
	}

}
