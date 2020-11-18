package com.dotw1ldcard.replay;

import org.bukkit.plugin.java.JavaPlugin;

import com.dotw1ldcard.replay.commands.ReplayCommand;
import com.dotw1ldcard.replay.commands.SnapCommand;

public class ReplayPlugin extends JavaPlugin {

	private SnapManager snapManager;
	private ReplayManager replayManager;
	private SnapCacheManager snapCacheManager;

	@Override
	public void onEnable() {
		this.snapManager = new SnapManager(this);
		this.replayManager = new ReplayManager(this);
		this.snapCacheManager = new SnapCacheManager(this);

		PacketEventListener packetEventListener = new PacketEventListener(this);
		packetEventListener.injectPacketListener();
		
		getCommand("snap").setExecutor(new SnapCommand(this));
		getCommand("replay").setExecutor(new ReplayCommand(this));
	}

	public SnapManager getSnapManager() {
		return snapManager;
	}

	public ReplayManager getReplayManager() {
		return replayManager;
	}

	public SnapCacheManager getSnapCacheManager() {
		return snapCacheManager;
	}

}