package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static pgwire.SenderUtils.*;


/**
 * This is the class that sends messages to the client, hiding the lower level details of the protocol like the order
 * between fields and most of the magic numbers used in the protocol.
 *
 * By using this class, servers can focus on what they want to send and not how to send it.
 * This class is still pretty low level and other higher level apis (like {@link High} or even a BackendMessageListener
 * that uses Strings and byte[]) could be created on top of this.
 */
public class BackendMessageSender implements BackendMessageListener<ChannelHandlerContext, ByteBuf, ByteBuf> {

  @Override
  public void onBindComplete(ChannelHandlerContext ctx) {
    int length = 5;
    ByteBuf byteBuf = ctx.alloc().buffer(length)
        .writeByte(MessageConstants.BIND_COMPLETE)
        .writeInt(length);
    ctx.write(byteBuf);
  }

  @Override
  public void onCommandComplete(ChannelHandlerContext ctx, int affectedRows, CommandType type) {
    StringBuilder sb = new StringBuilder(128);
    sb.append(type.name().toUpperCase(Locale.US));
    if (type == CommandType.INSERT) {
      sb.append(" 0");
    }
    sb.append(' ').append(affectedRows);

    int length = sb.length() + 1 + 4; // sb does only contain ascii characters
    ByteBuf byteBuf = ctx.alloc()
        .buffer(1 + length)
        .writeByte(MessageConstants.COMMAND_COMPLETE)
        .writeInt(length);
    writeCStr(byteBuf, sb);
    assert byteBuf.writerIndex() == length + 1;

    ctx.write(byteBuf);
  }

  @Override
  public void onNoData(ChannelHandlerContext ctx) {
    sendSimpleMsg(ctx, MessageConstants.NO_DATA);
  }

  @Override
  public void onParameterDescription(ChannelHandlerContext ctx, List<PgType> parameters) {
    int length = 4 + 2 + parameters.size();
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.PARAMETER_DESCRIPTION);
    buffer.writeByte(length);

    if (parameters.size() > Short.MAX_VALUE) {
      buffer.release();
      throw new IllegalArgumentException("Too many parameters. Trying to send " + parameters.size()
          + " when max is " + Short.MAX_VALUE);
    }
    writeUnsignedShort(buffer, parameters.size());
    for (PgType parameter : parameters) {
      buffer.writeInt(parameter.getOid());
    }

