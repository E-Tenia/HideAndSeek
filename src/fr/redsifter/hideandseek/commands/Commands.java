package fr.redsifter.hideandseek.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class Commands implements CommandExecutor {
	
	private void createTeam(String nm,String[] gm) {
		String name = nm;
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		Team team = null;

		for (Team t : scoreboard.getTeams()) {
			if (t.getName().equals(name)) {
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
				sender.sendMessage("Not done yet...");
				break;
			case "setgamelist":
				if (args.length > 1) {
				int i = 0;
				int j = 0;
				int a = 0;
				String[] gamelist = new String[12];
				for (i = 1;i < args.length;i++){
					gamelist[j] = args[i];
					j++;
					}
				String[] hiders = new String[6];
				String[] seekers = new String[6];
				Location spawn = new Location(Bukkit.getWorld("world"),(-223.500),66,(-213.500));
				for(j = 0;j < i-1;j++) {
					Player pl = Bukkit.getPlayerExact(gamelist[j]);
					if (pl != null) {
						if (j - a > (i/2)-1) {
							sender.sendMessage("Inviting " + gamelist[j] + " as Hider !");
							hiders[j - a] = gamelist[j];
						}
						else{
							sender.sendMessage("Inviting " + gamelist[j] + " as Seeker !");
							seekers[j - a] = gamelist[j];
						}
						pl.teleport(spawn);
					}
					else {
						a++;
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


