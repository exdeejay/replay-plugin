package com.dotw1ldcard.replay.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dotw1ldcard.replay.ReplayManager;
import com.dotw1ldcard.replay.ReplayPlugin;
import com.dotw1ldcard.replay.SnapManager;
import com.dotw1ldcard.replay.container.PlayerSnap;

public class ReplayCommand implements CommandExecutor {

	private ReplayPlugin plugin;

	public ReplayCommand(ReplayPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		SnapManager snapManager = plugin.getSnapManager();
		ReplayManager replayManager = plugin.getReplayManager();
		if (args.length == 0) {

			if (sender instanceof Player) {
				Player player = (Player) sender;
				PlayerSnap snap = snapManager.getSnap(player);
				if (snap == null) {
					sender.sendMessage("That player does not have a snap saved.");
				} else if (snapManager.isSnapping(player)) { 
					sender.sendMessage("Currently snapping " + player.getName() + ", please stop the snap before replaying.");
				} else {
					sender.sendMessage("Replaying snap of " + player.getName() + "...");
					replayManager.replay(snap);
				}
			} else {
				return false;
			}

		} else {
			
			for (String s : args) {
				Player player = Bukkit.getPlayer(s);
				if (player == null) {
					sender.sendMessage("The player " + s + " is not online right now.");
					return true;
				}
				PlayerSnap snap = snapManager.getSnap(player);
				if (snap == null) {
					sender.sendMessage("That player does not have a snap saved.");
				} else if (snapManager.isSnapping(player)) { 
					sender.sendMessage("Currently snapping " + player.getName() + ", please stop the snap before replaying.");
				} else {
					sender.sendMessage("Replaying snap of " + player.getName() + "...");
					replayManager.replay(snap);
				}
			}

		}

		
		return true;
	}

}
