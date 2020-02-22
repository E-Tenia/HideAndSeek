package fr.redsifter.hideandseek.commands;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import fr.redsifter.hideandseek.HideAndSeek;
import fr.redsifter.hideandseek.timer.Timer;
import fr.redsifter.hideandseek.timer.Wait;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

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
	
	public static String[] purge2(String[] list) {
		
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
		String[] gm2 = null;
		if(gm != null) {
			gm2 = purge2(gm);
		}
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
		if(gm2 != null) {
			for(String str : gm2) {
				team.addEntry(str);	
			}
		}
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
	
	public void startTimer(int arg1,ArrayList<Player> lst) {
		Timer timer = new Timer();
		HideAndSeek.cancel = false;
		HideAndSeek.initialtime = arg1;
		timer.time = arg1;
		timer.lst = lst;
		timer.runTaskTimer(main, 0, 20);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		if (cmd.getName().equalsIgnoreCase("hs")){
			if(args.length > 0) {
			Wait wait = new Wait();
			switch(args[0]) {
			case "startgame":
				if(HideAndSeek.hiders.isEmpty() || HideAndSeek.seekers.isEmpty()) {
					sender.sendMessage("Can't start an empty game");
					return true;
				}
				Set<String> warplist = main.getConfig().getConfigurationSection("warps").getKeys(false);
				int arg1 = Integer.parseInt(args[1].trim());
				if(args.length < 3) {
					break;
				}
				else if((arg1 > 100000 || arg1 < 100) || !warplist.contains(args[2])) {
					sender.sendMessage("Invalid parameters, you must precise a time amount between 100000 and 100 and a valid warp (to list warp, use \"/hs listwarps\")");
					return true;
				}	
				sender.sendMessage("Starting new game of hide and seek !");
				ArrayList<Player> players = HideAndSeek.players;
				Location location = main.getConfig().getLocation("warps."+args[2]+".Location");
				if(location == null) {
					sender.sendMessage("Invalid warp name, aborting...");
					return true;
				}
				HideAndSeek.gamewarp = location;
				for(Player p : players) {
					p.teleport(location);
				}
				HideAndSeek.set = false;
				for (Player p : HideAndSeek.seekers) {
					p.setGameMode(GameMode.ADVENTURE);
					p.addPotionEffect((new PotionEffect(PotionEffectType.BLINDNESS, 20*60, 1)));
					p.addPotionEffect((new PotionEffect(PotionEffectType.SLOW, 20*60, 100)));
					p.addPotionEffect((new PotionEffect(PotionEffectType.JUMP, 20*60, 200)));
					p.sendMessage(ChatColor.GOLD + "The hiders are hiding, you'll be able to move after 1 min");
				}
				for (Player p : HideAndSeek.hiders) {
					p.setGameMode(GameMode.ADVENTURE);
					p.sendMessage(ChatColor.GOLD + "You have 1 min to get as far as possible and hide");
					}
				startTimer(arg1,HideAndSeek.players);
				break;
			case "setgamelist":
				if (args.length > 1) {
				int i = 0;
				int j = 0;
				int a = 0;
				int b = 0;
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
					init1 = (purged.length/2)-1;
					init2 = (purged.length/2)+1;
				}
				if (purged.length <= 1) {
					sender.sendMessage("Precise more players to invite");
					break;
				}
				String[] hiders = new String[init2];
				String[] seekers = new String[init1];
				for(j = 0;j < purged.length;j++) {
					HideAndSeek.players.add(Bukkit.getPlayerExact(purged[j]));
					if (j >= purged.length/2) {
						sender.sendMessage("Inviting " + gamelist[j] + " as Hider !");
						HideAndSeek.hiders.add(Bukkit.getPlayerExact(purged[j]));
						hiders[b] = purged[j];
						b++;
					}
					else{
						sender.sendMessage("Inviting " + gamelist[j] + " as Seeker !");
						HideAndSeek.seekers.add(Bukkit.getPlayerExact(purged[j]));
						seekers[a] = purged[j];
						a++;
					}
				}
				createTeam("seek",seekers);
				createTeam("hide",hiders);
				}
				else {
					sender.sendMessage("Precise players to invite");
				}
				break;
			case "setgame":
				if(HideAndSeek.set == true) {
					sender.sendMessage("A game is already set");
					return true;
				}
				if(args.length > 1) {
				int time = Integer.parseInt(args[1].trim());
					if(time < 20 || time > 100) {
						sender.sendMessage("You must precise a time amount between 20 and 100 seconds");
						return true;
					}	
				HideAndSeek.set = true;
				Bukkit.broadcastMessage(ChatColor.GOLD + "A NEW GAME OF H&S IS SET");
				TextComponent m = new TextComponent(ChatColor.DARK_PURPLE + "[JOIN GAME]");
				m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("Join").create()));
				m.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/hs join"));
				for(Player player : HideAndSeek.general) {
					player.spigot().sendMessage(m);
				}
				createTeam("hide",null);
				createTeam("seek",null);
				wait.time = time;
				wait.runTaskTimer(main,0,20);
				}
				else {
					sender.sendMessage("You must precise a time amount");
					return true;
				}
				break;
			case "cancelgame":
				wait.cancel = true;
				break;
			case "setgamewarp":
				if (sender instanceof Player) {
					if(args.length < 2) {
						sender.sendMessage("Precise a name");
						return true;
					}
					Player player = Bukkit.getPlayerExact(sender.getName());
					Location loc = player.getLocation();
					String name = args[1];
					main.getConfig().set("warps."+ name,name);
					main.getConfig().set("warps." + name + ".Location",loc);
					sender.sendMessage("Warp set successfuly");
				}
				break;
			case "remgamewarp":
				if(args.length < 2) {
					sender.sendMessage("Precise a warp");
					return true;
				}
				Set<String> warpset = main.getConfig().getConfigurationSection("warps").getKeys(false);
				String name = args[1];
				if(warpset.contains(name)) {
					main.getConfig().set("warps."+name, null);
					main.saveConfig();
					sender.sendMessage("Warp removed successfuly");
				}
				else {
					sender.sendMessage("This warp doesn't exist");
				}
				break;
			case "listwarps":
				Set<String> warps = main.getConfig().getConfigurationSection("warps").getKeys(false);
				sender.sendMessage("Available warps : ");
				for (String s : warps) {
					sender.sendMessage(s);
				}
				break;
			case "addplayer":
				if(HideAndSeek.gamewarp == null) {
					if(Bukkit.getPlayerExact(args[1]) instanceof Player) {
						if(HideAndSeek.players.contains(Bukkit.getPlayerExact(args[1]))) {
							sender.sendMessage("This player is already in a game");
							return true;
						}
						boolean a = true;
						if(args[2].equalsIgnoreCase("seeker")) {
							HideAndSeek.players.add(Bukkit.getPlayerExact(args[1]));
							HideAndSeek.seekers.add(Bukkit.getPlayerExact(args[1]));
							a = teamAdd(args[2],"seek");
						}
						else if(args[2].equalsIgnoreCase("hider")) {
							HideAndSeek.players.add(Bukkit.getPlayerExact(args[1]));
							HideAndSeek.hiders.add(Bukkit.getPlayerExact(args[1]));
							a = teamAdd(args[2],"hide");
						}
						else {
							sender.sendMessage("Invalid role, please choose between seeker and hider");
						}
						if(a) {
							sender.sendMessage("Player added successfully");
						}
						else {
							sender.sendMessage("No game is set");
						}
					}
					else {
						sender.sendMessage("This player is not here");
					}
				}
				else {
					sender.sendMessage("The game already started");
				}
				break;
			case "removeplayer":
				if(HideAndSeek.players.contains(Bukkit.getPlayerExact(args[1]))) {
					HideAndSeek.players.remove(Bukkit.getPlayerExact(args[1]));
					if(HideAndSeek.seekers.contains(Bukkit.getPlayerExact(args[1]))) {
						HideAndSeek.seekers.remove(Bukkit.getPlayerExact(args[1]));
					}
					else if(HideAndSeek.hiders.contains(Bukkit.getPlayerExact(args[1]))) {
						HideAndSeek.hiders.remove(Bukkit.getPlayerExact(args[1]));
					}
				}
				else {
					sender.sendMessage("This player is not in a game");
				}
				break;
			case "join":
				if(HideAndSeek.gamewarp != null) {
					sender.sendMessage("The game has already started");
					return true;
					
				}
				if(!HideAndSeek.set) {
					sender.sendMessage("There is no pending game");
					return true;
					
				}
				if(sender instanceof Player) {
					Player player = Bukkit.getPlayerExact(sender.getName());
					if(HideAndSeek.players.contains(Bukkit.getPlayerExact(sender.getName()))) {
						sender.sendMessage("You are already in a game");
						return true;
					}
					HideAndSeek.general.remove(player);
					HideAndSeek.players.add(player);
					System.out.println(HideAndSeek.players);
					/*boolean a = true;
					if(args.length <= 1) {
						if (HideAndSeek.seekers.size() > HideAndSeek.hiders.size()) {
							a = teamAdd(player.getName(),"hide");
							if(a) {
								HideAndSeek.players.add(player);
								HideAndSeek.hiders.add(player);
							}
						}
						else if (HideAndSeek.seekers.size() < HideAndSeek.hiders.size()) {
							a = teamAdd(player.getName(),"seek");
							if(a) {
								HideAndSeek.players.add(player);
								HideAndSeek.seekers.add(player);
							}
						}
						else {
							double r = Math.random();
							if(r > 0.5) {
								a = teamAdd(player.getName(),"seek");
								if(a) {
									HideAndSeek.players.add(player);
									HideAndSeek.seekers.add(player);
								}
							}
							else {
								a = teamAdd(player.getName(),"hide");
								if(a){
									HideAndSeek.players.add(player);
									HideAndSeek.hiders.add(player);
								}
							}
						}
					}
					else if(args[1].equalsIgnoreCase("seek")) {
						a = teamAdd(player.getName(),"seek");
						if(a) {
							HideAndSeek.players.add(player);
							HideAndSeek.seekers.add(player);
						}
					}
					else if(args[1].equalsIgnoreCase("hide")) {
						a = teamAdd(player.getName(),"hide");
						if(a) {
							HideAndSeek.players.add(player);
							HideAndSeek.hiders.add(player);
						}
					}*/	
					sender.sendMessage("Joined successfully");

				}
				else {
					sender.sendMessage("You must be a player to perform this command");
				}
				break;
			default:
				sender.sendMessage("Unknown hs command");
				break;
			}
			}
			else {
				sender.sendMessage("Not enough arguments");
			}
		}
	return false;
	}
}
