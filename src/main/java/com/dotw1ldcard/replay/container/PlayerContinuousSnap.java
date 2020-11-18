package com.dotw1ldcard.replay.container;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Container for continuous cache of a player's packets. This is gonna be
 * accessed asynchronously a <i>lot</i>.
 * 
 * @author DJ
 */
public class PlayerContinuousSnap {

	private Player player;
	private List<ReproducablePacket> packets;
	private List<PlayerState> cachedStates;
	private long earliestTimestamp = 0;

	public PlayerContinuousSnap(Player player) {
		this.player = player;
		this.packets = Collections.synchronizedList(new LinkedList<ReproducablePacket>());
		this.cachedStates = Collections.synchronizedList(new LinkedList<PlayerState>());
	}

	/**
	 * Get the player associated with this cache. Doesn't have to be online.
	 * 
	 * @return OfflinePlayer associated with the cached packets
	 */
	public OfflinePlayer getPlayer() {
		return player;
	}

	/**
	 * Get the packets currently cached. Expect it to change.
	 * 
	 * @return cached packets
	 */
	public List<ReproducablePacket> getPackets() {
		return packets;
	}

	/**
	 * Get the player state associated with each packet.
	 * 
	 * This is needed for the initial state of any static snaps made from this
	 * cache.
	 * 
	 * @return list of cached player states
	 */
	public List<PlayerState> getCachedStates() {
		return cachedStates;
	}

	/**
	 * Get the time that the first packet came in.
	 * 
	 * This is needed to calculate the amount of time spanned by these packets, as
	 * the only other way to do it is to loop through every packet and add up the
	 * deltas every time, which is super inefficient
	 * 
	 * @return timestamp of oldest packet
	 */
	public long getEarliestTimestamp() {
		return earliestTimestamp;
	}

	/**
	 * Set the timestamp of the oldest packet.
	 * 
	 * @param earliestTimestamp timestamp of the oldest packet
	 */
	public void setEarliestTimestamp(long earliestTimestamp) {
		this.earliestTimestamp = earliestTimestamp;
	}

}
