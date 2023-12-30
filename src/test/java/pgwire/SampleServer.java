package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;


public class SampleServer {

  public static void main(String[] args)
      throws InterruptedException {
    PostgresServer server = new PostgresServer("localhost") {
      @Override
      protected FrontendMessageListener<ChannelHandlerContext, ByteBuf, ByteBuf> createMessageListener() {
        BackendMessageSender sender = new BackendMessageSender();

        return new FrontendMessageListener.Abstract<>() {
          @Override
          public void onSslRequest(ChannelHandlerContext ctx) {
            ByteBuf byteBuf = ctx.alloc().buffer(1).writeByte('N');
            ctx.writeAndFlush(byteBuf);
          }

          @Override
          public void onStartup(ChannelHandlerContext ctx, int version, Map<ByteBuf, ByteBuf> parameters) {
            sender.onAuthenticationOk(ctx);
            sender.onBackendKeyData(ctx, 123, 123);
            sender.onReadyForQuery(ctx, TransactionStatus.IDLE);
          }

          @Override
          public void onQuery(ChannelHandlerContext ctx, ByteBuf query) {
            Field f1 = new Field(bufWithText(ctx, "col1"), 0, 0, PgType.StandardTypes.VARCHAR, -1, Format.TEXT);

            Field f2 = new Field(bufWithText(ctx, "col2"), 0, 0, PgType.StandardTypes.INT4, -1, Format.TEXT);

            ArrayList<Field> fields = new ArrayList<>(2);
            fields.add(f1);
            fields.add(f2);
            sender.onRowDescription(ctx, fields);

            High<ChannelHandlerContext, Object, ByteBuf> high = new High.NettyBuilder<>(sender)
                .withSimpleConverter(new ValueConverter.StringToText())
                .withSimpleConverter(new ValueConverter.IntToText())
                .build();

            high.onDataRow(ctx, "hello", 0);
            high.onDataRow(ctx, "world", Integer.MAX_VALUE);
            high.onDataRow(ctx, "!", Integer.MIN_VALUE);

            sender.onCommandComplete(ctx, 3, CommandType.SELECT);
            sender.onReadyForQuery(ctx, TransactionStatus.IDLE);
          }

          private ByteBuf bufWithText(ChannelHandlerContext ctx, String text) {
            ByteBuf buf = ctx.alloc().buffer(text.length());
            buf.writeCharSequence(text, StandardCharsets.UTF_8);
            return buf;
          }
        };
      }
    };
    server.start();
  }
}
