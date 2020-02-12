package fr.redsifter.hideandseek.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.redsifter.hideandseek.timer.Timer;

public class Commands implements CommandExecutor {
	
	public static String[] purge(String[] list) {
		
		String[] gm = new String[list.length];
		int x = 0;
		for(int b = 0; b < list.length;b++) {
			if (list[b] != null) {
				gm[x] = list[b];
				x++;
			}
		}
		System.out.println(x);
		String[] gmlst = new String[x];
		for(int a = 0; a < gmlst.length;a++) {
			if (gm[a] != null) {
				gmlst[a] = gm[a];
			}
		}

		return gmlst;
	}
	
	private void createTeam(String nm,String[] gm) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		Team team = null;
		
		for (Team t : scoreboard.getTeams()) {
			if (t.getName().equals(nm)) {
				team = t;
				break;
			}
		}

		if (team == null)
			team = scoreboard.registerNewTeam(nm);
		if (nm == "hide") {
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
		}
		for(String str : gm) {
		team.addEntry(str);	
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		if (cmd.getName().equalsIgnoreCase("test")){
		sender.sendMessage("Test working great so far!");
		return false;
		}
		
		else if (cmd.getName().equalsIgnoreCase("hs")){
			if(args.length > 0) {
			switch(args[0]) {
			case "startgame":
				sender.sendMessage("Starting new game of hide and seek !");
				Timer timer = new Timer();
				timer.run();
				break;
			case "setgamelist":
				if (args.length > 1) {
				int i = 0;
				int j = 0;
				int a = 0;
				int b = 0;
				int x = 0;
				String[] gamelist = new String[12];
				for (i = 1;i < args.length;i++){
					gamelist[j] = args[i];
					j++;
					}
				String[] purged = purge(gamelist);
				int init1 = 0;
				int init2 = 0;
				if (purged.length %2 == 0) {
					init1 = purged.length/2;
					init2 = purged.length/2;
					}
				else {
					init1 = purged.length-1;
					init2 = purged.length+1;
				}
				if (init1 == 0 || init2 == 0) {
					sender.sendMessage("Precise more players to invite");
					break;
				}
				String[] hiders = new String[init2];
				String[] seekers = new String[init1];
				Location spawn = new Location(Bukkit.getWorld("world"),(-296.500),79,(-260.500));
				for(j = 0;j < i-1;j++) {
					Player pl = Bukkit.getPlayerExact(gamelist[j]);
					if (pl != null) {
						if (j - x > (i/2)-1) {
							sender.sendMessage("Inviting " + gamelist[j] + " as Hider !");
							hiders[b - x] = gamelist[j];
							b++;
						}
						else{
							sender.sendMessage("Inviting " + gamelist[j] + " as Seeker !");
							seekers[a - x] = gamelist[j];
							a++;
						}
						pl.teleport(spawn);
					}
					else {
						x++;
						sender.sendMessage("Player " + gamelist[j] + " is not here !");
					}
				}
				createTeam("seek",seekers);
				createTeam("hide",hiders);
				}
				else {
					sender.sendMessage("Precise players to invite");
				}
				break;
			default:
				sender.sendMessage("Unknown command");
				break;
			}
			}
			else {
				sender.sendMessage("Precise arguments : startgame, setgamelist, setgamewarp");
			}
		}
	return false;
	}
}


