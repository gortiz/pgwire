package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * The handler that frontend messages and process them by delegating into a {@link FrontendMessageListener}.
 *
 * This is the handler used by {@link PostgresServer} to process messages from the client.
 */
public class FrontendPostgresHandler extends AbstractPostgresHandler {

  private static final Logger LOGGER = LogManager.getLogger(FrontendPostgresHandler.class);
  private boolean expectingStartup = true;
  private final FrontendMessageListener<ChannelHandlerContext, ByteBuf, ByteBuf> listener;

  public FrontendPostgresHandler(FrontendMessageListener<ChannelHandlerContext, ByteBuf, ByteBuf> listener) {
    this.listener = listener;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in)
      throws Exception {
    if (expectingStartup) {
      in.skipBytes(4); // length
      int protocolVersion = in.readInt();
      if (isSSL(protocolVersion)) {
        listener.onSslRequest(ctx);
      } else if (isCancelRequest(protocolVersion)) {
        int processId = in.readInt();
        int secretKey = in.readInt();
        listener.onCancelRequest(ctx, processId, secretKey);
      } else if (isGSSENCRequest(protocolVersion)) {
        listener.onGSSENCRequest(ctx);
      } else if (isStartupMessage(protocolVersion)) {
        expectingStartup = false;
        LOGGER.debug("Read startup message");
        readStartUpMessage(ctx, protocolVersion, in);
      } else {
        ctx.close();
        throw new UnsupportedOperationException("Unknown startup message type " + protocolVersion);
      }
    } else {
      char id = (char) in.readByte();
      in.skipBytes(4); // length
      LOGGER.debug("Read regular message with id {}", id);

      switch (id) {
        case MessageConstants.BIND:
          decodeBind(ctx, in);
          break;
        case MessageConstants.PARSE:
          decodeParse(ctx, in);
          break;
        case MessageConstants.QUERY:
          decodeQuery(ctx, in);
          break;
        case MessageConstants.PASSWORD_MESSAGE:
          listener.onPasswordLikeMessage(ctx, in);
          break;
        case MessageConstants.DESCRIBE:
          decodeDescribe(ctx, in);
          break;
        case MessageConstants.EXECUTE:
          decodeExecute(ctx, in);
          break;
        case MessageConstants.FLUSH:
          listener.onFlush(ctx);
          break;
        case MessageConstants.SYNC:
          listener.onSync(ctx);
          break;
        case MessageConstants.CLOSE:
          decodeOnClose(ctx, in);
          break;
        case MessageConstants.TERMINATE:
          listener.onTerminate(ctx);
          break;
        case MessageConstants.COPY_FAIL:
          decodeCopyFail(ctx, in);
          break;
        case MessageConstants.FUNCTION_CALL:
          decodeFunctionCall(ctx, in);
          break;
        default: {
          ctx.close();
          throw new UnsupportedOperationException("Unknown message type " + id);
        }
      }
    }
  }

  private void decodeFunctionCall(ChannelHandlerContext ctx, ByteBuf in) {
    int funcOid = in.readInt();

    IntFunction<Format> formatDecoder = readFormatDecoder(in);

    int numParams = in.readUnsignedShort();
    List<ParameterBound<ByteBuf>> params = new ArrayList<>(numParams);
    for (int i = 0; i < numParams; i++) {
      Format format = formatDecoder.apply(i);
      ByteBuf bytes = readSliceFromLength(in);
      params.add(new ParameterBound<>(format, bytes));
    }
    Format resultFormat = Format.fromId(in.readUnsignedShort());

    listener.onFunctionCall(ctx, funcOid, params, resultFormat);
  }

  private void decodeCopyFail(ChannelHandlerContext ctx, ByteBuf slice) {
    ByteBuf cause = readNullEndedSlice(slice);
    listener.onCopyFail(ctx, cause);
  }

