package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.util.function.IntFunction;


/**
 * This is the default ChannelInitializer.
 * <p>
 * The default pipeline consist of a {@link LoggingHandler}, a {@link PgFrameDecoder} and
 * a {@link FrontendMessageListener}.
 */
public class PostgresBackendChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final IntFunction<PgType> typeLibrary;
  private final FrontendMessageListener<ChannelHandlerContext, ByteBuf, ByteBuf> listener;

  public PostgresBackendChannelInitializer(IntFunction<PgType> typeLibrary,
      FrontendMessageListener<ChannelHandlerContext, ByteBuf, ByteBuf> listener) {
    this.typeLibrary = typeLibrary;
    this.listener = listener;
  }

  @Override
  protected void initChannel(SocketChannel ch)
      throws Exception {
    ch.pipeline()
        .addLast(new LoggingHandler(LogLevel.DEBUG))
        .addLast(new PgFrameDecoder(true))
        .addLast(new FrontendPostgresHandler(listener));
  }
}
