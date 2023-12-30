package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class BackendPosgresHandler extends AbstractPostgresHandler {

  private static final Logger LOGGER = LogManager.getLogger(BackendPosgresHandler.class);
  private final IntFunction<PgType> typeLibrary;
  private final BackendMessageListener<ChannelHandlerContext, ByteBuf, ByteBuf> listener;

  public BackendPosgresHandler(IntFunction<PgType> typeLibrary,
      BackendMessageListener<ChannelHandlerContext, ByteBuf, ByteBuf> listener) {
    this.typeLibrary = typeLibrary;
    this.listener = listener;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buf)
      throws Exception {
    char id = (char) buf.readByte();
    int length = buf.readInt();
    LOGGER.debug("Read regular message with id {}", id);

    switch (id) {
      case MessageConstants.COMMAND_COMPLETE:
        decodeCommandComplete(ctx, buf);
        break;
      case MessageConstants.DATA_ROW:
        decodeDataRow(ctx, buf);
        break;
      case MessageConstants.ERROR_RESPONSE:
        decodeErrorResponse(ctx, buf);
        break;
      case MessageConstants.PARAMETER_STATUS:
        decodeParameterStatus(ctx, buf);
        break;
      case MessageConstants.READY_FOR_QUERY:
        decodeReadyForQuery(ctx, buf);
        break;
      case MessageConstants.ROW_DESCRIPTION:
        decodeRowDescription(ctx, buf);
        break;
      default: {
        throw new UnsupportedOperationException("Unknown message type " + id);
      }
    }
  }

  private void decodeRowDescription(ChannelHandlerContext ctx, ByteBuf slice) {
    int numFields = slice.readUnsignedShort();
    ArrayList<Field> fields = new ArrayList<>(numFields);
    for (int i = 0; i < numFields; i++) {
      ByteBuf fieldName = readNullEndedSlice(slice);
      int tableOid = slice.readInt();
      int columnIdx = slice.readUnsignedShort();

      int typeOid = slice.readInt();
      PgType type = typeLibrary.apply(typeOid);

      int typeModifier = slice.readInt();

      Format format = Format.fromId(slice.readUnsignedShort());
      fields.add(new Field(fieldName, tableOid, columnIdx, type, typeModifier, format));
    }
    listener.onRowDescription(ctx, fields);
  }

  private void decodeReadyForQuery(ChannelHandlerContext ctx, ByteBuf slice) {
    TransactionStatus status = TransactionStatus.fromValue(slice.readByte());
    listener.onReadyForQuery(ctx, status);
  }

  private void decodeParameterStatus(ChannelHandlerContext ctx, ByteBuf slice) {
    ByteBuf name = readNullEndedSlice(slice);
    ByteBuf value = readNullEndedSlice(slice);
    listener.onParameterStatus(ctx, name, value);
  }

  private void decodeErrorResponse(ChannelHandlerContext ctx, ByteBuf slice) {
    HashMap<Character, ByteBuf> data = new HashMap<>();
    while (slice.isReadable() && slice.getByte(slice.readerIndex()) != 0) {
      char entry = (char) slice.readUnsignedShort();
      ByteBuf value = readNullEndedSlice(slice);
      data.put(entry, value);
    }
    listener.onErrorResponse(ctx, data);
  }

  private void decodeDataRow(ChannelHandlerContext ctx, ByteBuf slice) {
    int numValues = slice.readUnsignedShort();
    List<ByteBuf> values = new ArrayList<>(numValues);
    for (int i = 0; i < numValues; i++) {
      int valueLength = slice.readInt();
      ByteBuf value = slice.readSlice(valueLength);
      values.add(value);
    }
    listener.onDataRow(ctx, values);
  }

  private void decodeCommandComplete(ChannelHandlerContext ctx, ByteBuf slice) {
    String cmd = readNullEndedString(slice);
    int affectedRows;
    CommandType type;
    String[] splits = cmd.split(" ");
    if (splits.length == 0) {
      throw new IllegalArgumentException("Unexpected text '" + cmd + "'");
    }
    try {
      type = CommandType.valueOf(splits[0].toUpperCase(Locale.US));
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Unexpected text '" + cmd + "'", ex);
    }
    if (splits.length == 1) {
      throw new IllegalArgumentException("Unexpected text '" + cmd + "'");
    }
    affectedRows = Integer.parseInt(splits[splits.length - 1]);
    listener.onCommandComplete(ctx, affectedRows, type);
  }
}