    ctx.write(buffer);
  }

  @Override
  public void onDataRow(ChannelHandlerContext ctx, List<ByteBuf> cells) {
    int length = 4 + 2 + cells.stream().mapToInt(ByteBuf::readableBytes).sum() + cells.size() * 4;
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.DATA_ROW);
    buffer.writeInt(length);

    writeUnsignedShort(buffer, cells.size());
    for (ByteBuf cell : cells) {
      writeByteN(buffer, cell);
    }

    ctx.write(buffer);
  }

  @Override
  public void onParseComplete(ChannelHandlerContext ctx) {
    sendSimpleMsg(ctx, MessageConstants.PARSE_COMPLETE);
  }

  @Override
  public void onErrorResponse(ChannelHandlerContext ctx, Map<Character, ByteBuf> fields) {
    onErrorLikeResponse(ctx, fields, MessageConstants.ERROR_RESPONSE);
  }

  @Override
  public void onCloseComplete(ChannelHandlerContext ctx) {
    sendSimpleMsg(ctx, MessageConstants.CLOSE_COMPLETE);
  }

  @Override
  public void onEmptyQueryResponse(ChannelHandlerContext ctx) {
    sendSimpleMsg(ctx, MessageConstants.EMPTY_QUERY_RESPONSE);
  }

  @Override
  public void onFunctionCallResponse(ChannelHandlerContext ctx, ByteBuf data) {
    int length = 4 + 4 + (data != null ? data.readableBytes() : 0);
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.FUNCTION_CALL_RESPONSE);
    buffer.writeInt(length);
    writeByteN(buffer, data);

    ctx.write(buffer);
  }

  @Override
  public void onNoticeResponse(ChannelHandlerContext ctx, Map<Character, ByteBuf> fields) {
    onErrorLikeResponse(ctx, fields, MessageConstants.NOTICE_RESPONSE);
  }

  @Override
  public void onNotificationResponse(ChannelHandlerContext ctx, int processId, ByteBuf channel,
      ByteBuf payload) {
    int length = 4 + 4 + channel.readableBytes() + 1 + payload.readableBytes() + 1;
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.NOTIFICATION_RESPONSE);
    buffer.writeInt(length);
    buffer.writeInt(processId);
    writeCStr(buffer, channel);
    writeCStr(buffer, payload);

    ctx.write(buffer);
  }

  @Override
  public void onParameterStatus(ChannelHandlerContext ctx, ByteBuf name, ByteBuf value) {
    int length = 4 + name.readableBytes() + 1 + value.readableBytes() + 1;
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.PARAMETER_STATUS);
    buffer.writeInt(length);
    writeCStr(buffer, name);
    writeCStr(buffer, value);

    ctx.write(buffer);
  }

  @Override
  public void onPortalSuspended(ChannelHandlerContext ctx) {
    sendSimpleMsg(ctx, MessageConstants.PORTAL_SUSPENDED);
  }

  @Override
  public void onReadyForQuery(ChannelHandlerContext ctx, TransactionStatus status) {
    ByteBuf buffer = ctx.alloc().buffer(1 + 4 + 1);
    buffer.writeByte(MessageConstants.READY_FOR_QUERY);
    buffer.writeInt(5);
    buffer.writeByte(status.getId());

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onRowDescription(ChannelHandlerContext ctx, List<Field> fields) {
    int fieldSize = 4 + 2 + 4 + 2 + 4 + 2;
    int length = 4 + 2 + fields.size() * fieldSize + fields.stream().mapToInt(f -> f.getName().readableBytes()).sum();

    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.ROW_DESCRIPTION);
    buffer.writeInt(-1);
    
    writeUnsignedShort(buffer, fields.size());

    for (Field field : fields) {
      writeCStr(buffer, field.getName());
      buffer.writeInt(field.getTableOid());
      writeUnsignedShort(buffer, field.getColumnIdx());
      buffer.writeInt(field.getPgType().getOid());
      writeUnsignedShort(buffer, field.getPgType().getByteLength());
      buffer.writeInt(field.getTypeModifier());
      writeUnsignedShort(buffer, field.getFormat().getId());
    }

    buffer.setInt(1, buffer.readableBytes() - 1);

    ctx.write(buffer);
  }

  @Override
  public void onCopyData(ChannelHandlerContext ctx, ByteBuf bytes) {
    int length = 4 + 4 + byteNLength(bytes);
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.COPY_DATA);
    buffer.writeInt(length);
    writeByteN(buffer, bytes);

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onCopyDone(ChannelHandlerContext ctx) {
    sendSimpleMsg(ctx, MessageConstants.COPY_DONE);
  }

  @Override
  public void onCopyInResponse(ChannelHandlerContext ctx, Format overallFormat, List<Format> columnFormats) {
    copyAnyResponse(ctx, overallFormat, columnFormats, MessageConstants.COPY_IN_RESPONSE);
  }

  @Override
  public void onCopyOutResponse(ChannelHandlerContext ctx, Format overallFormat, List<Format> columnFormats) {
    copyAnyResponse(ctx, overallFormat, columnFormats, MessageConstants.COPY_OUT_RESPONSE);
  }

  @Override
  public void onCopyBothResponse(ChannelHandlerContext ctx, Format overallFormat, List<Format> columnFormats) {
    copyAnyResponse(ctx, overallFormat, columnFormats, MessageConstants.COPY_BOTH_RESPONSE);
  }

  @Override
  public void onAuthenticationSASL(ChannelHandlerContext ctx, List<ByteBuf> authMechNames) {
    int length = 4 + 4 + authMechNames.stream().mapToInt(SenderUtils::cStrLength).sum() + 1;
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.AUTHENTICATION_SASL);
    buffer.writeInt(length);
    buffer.writeInt(10);
    for (ByteBuf authMechName : authMechNames) {
      writeCStr(buffer, authMechName);
    }
    buffer.writeByte(0);

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onAuthenticationSASLContinue(ChannelHandlerContext ctx, ByteBuf data) {
    int length = 4 + 4 + byteNLength(data);
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.AUTHENTICATION_SASL);
    buffer.writeInt(length);
    buffer.writeInt(11);

    writeByteN(buffer, data);

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onAuthenticationSASLFinal(ChannelHandlerContext ctx, ByteBuf data) {
    int length = 4 + 4 + byteNLength(data);
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.AUTHENTICATION_SASL);
    buffer.writeInt(length);
    buffer.writeInt(12);

    writeByteN(buffer, data);

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onBackendKeyData(ChannelHandlerContext ctx, int processId, int secretKey) {
    ByteBuf buffer = ctx.alloc().buffer(17);
    buffer.writeByte(MessageConstants.BACKEND_KEY_DATA);
    buffer.writeInt(12);
    buffer.writeInt(processId);
    buffer.writeInt(secretKey);

    ctx.write(buffer);
  }

  @Override
  public void onAuthenticationOk(ChannelHandlerContext ctx) {
    ByteBuf buffer = ctx.alloc().buffer(9);
    buffer.writeByte(MessageConstants.AUTHENTICATION_OK);
    buffer.writeInt(8);
    buffer.writeInt(0); // Specifies that the authentication was successful.

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onAuthenticationMD5Password(ChannelHandlerContext ctx, int salt) {
    ByteBuf buffer = ctx.alloc().buffer(13);
    buffer.writeByte(MessageConstants.AUTHENTICATION_PROTOCOL_REQUESTED);
    buffer.writeInt(12);
    buffer.writeInt(5); // Specifies that an MD5-encrypted password is required.
    buffer.writeInt(salt);

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onAuthenticationKerberosV5(ChannelHandlerContext ctx) {
    ByteBuf buffer = ctx.alloc().buffer(9);
    buffer.writeByte(MessageConstants.AUTHENTICATION_PROTOCOL_REQUESTED);
    buffer.writeInt(8);
    buffer.writeInt(2); // Specifies that Kerberos V5 authentication is required.

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onAuthenticationCleartextPassword(ChannelHandlerContext ctx) {
    ByteBuf buffer = ctx.alloc().buffer(9);
    buffer.writeByte(MessageConstants.AUTHENTICATION_PROTOCOL_REQUESTED);
    buffer.writeInt(8);
    buffer.writeInt(3); // Specifies that a clear-text password is required.

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onAuthenticationGSS(ChannelHandlerContext ctx) {
    ByteBuf buffer = ctx.alloc().buffer(9);
    buffer.writeByte(MessageConstants.AUTHENTICATION_PROTOCOL_REQUESTED);
    buffer.writeInt(8);
    buffer.writeInt(7); // Specifies that GSSAPI authentication is required.

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onAuthenticationGSSContinue(ChannelHandlerContext ctx, ByteBuf bytes) {
    int length = 4 + 4 + bytes.readableBytes() + 1;
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.AUTHENTICATION_PROTOCOL_REQUESTED);
    buffer.writeInt(length);
    buffer.writeInt(8); // Specifies that this message contains GSSAPI or SSPI data.

    writeByteN(buffer, bytes);

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onAuthenticationSSPI(ChannelHandlerContext ctx) {
    ByteBuf buffer = ctx.alloc().buffer(9);
    buffer.writeByte(MessageConstants.AUTHENTICATION_PROTOCOL_REQUESTED);
    buffer.writeInt(8);
    buffer.writeInt(9); // Specifies that SSPI authentication is required.

    ctx.writeAndFlush(buffer);
  }

  @Override
  public void onNegotiateProtocolVersion(ChannelHandlerContext ctx, int protocolLatest,
      List<ByteBuf> unrecognizedProtocolOptions) {
    int length = 4 + 4 + unrecognizedProtocolOptions.stream().mapToInt(ByteBuf::readableBytes).sum()
        + unrecognizedProtocolOptions.size();
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(MessageConstants.NEGOTIATE_PROTOCOL_VERSION);
    buffer.writeInt(length);
    buffer.writeInt(unrecognizedProtocolOptions.size());
    for (ByteBuf unrecognized : unrecognizedProtocolOptions) {
      writeCStr(buffer, unrecognized);
    }
  }

  /**
   * This is used by both {@link #onErrorResponse(ChannelHandlerContext, Map)} and
   * {@link #onNoticeResponse(ChannelHandlerContext, Map)}
   */
  private void onErrorLikeResponse(ChannelHandlerContext ctx, Map<Character, ByteBuf> fields, char id) {
    int length = 4 + //length itself
        3 * fields.size() + // 2 bytes for the char and 1 for the null termination
        fields.values().stream().mapToInt(ByteBuf::readableBytes).sum(); // the byte content
    ByteBuf buffer = ctx.alloc().buffer(1 + length);
    buffer.writeByte(id);
    buffer.writeInt(length);

    for (Map.Entry<Character, ByteBuf> entry : fields.entrySet()) {
      buffer.writeByte(entry.getKey());
      writeCStr(buffer, entry.getValue());
    }
    buffer.writeByte(0); // marks the end of the message
  }
}
