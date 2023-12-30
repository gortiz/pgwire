package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.nio.charset.StandardCharsets;


public interface ValueConverter<C, O> {

  O convert(C ctx, Object input);

  class IntToText implements ValueConverter<ByteBufAllocator, ByteBuf> {
    @Override
    public ByteBuf convert(ByteBufAllocator ctx, Object input) {
      Integer i = (Integer) input;
      StringBuilder sb = new StringBuilder();
      sb.append(i);
      ByteBuf buffer = ctx.buffer(sb.length());
      buffer.writeCharSequence(sb, StandardCharsets.UTF_8);
      return buffer;
    }
  }

  class StringToText implements ValueConverter<ByteBufAllocator, ByteBuf> {
    @Override
    public ByteBuf convert(ByteBufAllocator ctx, Object input) {
      String string = (String) input;
      ByteBuf buffer = ctx.buffer(string.length());
      buffer.writeCharSequence(string, StandardCharsets.UTF_8);
      return buffer;
    }
  }
}
