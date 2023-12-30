package pgwire;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public abstract class PostgresServer {

  private static final Logger LOGGER = LogManager.getLogger(PostgresServer.class);

  private final String host;
  private int port;
  private Channel channel;

  public PostgresServer() {
    this("0.0.0.0");
  }

  public PostgresServer(String host) {
    this(host, 5432);
  }

  public PostgresServer(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void start()
      throws InterruptedException {
    ServerBootstrap serverBootstrap = new ServerBootstrap();

    EventLoopGroup parentGroup = createParentGroup();
    EventLoopGroup childGroup = createChildGroup();

    serverBootstrap.group(parentGroup, childGroup)
        .channel(getChannelClass())
        .handler(new LoggingHandler(LogLevel.TRACE))
        .childHandler(new PostgresBackendChannelInitializer(createLibrary(), createMessageListener()));

    ChannelFuture bind = serverBootstrap.bind(host, port);

    beforeStart(bind);

    try {
      channel = bind.sync().channel();
      port = ((java.net.InetSocketAddress) channel.localAddress()).getPort();

      LOGGER.info("Listening on {}", channel.localAddress());

    } catch (InterruptedException ex) {
      shutdownGroup(parentGroup);
      shutdownGroup(childGroup);
      throw ex;
    }
    channel.closeFuture().addListener(future -> {
      shutdownGroup(parentGroup);
      shutdownGroup(childGroup);
    });
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public ChannelFuture close() {
    return channel.close();
  }

  protected IntFunction<PgType> createLibrary() {
    return new StandardTypeLibrary();
  }

  protected abstract FrontendMessageListener<ChannelHandlerContext, ByteBuf, ByteBuf> createMessageListener();

  protected void beforeStart(ChannelFuture channelFuture) {
  }

  protected void shutdownGroup(EventLoopGroup group) {
    group.shutdownGracefully(0, 1000, TimeUnit.MILLISECONDS);
  }

  protected Class<? extends ServerChannel> getChannelClass() {
    return NioServerSocketChannel.class;
  }

  protected EventLoopGroup createParentGroup() {
    return new NioEventLoopGroup();
  }

  protected EventLoopGroup createChildGroup() {
    return new NioEventLoopGroup();
  }
}
