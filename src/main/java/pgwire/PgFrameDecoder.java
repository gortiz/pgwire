package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PgFrameDecoder extends ByteToMessageDecoder {
  private static final Logger LOGGER = LogManager.getLogger();
  private boolean expectingStartup;

  public PgFrameDecoder(boolean expectingStartup) {
    this.expectingStartup = expectingStartup;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
      throws Exception {
    int frameLength;
    if (in.readableBytes() < 4) {
      LOGGER.debug("At least 4 bytes are required to get the frame length, but {} were found", in.readableBytes());
      return ;
    }
    if (expectingStartup) {
      frameLength = in.getInt(in.readerIndex());
    } else {
      if (in.readableBytes() < 8) {
        LOGGER.debug("Not enough bytes to find the length of the not startup message");
        return ;
      }
      frameLength = in.getInt(in.readerIndex() + 1) + 1;
    }
    if (frameLength <= in.readableBytes()) {
      if (expectingStartup && in.getInt(4) == 196608) {
        expectingStartup = false;
      }
      out.add(in.readRetainedSlice(frameLength));
    } else {
      LOGGER.debug("Required {} bytes, but only {} available", frameLength, in.readableBytes());
    }
  }
}
