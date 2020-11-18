package com.dotw1ldcard.replay;

import java.util.Iterator;
import java.util.List;

import com.dotw1ldcard.replay.container.ReproducablePacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Netty handler for replaying saved packets.
 * 
 * @author DJ
 */
public class PacketReplayHandler extends ChannelInboundHandlerAdapter {

	private Iterator<ReproducablePacket> iter;
	private int token;

	public PacketReplayHandler(List<ReproducablePacket> packetsToReproduce, int token) {
		this.iter = packetsToReproduce.iterator();
		this.token = token;
	}

	// Occurs on socket connection
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ByteBuf buf = ctx.alloc().buffer();
		buf.writeByte(0x05);
		buf.writeByte(0x69);
		buf.writeInt(token);
		ctx.writeAndFlush(buf);
	}

	// Occurs on data read complete
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// If there's no more packets, we're done
		if (!iter.hasNext()) {
			ctx.close();
			return;
		}

		ReproducablePacket packet = iter.next();

		// Sleep for the change in time between the last packet and this one to recreate
		// the timeframe in which the packets came in
		Thread.sleep(packet.getDelta());

		ByteBuf buf = ctx.alloc().buffer();
		buf.writeByte(packet.getBytes().size());
		System.out.println("Sending packet");
		for (int i = 0; i < packet.getBytes().size(); i++) {
			buf.writeByte(packet.getBytes().get(i));
			System.out.println(packet.getBytes().get(i));
		}

		ctx.writeAndFlush(buf);

	}

	// Occurs on socket error
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) throws Exception {
		System.err.println("ERROR: Could not write all replay packets due to: " + t.toString());
		ctx.close();
	}

}
