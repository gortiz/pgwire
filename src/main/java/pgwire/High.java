package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class High<C, V, B> {

  private final BackendMessageListener<C, ?, B> listener;
  private ValueConverter<C, B>[] converters;

  public High(BackendMessageListener<C, ?, B> listener, ValueConverter<C, B>... converters) {
    this.listener = listener;
    this.converters = converters;
  }

  public void onDataRow(C ctx, V... cells) {
    ArrayList<B> convertedCells = new ArrayList<>(cells.length);
    for (int i = 0; i < cells.length; i++) {
      convertedCells.add(converters[i].convert(ctx, cells[i]));
    }
    listener.onDataRow(ctx, convertedCells);
  }

  public void onDataRow(C ctx, List<V> cells) {
    ArrayList<B> convertedCells = new ArrayList<>(cells.size());
    for (int i = 0; i < cells.size(); i++) {
      convertedCells.add(converters[i].convert(ctx, cells.get(i)));
    }
    listener.onDataRow(ctx, convertedCells);
  }

  public static class Builder<C, V, B> {
    private final BackendMessageListener<C, ?, B> listener;
    private final ArrayList<ValueConverter<C, B>> converters = new ArrayList<>();

    public Builder(BackendMessageListener<C, ?, B> listener) {
      this.listener = listener;
    }

    public Builder<C, V, B> withConverter(ValueConverter<C, B> converter) {
      converters.add(converter);
      return this;
    }

    public High<C, V, B> build() {
      return new High<>(listener, converters.toArray(i -> new ValueConverter[i]));
    }
  }

  public static class NettyBuilder<V> extends Builder<ChannelHandlerContext, V, ByteBuf> {
    public NettyBuilder(BackendMessageListener<ChannelHandlerContext, ?, ByteBuf> listener) {
      super(listener);
    }

    public NettyBuilder<V> withSimpleConverter(ValueConverter<ByteBufAllocator, ByteBuf> converter) {
      withConverter((ctx, input) -> converter.convert(ctx.alloc(), input));
      return this;
    }
  }
}
