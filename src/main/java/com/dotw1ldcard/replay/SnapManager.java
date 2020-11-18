package com.dotw1ldcard.replay;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.dotw1ldcard.replay.container.PlayerSnap;
import com.dotw1ldcard.replay.container.PlayerState;

/**
 * Handles creation of set beginning and end player replays.
 * 
 * @author DJ
 */
public class SnapManager {

	private Map<OfflinePlayer, PlayerSnap> snaps;
	private Set<Player> snapping;

	public SnapManager(ReplayPlugin plugin) {
		this.snaps = Collections.synchronizedMap(new HashMap<OfflinePlayer, PlayerSnap>());
		this.snapping = Collections.synchronizedSet(new HashSet<Player>());
	}

	/**
	 * Start or stop recording a player.
	 * 
	 * Starting a new snap overwrites previous snaps.
	 * 
	 * @param player player to record
	 */
	public void snap(Player player) {
		if (!this.snapping.contains(player)) {
			player.sendMessage("Snapping you...");
			this.snaps.remove(player);
			this.snapping.add(player);

			PlayerState state = new PlayerState();
			state.setLocation(player.getLocation());
			state.setVelocity(player.getVelocity());
			state.setArmorContents(player.getEquipment().getArmorContents());
			state.setInventoryContents(player.getInventory().getContents());
			state.setSlot(player.getInventory().getHeldItemSlot());
			state.setGameMode(player.getGameMode());
			PlayerSnap snap = new PlayerSnap(player, state);
			this.snaps.put(player, snap);
		} else {
			this.snapping.remove(player);
			player.sendMessage("Snap complete");
		}
	}

	/**
	 * Get latest player replay.
	 * 
	 * @param player player to get replay for
	 * @return player replay
	 */
	public PlayerSnap getSnap(Player player) {
		return this.snaps.get(player);
	}

	/**
	 * Check if this player is being recorded.
	 * 
	 * @param player player to check if recording
	 * @return true if recording player, false otherwise
	 */
	public boolean isSnapping(Player player) {
		return this.snapping.contains(player);
	}

}
