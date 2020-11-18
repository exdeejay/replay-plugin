package com.dotw1ldcard.replay.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.netty.buffer.ByteBuf;

/**
 * Tools to read and write VarInts. Currently unused.
 * 
 * @author DJ
 */
public class VarIntUtil {

	public static int readVarInt(InputStream in) throws IOException {
		int i = 0;
		int j = 0;
		int b0;
		do {
			b0 = in.read();
			i |= (b0 & 0x7F) << j++ * 7;
			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}
		} while ((b0 & 0x80) == 0x80);
		return i;
	}

	public static void writeVarInt(int i, OutputStream out) throws IOException {
		while ((i & 0xFFFFFF80) != 0x0) {
			out.write((i & 0x7F) | 0x80);
			i >>>= 7;
		}
		out.write(i);
	}

	public static void writeVarInt(int i, ByteBuf out) throws IOException {
		while ((i & 0xFFFFFF80) != 0x0) {
			out.writeByte((i & 0x7F) | 0x80);
			i >>>= 7;
		}
		out.writeByte(i);
	}

}
