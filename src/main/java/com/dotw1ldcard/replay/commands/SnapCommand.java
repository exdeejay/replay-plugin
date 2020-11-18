package com.dotw1ldcard.replay.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dotw1ldcard.replay.ReplayPlugin;
import com.dotw1ldcard.replay.SnapManager;

public class SnapCommand implements CommandExecutor {

	private ReplayPlugin plugin;

	public SnapCommand(ReplayPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		SnapManager snapManager = plugin.getSnapManager();
		if (args.length == 0) {

			if (sender instanceof Player) {
				Player player = (Player) sender;
				snapManager.snap(player);
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
				snapManager.snap(player);
			}
		}

		return true;
	}

}
