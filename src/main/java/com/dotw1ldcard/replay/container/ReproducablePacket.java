package com.dotw1ldcard.replay.container;

import java.util.List;
import java.util.UUID;

/**
 * Container for a packet intended to be reproduced.
 * 
 * @author DJ
 */
public class ReproducablePacket {

	private int delta;
	private List<Integer> bytes;
	private UUID target;

	public ReproducablePacket(int delta, List<Integer> bytes) {
		this.delta = delta;
		this.bytes = bytes;
	}

	/**
	 * Get time passed between last received packet and this one.
	 * 
	 * @return time passed since last packet
	 */
	public long getDelta() {
		return delta;
	}

	/**
	 * Get list of bytes that comprise this packet.
	 * 
	 * @return bytes of this packet
	 */
	public List<Integer> getBytes() {
		return bytes;
	}

	/**
	 * Get UUID of target.
	 * 
	 * This will only be present if packet is UseEntity on a player.
	 * 
	 * @return UUID of target
	 */
	public UUID getTarget() {
		return target;
	}

	/**
	 * Set UUID of target.
	 * 
	 * Should only be set if packet is UseEntity on a player.
	 * 
	 * @param target UUID of target player
	 */
	public void setTarget(UUID target) {
		this.target = target;
	}

}
