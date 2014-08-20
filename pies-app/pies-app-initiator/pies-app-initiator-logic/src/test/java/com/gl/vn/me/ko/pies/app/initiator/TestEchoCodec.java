package com.gl.vn.me.ko.pies.app.initiator;

import static org.junit.Assert.assertTrue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import java.util.Arrays;
import org.junit.Test;

public final class TestEchoCodec {
	public TestEchoCodec() {
	}

	@Test
	public final void testEncode() {
		final EmbeddedChannel channel = new EmbeddedChannel(new EchoCodec());
		final byte[] bytesOut;
		{//write out data
			final ByteBuf bufOut = Unpooled.buffer(4);
			bufOut.writeInt(1);
			bytesOut = byteBufToArray(bufOut);
			final Object msgOut = bytesOut;
			assertTrue("Assert success channel write out", channel.writeOutbound(msgOut));
		}
		{//test encoded data
			final ByteBuf bufOutEncoded = (ByteBuf)channel.readOutbound();
			final int bufOutEncodedSize = bufOutEncoded.readableBytes();
			final byte[] bytesOutEncoded = new byte[bufOutEncodedSize];
			bufOutEncoded.readBytes(bytesOutEncoded, 0, bufOutEncodedSize);
			assertTrue("Assert message was correctly encoded", Arrays.equals(bytesOut, bytesOutEncoded));
		}
	}

	@Test(expected = EncoderException.class)
	public final void testEncodeEmpty() {
		final EmbeddedChannel channel = new EmbeddedChannel(new EchoCodec());
		channel.writeOutbound((Object)new byte[0]);
	}

	@Test
	public final void testDecode() {
		final EmbeddedChannel channel = new EmbeddedChannel(new EchoCodec());
		final int data = 1;
		final byte[] bytesIn;
		{//prepare channel for in data
			final ByteBuf bufOut = Unpooled.buffer(4);
			bufOut.writeInt(data);
			final Object msgOut = byteBufToArray(bufOut);
			channel.writeOutbound(msgOut);
		}
		{//write in data
			final ByteBuf bufIn = Unpooled.buffer(4);
			bufIn.writeInt(data);
			bytesIn = byteBufToArray(bufIn);
			final Object msgIn = bufIn;
			assertTrue("Assert success channel write in", channel.writeInbound(msgIn));
		}
		{//test decoded data
			final byte[] bytesInDecoded = (byte[])channel.readInbound();
			assertTrue("Assert message was correctly decoded", Arrays.equals(bytesIn, bytesInDecoded));
		}
	}

	@Test
	public final void testMultipleDecode() {
		final EmbeddedChannel channel = new EmbeddedChannel(new EchoCodec());
		final int data1 = 1;
		final long data2 = 2;
		final byte[] bytesIn1;
		final byte[] bytesIn2;
		{//prepare channel for in data1
			final ByteBuf bufOut1 = Unpooled.buffer(4);
			bufOut1.writeInt(data1);
			final Object msgOut1 = byteBufToArray(bufOut1);
			channel.writeOutbound(msgOut1);
		}
		{//prepare channel for in data2
			final ByteBuf bufOut2 = Unpooled.buffer(8);
			bufOut2.writeLong(data2);
			final Object msgOut2 = byteBufToArray(bufOut2);
			channel.writeOutbound(msgOut2);
		}
		{//write in data1
			final ByteBuf bufIn1 = Unpooled.buffer(4);
			bufIn1.writeInt(data1);
			bytesIn1 = byteBufToArray(bufIn1);
			final Object msgIn1 = bufIn1;
			channel.writeInbound(msgIn1);
		}
		{//write in data2
			final ByteBuf bufIn2 = Unpooled.buffer(8);
			bufIn2.writeLong(data2);
			bytesIn2 = byteBufToArray(bufIn2);
			final Object msgIn2 = bufIn2;
			channel.writeInbound(msgIn2);
		}
		{//test decoded data1
			final byte[] bytesInDecoded1 = (byte[])channel.readInbound();
			assertTrue("Assert message 1 was correctly decoded", Arrays.equals(bytesIn1, bytesInDecoded1));
		}
		{//test decoded data2
			final byte[] bytesInDecoded2 = (byte[])channel.readInbound();
			assertTrue("Assert message 2 was correctly decoded", Arrays.equals(bytesIn2, bytesInDecoded2));
		}
	}

	private final byte[] byteBufToArray(final ByteBuf buf) {
		final int size = buf.readableBytes();
		final byte[] result = new byte[size];
		buf.duplicate().readBytes(result);
		return result;
	}
}
