package com.dotw1ldcard.replay;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.dotw1ldcard.replay.container.PlayerSnap;
import com.dotw1ldcard.replay.container.PlayerState;
import com.mojang.authlib.GameProfile;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.PacketPlayOutKeepAlive;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;

/**
 * Handles the replaying of saved player packets.
 * 
 * @author DJ
 */
public class ReplayManager {

	private ReplayPlugin plugin;
	private Random random;
	private Map<Integer, PlayerSnap> tokenSync;
	private Map<UUID, OfflinePlayer> fakeToReal;

	public ReplayManager(ReplayPlugin plugin) {
		this.plugin = plugin;
		this.random = new Random();
		this.tokenSync = Collections.synchronizedMap(new HashMap<Integer, PlayerSnap>());
		this.fakeToReal = new HashMap<UUID, OfflinePlayer>();
	}

	/**
	 * Start the given replay.
	 * 
	 * @param snap replay to play out
	 */
	public void replay(PlayerSnap snap) {
		// Ignore null snaps
		if (snap == null) {
			return;
		}

		// This token synchronizes the "client" of the replayed player and the server so
		// that player data can be accessed without going through the hassle of sending
		// it all over
		int token = random.nextInt();
		this.tokenSync.put(token, snap);

		// This whole thing is just connecting a Netty client to the server and adding
		// PacketReplayHandler as a handler, practically copy-pasted from their website
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				EventLoopGroup workerGroup = new NioEventLoopGroup();
				Bootstrap b = new Bootstrap();
				b.group(workerGroup);
				b.channel(NioSocketChannel.class);
				b.option(ChannelOption.TCP_NODELAY, true);
				b.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel p0) throws Exception {
						p0.pipeline().addLast(new PacketReplayHandler(snap.getPackets(), token));
					}
				});

				ChannelFuture f;
				try {
					f = b.connect("localhost", 25565).sync();
					f.channel().closeFuture().sync();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					workerGroup.shutdownGracefully();
				}
			}
		});
		thread.start();
	}

	/**
	 * Get the actual player associated with the replayed player UUID.
	 * 
	 * @param uuid UUID of the replayed player
	 * @return player that the input is replaying
	 */
	public OfflinePlayer getRealPlayer(UUID uuid) {
		return this.fakeToReal.get(uuid);
	}

	/**
	 * Sync the incoming replay connection with the associated player data. Used
	 * internally.
	 * 
	 * @param channel incoming channel
	 * @param token   token to get player data from
	 * @return replayed player's new UUID
	 */
	public void syncReplay(Channel channel, int token) {

		// token gets us the full snap without sending it over, jackpot!
		PlayerSnap snap = tokenSync.get(token);
		if (snap == null) {
			// unless it's a fake token, in which case SHAME ON YOU
			channel.close();
			return;
		}

		// Get the network manager of the incoming channel to tie to an EntityPlayer
		NetworkManager netManager = (NetworkManager) channel.pipeline().get("packet_handler");
		// Generate random garbage UUID for replayed player
		UUID uuid = UUID.randomUUID();
		this.fakeToReal.put(uuid, snap.getPlayer());

		// this stuff's gotta go on the main thread
		new BukkitRunnable() {
			@Override
			public void run() {
				EntityPlayer ep = spawnDummyPlayer(uuid, snap.getPlayer().getName() + "'s Ghost", netManager,
						snap.getInitialState());
				// Pretty sure we need this but don't wanna check
				ep.playerConnection.sendPacket(new PacketPlayOutKeepAlive());
			}
		}.runTask(plugin);
		return;
	}

	/**
	 * Spawn in the dummy replayed player and ties it to the incoming replay
	 * connection.
	 * 
	 * @param uuid       garbage UUID of the replayed player
	 * @param name       nametag of the replayed player
	 * @param netManager incoming NetworkManager
	 * @param state      initial state to restore
	 * @return
	 */
	private EntityPlayer spawnDummyPlayer(UUID uuid, String name, NetworkManager netManager, PlayerState state) {
		// Login's been bypassed so this doesn't have to be valid
		GameProfile dummyGameProfile = new GameProfile(uuid, name);
		EntityPlayer dummyPlayer = new EntityPlayer(MinecraftServer.getServer(),
				MinecraftServer.getServer().getWorldServer(0), dummyGameProfile,
				new PlayerInteractManager(MinecraftServer.getServer().getWorldServer(0)));
		// Granted, it only spawns the player into the first world, so if there's more
		// than one world then change this

		// Restore player location
		dummyPlayer.locX = state.getLocation().getX();
		dummyPlayer.locY = state.getLocation().getY();
		dummyPlayer.locZ = state.getLocation().getZ();

		// Restore other state fields
		Player player = dummyPlayer.getBukkitEntity();
		player.getEquipment().setArmorContents(state.getArmorContents());
		player.getInventory().setContents(state.getInventoryContents());
		((CraftInventoryPlayer) player.getInventory()).getInventory().itemInHandIndex = state.getSlot();

		// Spawn in player
		MinecraftServer.getServer().getPlayerList().a(netManager, dummyPlayer);

		// This state has to be restored after spawning in
		player.setGameMode(state.getGameMode());

		return dummyPlayer;
	}

	public Set<UUID> getFakePlayers() {
		return this.fakeToReal.keySet();
	}

}
