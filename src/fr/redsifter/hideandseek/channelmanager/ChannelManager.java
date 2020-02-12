package fr.redsifter.hideandseek.channelmanager;

import java.util.HashMap;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatColor;

public class ChannelManager {
	public HashMap<Player,String> channel = new HashMap<Player,String>();
	public HashMap<String,ArrayList<Player>> channelList = new HashMap<String,ArrayList<Player>>();
	//join channel
	public void joinChannel(Player player, String nm) {
		if(channel.get(player) != null) {
			String prevChannel = channel.get(player);
			leaveChannel(player,prevChannel);
		}
		ArrayList<Player> players = channelList.get(nm);
		if(players == null) {
			players = new ArrayList<Player>();
		}
		players.add(player);
		channelList.put(nm,players);
		channel.put(player,nm);
		player.sendMessage(ChatColor.GREEN + "You joined " + ChatColor.GOLD + nm);
	}
	//leave channel
	public void leaveChannel(Player player, String nm) {
		ArrayList<Player> players = channelList.get(nm);
		players.remove(player);
		channelList.put(nm, players);
		channel.remove(player);
		player.sendMessage(ChatColor.RED + "You left " + ChatColor.GOLD + nm);
	}
	//return a list of players in the channel
	public ArrayList<Player> getChannel(Player player){
		String nm = channel.get(player);
		return channelList.get(nm);
	}
}

