package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class SenderUtils {


  public static void writeByteN(ByteBuf into, ByteBuf data) {
    if (data == null) {
      into.writeInt(-1);
    } else {
      into.writeInt(data.readableBytes());
      into.writeBytes(data.slice());
    }
  }

  public static int byteNLength(ByteBuf data) {
    return 4 + data.readableBytes();
  }

  public static int cStrLength(ByteBuf data) {
    return 1 + data.readableBytes();
  }

  public static void writeCStr(ByteBuf buffer, ByteBuf data) {
    buffer.writeBytes(data.slice());
    buffer.writeByte(0);
  }

  public static void writeCStr(ByteBuf buffer, CharSequence str) {
    buffer.writeCharSequence(str, StandardCharsets.UTF_8);
    buffer.writeByte(0);
  }

  public static void writeUnsignedShort(ByteBuf out, int unsignedShort) {
    out.writeShort((byte) unsignedShort);
  }

  public static ChannelFuture sendSimpleMsg(ChannelHandlerContext ctx, char id) {
    return ctx.write(
        ctx.alloc().buffer(5)
            .writeByte(id)
            .writeInt(4)
    );
  }

  public static void copyAnyResponse(ChannelHandlerContext ctx, Format overallFormat, List<Format> columnFormats,
      char id) {
    int length = 4 + 1 + 2 + columnFormats.size() * 2;
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(id);
    buffer.writeInt(length);
    buffer.writeByte(overallFormat.getId());
    writeUnsignedShort(buffer, columnFormats.size());
    for (Format columnFormat : columnFormats) {
      writeUnsignedShort(buffer, columnFormat.getId());
    }

    ctx.writeAndFlush(buffer);
  }
}
