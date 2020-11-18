package com.dotw1ldcard.replay;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.comphenix.tinyprotocol.TinyProtocol;
import com.dotw1ldcard.replay.container.PlayerContinuousSnap;
import com.dotw1ldcard.replay.container.ReproducablePacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.PacketListener;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PlayerConnection;

/**
 * Registers the listeners for incoming and outgoing packets using a modified
 * version of TinyProtocol from ProtocolLib. This is needed for both packet
 * recording and replaying.
 * 
 * It shouldn't interfere with other modifications of the Minecraft network
 * code, but it hasn't been tested with software with that, so who knows ;)
 * 
 * @author DJ
 */
public class PacketEventListener {

	private ReplayPlugin plugin;
	private Map<OfflinePlayer, List<Long>> timestamps;

	public PacketEventListener(ReplayPlugin plugin) {
		this.plugin = plugin;
		this.timestamps = Collections.synchronizedMap(new HashMap<OfflinePlayer, List<Long>>());
	}

	public void injectPacketListener() {
		new TinyProtocol(plugin) {
			@Override
			public boolean onRawPacketIn(Channel channel, Object o) {
				// Read in bytes first, no matter what
				List<Integer> bytesList = new LinkedList<Integer>();

				ByteBuf buf = ((ByteBuf) o).duplicate();
				int readable = buf.readableBytes();
				for (int i = 0; i < readable; i++) {
					bytesList.add((int) (buf.readByte() & 0xFF));
				}

				// This is the magic byte to listen for to detect replayed player snapshots
				if (bytesList.size() > 0 && bytesList.get(0) == 0x69) { // (nice)
					buf.resetReaderIndex();
					buf.readByte();

					// Read in token used to synchronize player data with the incoming connection
					int token = buf.readInt();

					getPlugin().getReplayManager().syncReplay(channel, token);
					return false;
				}

				PacketListener pl = ((NetworkManager) channel.pipeline().get("packet_handler")).getPacketListener();
				if (pl instanceof PlayerConnection) {
					// Guaranteed to only do stuff on Play packets (ignores login packets)

					Player player = ((PlayerConnection) pl).player.getBukkitEntity();

					// Need to keep track of timestamps to calculate packet deltas.
					// I don't want to keep actual timestamps in the packets
					// so we do it here
					List<Long> timestampList = timestamps.get(player);
					if (timestampList == null) {
						timestampList = Collections.synchronizedList(new ArrayList<Long>());
						timestamps.put(player, timestampList);
					}
					long lastTimestamp = 0;
					if (timestampList.size() != 0) {
						lastTimestamp = timestampList.get(timestampList.size() - 1);
					}
					long timestamp = System.currentTimeMillis();
					timestampList.add(timestamp);

					// This is for set beginning and end snaps
					if (getPlugin().getSnapManager().isSnapping(player)) {
						int delta = (int) (timestamp - lastTimestamp);
						System.out.println(bytesList.get(0));
						ReproducablePacket packet = new ReproducablePacket(delta, bytesList);
						getPlugin().getSnapManager().getSnap(player).getPackets().add(packet);
					}

					// This is where the stuff we care about happens
					if (getPlugin().getSnapCacheManager().isCaching(player)) {

						// Create a frame of the player state
						getPlugin().getSnapCacheManager().updateCache(player);

						// Calculate packet delta
						int delta = (int) (timestamp - lastTimestamp);
						if (delta < 0) {
							delta = 0;
						}

						ReproducablePacket packet = new ReproducablePacket(delta, bytesList);

						PlayerContinuousSnap cache = getPlugin().getSnapCacheManager().getCache(player);

						// I don't remember if I need this or not but I ain't gonna touch it
						synchronized (cache) {

							// If the cache is empty, initialize it
							if (cache.getPackets().size() == 0) {
								cache.setEarliestTimestamp(timestamp);
							}

							List<ReproducablePacket> cachedPackets = cache.getPackets();
							cachedPackets.add(packet);

							// Calculate time spanned from earliest to latest packet in the cache and remove
							// oldest packets to get it within CACHE_SECONDS
							while (cachedPackets.size() != 0 && timestamp
									- cache.getEarliestTimestamp() > SnapCacheManager.CACHE_SECONDS * 1000) {
								cache.setEarliestTimestamp(
										cache.getEarliestTimestamp() + cache.getPackets().get(0).getDelta());
								cache.getPackets().remove(0);
								cache.getCachedStates().remove(0);

							}
						}
					}
				}

				return true;
			}

			@Override
			public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
				// The only reason we need this method is to modify the UUID shown to the
				// players to make the replayed players look pretty

				// If there's a way to do it with Bukkit I haven't found it yet
				if (packet instanceof PacketPlayOutNamedEntitySpawn) {
					PacketPlayOutNamedEntitySpawn playerPacket = (PacketPlayOutNamedEntitySpawn) packet;
					try {
						// Cancel if player is replay
						if (receiver == null) {
							return packet;
						}

						// Get the outgoing UUID
						Field uuidField = playerPacket.getClass().getDeclaredField("b");
						uuidField.setAccessible(true);
						UUID uuid = (UUID) uuidField.get(playerPacket);

						OfflinePlayer player = getPlugin().getReplayManager().getRealPlayer(uuid);
						if (player != null) {
							// Grab the real UUID of the player being replayed and swap it into the packet
							UUID realUUID = player.getUniqueId();
							if (realUUID != null) {
								uuidField.set(playerPacket, realUUID);
							}
						}
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
							| IllegalAccessException e) {
						e.printStackTrace();
					}

				}
				return packet;
			}
		};
	}

	public ReplayPlugin getPlugin() {
		return plugin;
	}

}