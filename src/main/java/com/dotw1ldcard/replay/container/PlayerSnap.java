package com.dotw1ldcard.replay.container;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;

/**
 * Container for static player replay.
 * 
 * @author DJ
 */
public class PlayerSnap {

	private OfflinePlayer player;
	private List<ReproducablePacket> packets;
	private PlayerState initialState;

	public PlayerSnap(OfflinePlayer player, PlayerState initialState) {
		this.player = player;
		this.initialState = initialState;
		this.packets = new ArrayList<ReproducablePacket>();
	}

	public PlayerSnap(OfflinePlayer player) {
		this.player = player;
	}

	public PlayerSnap(PlayerContinuousSnap continuous) {
		this(continuous.getPlayer());
		synchronized (continuous) {
			setPackets(new ArrayList<ReproducablePacket>(continuous.getPackets()));
			setInitialState(continuous.getCachedStates().get(0));
		}
	}

	/**
	 * Get the player associated with this replay. Doesn't have to be online.
	 * 
	 * @return player associated with this replay
	 */
	public OfflinePlayer getPlayer() {
		return player;
	}

	/**
	 * Get the packets that comprise this replay. Will not change.
	 * 
	 * @return packets of replay
	 */
	public List<ReproducablePacket> getPackets() {
		return packets;
	}
	
	private void setPackets(List<ReproducablePacket> packets) {
		this.packets = packets;
	}

	/**
	 * Get initial state of player in this replay.
	 * 
	 * @return initial player state in replay
	 */
	public PlayerState getInitialState() {
		return initialState;
	}

	private void setInitialState(PlayerState initialState) {
		this.initialState = initialState;
	}

}
