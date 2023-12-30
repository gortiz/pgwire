package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;


// This class may be useful to implement clients or man in the middle processes. But it is not useful to implement
// servers. Given the current focus is on the latter, this implementation is partial.
// TODO: Implement all methods and do not inherit BackendMessageListener.Abstract
public class FrontendMessageSender
    extends BackendMessageListener.Abstract<ChannelHandlerContext, ByteBuf, ByteBuf>
    implements BackendMessageListener<ChannelHandlerContext, ByteBuf, ByteBuf> {
  @Override
  public void onCopyDone(ChannelHandlerContext ctx) {
    SenderUtils.sendSimpleMsg(ctx, MessageConstants.COPY_DONE);
  }

  @Override
  public void onCopyData(ChannelHandlerContext ctx, ByteBuf bytes) {
    int length = 4 + 4 + SenderUtils.byteNLength(bytes);
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.COPY_DATA);
    buffer.writeInt(length);
    SenderUtils.writeByteN(buffer, bytes);

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onCopyInResponse(ChannelHandlerContext ctx, Format overallFormat, List<Format> columnFormats) {
    SenderUtils.copyAnyResponse(ctx, overallFormat, columnFormats, MessageConstants.COPY_IN_RESPONSE);
  }

  @Override
  public void onCopyOutResponse(ChannelHandlerContext ctx, Format overallFormat, List<Format> columnFormats) {
    SenderUtils.copyAnyResponse(ctx, overallFormat, columnFormats, MessageConstants.COPY_OUT_RESPONSE);
  }

  @Override
  public void onCopyBothResponse(ChannelHandlerContext ctx, Format overallFormat, List<Format> columnFormats) {
    SenderUtils.copyAnyResponse(ctx, overallFormat, columnFormats, MessageConstants.COPY_BOTH_RESPONSE);
  }
}
