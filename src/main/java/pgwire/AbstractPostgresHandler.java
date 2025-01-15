package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.SimpleChannelInboundHandler;
import java.nio.charset.StandardCharsets;


/**
 * An {@link io.netty.channel.ChannelInboundHandler} that decodes Postgres messages.
 *
 * It assumes that received {@link ByteBuf} is already framed (see {@link PgFrameDecoder}).
 */
public abstract class AbstractPostgresHandler extends SimpleChannelInboundHandler<ByteBuf> {

  /**
   * Returns a slice of the given buf, assuming the first 4 bytes (read as int32) indicate the length of the slice.
   *
   * In case the first 4 bytes (read as int32) is negative, null is returned.
   * @param buf
   * @return
   */
  public static ByteBuf readSliceFromLength(ByteBuf buf) {
    int length = buf.readInt();
    if (length < 0) {
      return null;
    }
    return buf.readSlice(length);
  }

  public static ByteBuf readNullEndedSlice(ByteBuf buf) {
    int length = buf.bytesBefore((byte) 0);
    if (length < 0) {
      return buf.readSlice(0);
    }
    ByteBuf byteBuf = buf.readSlice(length);
    buf.readByte();
    return byteBuf;
  }

  public static String readNullEndedString(ByteBuf buf) {
    int length = buf.bytesBefore((byte) 0);
    if (length < 0) {
      return "";
    }
    String string = buf.readCharSequence(length, StandardCharsets.UTF_8).toString();
    buf.readByte();
    return string;
  }
}