  private void decodeOnClose(ChannelHandlerContext ctx, ByteBuf slice) {
    TargetType targetType = TargetType.fromId(slice.readByte());
    ByteBuf name = readNullEndedSlice(slice);
    listener.onClose(ctx, targetType, name);
  }

  private void decodeExecute(ChannelHandlerContext ctx, ByteBuf slice) {
    ByteBuf name = readNullEndedSlice(slice);
    int rowLimit = slice.readInt();
    listener.onExecute(ctx, name, rowLimit);
  }

  private void decodeDescribe(ChannelHandlerContext ctx, ByteBuf slice) {
    TargetType targetType = TargetType.fromId(slice.readByte());
    ByteBuf name = readNullEndedSlice(slice);

    listener.onDescribe(ctx, targetType, name);
  }

  private void decodeQuery(ChannelHandlerContext ctx, ByteBuf slice) {
    ByteBuf query = readNullEndedSlice(slice);
    listener.onQuery(ctx, query);
  }

  private void decodeParse(ChannelHandlerContext ctx, ByteBuf slice) {
    ByteBuf name = readNullEndedSlice(slice);
    ByteBuf query = readNullEndedSlice(slice);
    int numParamOids = slice.readUnsignedShort();
    List<Integer> paramOids = new ArrayList<>(numParamOids);
    for (int i = 0; i < numParamOids; i++) {
      int paramOid = slice.readInt();
      paramOids.add(paramOid);
    }
    listener.onParse(ctx, name, query, paramOids);
  }

  private void decodeBind(ChannelHandlerContext ctx, ByteBuf slice) {
    ByteBuf portalName = readNullEndedSlice(slice);
    ByteBuf psName = readNullEndedSlice(slice);

    IntFunction<Format> formatDecoder = readFormatDecoder(slice);

    int numParams = slice.readUnsignedShort();
    List<ParameterBound<ByteBuf>> params = new ArrayList<>(numParams);
    for (int i = 0; i < numParams; i++) {
      Format format = formatDecoder.apply(i);
      ByteBuf bytes = readNullEndedSlice(slice);
      params.add(new ParameterBound<ByteBuf>(format, bytes));
    }

    int numResultFormats = slice.readUnsignedShort();
    List<Format> resultFormats = new ArrayList<>(numResultFormats);
    for (int i = 0; i < numResultFormats; i++) {
      int formatId = slice.readUnsignedShort();
      Format format = Format.fromId(formatId);
      resultFormats.add(format);
    }

    listener.onBind(ctx, portalName, psName, params, resultFormats);
  }

  private boolean isSSL(int protocolVersion) {
    return protocolVersion == 80877103;
  }

  private boolean isCancelRequest(int protocolVersion) {
    return protocolVersion == 80877102;
  }

  private boolean isGSSENCRequest(int protocolVersion) {
    return protocolVersion == 80877104;
  }

  private boolean isStartupMessage(int protocolVersion) {
    return protocolVersion == 196608;
  }

  IntFunction<Format> readFormatDecoder(ByteBuf buf) {
    int formatLength = buf.readUnsignedShort();
    switch (formatLength) {
      case 0: {
        return __ -> Format.TEXT;
      }
      case 1: {
        int defaultFormatId = buf.readUnsignedShort();
        Format defaultFormat = Format.fromId(defaultFormatId);
        return __ -> defaultFormat;
      }
      default: {
        Format[] formats = new Format[formatLength];
        for (int i = 0; i < formats.length; i++) {
          formats[i] = Format.fromId(buf.readUnsignedShort());
        }
        return idx -> formats[idx];
      }
    }
  }

  private void readStartUpMessage(ChannelHandlerContext ctx, int protocolVersion, ByteBuf buf) {
    Map<ByteBuf, ByteBuf> parameters = new HashMap<>();
    while (buf.isReadable()) {
      ByteBuf parameterName = readNullEndedSlice(buf);
      ByteBuf parameterValue = readNullEndedSlice(buf);
      parameters.put(parameterName, parameterValue);
    }
    listener.onStartup(ctx, protocolVersion, parameters);
  }
}
