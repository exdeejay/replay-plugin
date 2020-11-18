package com.dotw1ldcard.replay;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.dotw1ldcard.replay.container.PlayerContinuousSnap;
import com.dotw1ldcard.replay.container.PlayerSnap;
import com.dotw1ldcard.replay.container.PlayerState;

/**
 * Handles the caching of player packets for instant replays.
 * 
 * @author DJ
 */
public class SnapCacheManager implements Listener {

	public static final int CACHE_SECONDS = 15;

	private ReplayPlugin plugin;
	private Set<Player> caching;
	private Map<Player, PlayerContinuousSnap> cached;

	public SnapCacheManager(ReplayPlugin plugin) {
		this.plugin = plugin;
		this.caching = Collections.synchronizedSet(new HashSet<Player>());
		this.cached = Collections.synchronizedMap(new HashMap<Player, PlayerContinuousSnap>());

		Bukkit.getPluginManager().registerEvents(this, this.plugin);
	}

	/**
	 * Create a snapshot of the packets sent by the player cached over the last
	 * CACHE_SECONDS. This is independent of the actual cache and its contents will
	 * stay static.
	 * 
	 * @param player player to snapshot cache of
	 * @return newly created snapshot of player's packet cache
	 */
	public PlayerSnap snapshot(Player player) {
		if (!this.caching.contains(player) || this.cached.get(player).getPackets().size() == 0) {
			return null;
		} else {
			return new PlayerSnap(this.cached.get(player));
		}
	}

	/**
	 * Checks if the given player's packets are being cached.
	 * 
	 * @param player player to check
	 * @return true if caching the given player's packets, false otherwise
	 */
	public boolean isCaching(Player player) {
		return this.caching.contains(player);
	}

	/**
	 * Initiate caching of the given player's packets.
	 * 
	 * @param player player to get packets for
	 */
	public void startCaching(Player player) {
		if (!this.caching.contains(player)) {
			this.cached.remove(player);
			this.caching.add(player);

			PlayerContinuousSnap snap = new PlayerContinuousSnap(player);
			this.cached.put(player, snap);
		}
	}

	/**
	 * Stop caching this player's packets. Preserves the already-cached packets in
	 * memory.
	 * 
	 * @param player player to stop caching
	 */
	public void stopCaching(Player player) {
		this.caching.remove(player);
	}

	/**
	 * Get the reference to the cached packets for the given player. This will
	 * change as new packets come in.
	 * 
	 * @param player
	 * @return the player's packet cache
	 */
	public PlayerContinuousSnap getCache(Player player) {
		return this.cached.get(player);
	}

	/**
	 * Update the player state for a given packet. Used internally.
	 * 
	 * @param player player to update cache of
	 */
	public void updateCache(Player player) {
		if (this.caching.contains(player)) {
			PlayerState state = new PlayerState();
			state.setLocation(player.getLocation());
			state.setVelocity(player.getVelocity());
			state.setArmorContents(player.getEquipment().getArmorContents());
			state.setInventoryContents(player.getInventory().getContents());
			state.setSlot(player.getInventory().getHeldItemSlot());
			state.setGameMode(player.getGameMode());

			PlayerContinuousSnap snap = this.cached.get(player);
			synchronized (snap) {
				snap.getCachedStates().add(state);
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!plugin.getReplayManager().getFakePlayers().contains(event.getPlayer().getUniqueId())) {
			// If not a replay-created player, cache this player's packets
			startCaching(event.getPlayer());
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		// Stop caching on player quit
		stopCaching(event.getPlayer());
	}

}
