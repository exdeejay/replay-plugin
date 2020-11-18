package com.dotw1ldcard.replay.container;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * Container for a snapshot of the state of a player.
 * 
 * @author DJ
 */
public class PlayerState {

	private Location location;
	private Vector velocity;
	private GameMode gameMode;
	private ItemStack[] armorContents;
	// There's a really good chance that storing both of these on every frame is
	// really inefficient on memory, but I'll take that for now
	private ItemStack[] inventoryContents;
	private int slot;

	/**
	 * Get the location of the saved player state.
	 * 
	 * @return location of the saved player state
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Set the location of the saved player state.
	 * 
	 * @param location location of the saved player state
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * Get the velocity of the saved player state. Currently unused.
	 * 
	 * @return velocity of the saved player state
	 */
	public Vector getVelocity() {
		return velocity;
	}

	/**
	 * Set the velocity of the saved player state. Currently unused.
	 * 
	 * @param velocity velocity of the saved player state
	 */
	public void setVelocity(Vector velocity) {
		this.velocity = velocity;
	}

	/**
	 * Get the gamemode of the saved player state.
	 * 
	 * @return gamemode of the saved player state
	 */
	public GameMode getGameMode() {
		return gameMode;
	}

	/**
	 * Set the gamemode of the saved player state.
	 * 
	 * @param gamemode gamemode of the saved player state
	 */
	public void setGameMode(GameMode gameMode) {
		this.gameMode = gameMode;
	}

	/**
	 * Get the armor contents of the saved player state.
	 * 
	 * @return armor contents of the saved player state
	 */
	public ItemStack[] getArmorContents() {
		return armorContents;
	}

	/**
	 * Set the armor contents of the saved player state.
	 * 
	 * @param armorContents armor contents of the saved player state
	 */
	public void setArmorContents(ItemStack[] armorContents) {
		this.armorContents = armorContents;
	}

	/**
	 * Get the inventory contents of the saved player state.
	 * 
	 * @return inventory contents of the saved player state
	 */
	public ItemStack[] getInventoryContents() {
		return inventoryContents;
	}

	/**
	 * Set the inventory contents of the saved player state.
	 * 
	 * @param inventoryContents inventory contents of the saved player state
	 */
	public void setInventoryContents(ItemStack[] inventoryContents) {
		this.inventoryContents = inventoryContents;
	}

	/**
	 * Get the held item slot of the saved player state.
	 * 
	 * @return held item slot of the saved player state
	 */
	public int getSlot() {
		return slot;
	}

	/**
	 * Set the held item slot of the saved player state.
	 * 
	 * @param slot held item slot of the saved player state
	 */
	public void setSlot(int slot) {
		this.slot = slot;
	}

}
