package fr.redsifter.hideandseek.commands;

import java.io.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.redsifter.hideandseek.HideAndSeek;

public class Commands implements CommandExecutor {
	private HideAndSeek main;
	
	public Commands(HideAndSeek hideAndSeek) {
		this.main = hideAndSeek;
	}

	public static String[] purge(String[] list,CommandSender p) {
		
		String[] gm = new String[list.length];
		int x = 0;
		for(int b = 0; b < list.length;b++) {
			if (list[b] != null) {
				gm[x] = list[b];
				x++;
			}
		}
		String[] gmlst = new String[x];
		x = 0;
		for(int a = 0; a < gmlst.length;a++) {
			if (gm[a] != null && Bukkit.getPlayerExact(gm[a]) != null) {
				gmlst[a] = gm[a];
				x++;
			}
			else if (gm[a] != null && Bukkit.getPlayerExact(gm[a]) == null) {
				p.sendMessage("Player " + gm[a] + " is not here !");
			}
		}
		String[] gmlst2 = new String[x];
		x = 0;
		for (String s : gmlst) {
			if(s != null) {
				gmlst2[x] = s;
			}
			x++;
		}
		return gmlst2;
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
		if (cmd.getName().equalsIgnoreCase("hs")){
			if(args.length > 0) {
			switch(args[0]) {
			case "startgame":
				sender.sendMessage("Starting new game of hide and seek !");
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
				String[] purged = purge(gamelist,sender);
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
				if (purged.length <= 1) {
					sender.sendMessage("Precise more players to invite");
					break;
				}
				String[] hiders = new String[init2];
				String[] seekers = new String[init1];
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
					}
				}
				createTeam("seek",seekers);
				createTeam("hide",hiders);
				}
				else {
					sender.sendMessage("Precise players to invite");
				}
				break;
			case "setgamewarp":
				if (sender instanceof Player) {
					Player player = Bukkit.getPlayerExact(sender.getName());
					Location loc = player.getLocation();
					String name = args[0];
					main.getConfig().set("warps."+ name,name);
					main.getConfig().set("warps." + name + ".Location",loc);
				}
				break;
			case "remgamewarp":
				String name = args[0];
				main.getConfig().set("warps."+name, null);
				main.saveConfig();
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

public void ConfigWrite(String txt) {
		try
		{
			FileWriter fw = new FileWriter("HideAndSeek/config.yml",true);
			fw.write(txt);
			fw.close();
		}
		catch(IOException ioe)
		{
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
}
