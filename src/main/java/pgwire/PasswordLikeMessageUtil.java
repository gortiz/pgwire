package pgwire;

import io.netty.buffer.ByteBuf;
import java.util.function.BiFunction;
import java.util.function.Function;


public class PasswordLikeMessageUtil {

  /**
   * @param buffer the buffer containing the data, where the 'p' and the length has been already read.
   *               {@link ByteBuf#readerIndex()} must be the first byte after length field and
   *               {@link ByteBuf#readableBytes()} must be exactly the value of length. This buffer will be consumed.
   * @param consumeData A function that receives the GSSAPI/SSPI specific message data.
   * @return the result returned by the function given as argument.
   * @param <R> the result type of the visitor.
   */
  public static <R> R treatAsGSSResponse(ByteBuf buffer, Function<ByteBuf, R> consumeData) {
    return consumeData.apply(buffer);
  }

  /**
   * @param buffer the buffer containing the data, where the 'p' and the length has been already read.
   *               {@link ByteBuf#readerIndex()} must be the first byte after length field and
   *               {@link ByteBuf#readableBytes()} must be exactly the value of length. This buffer will be consumed.
   * @param consumePassword A function that receives the password (encrypted, if requested).
   * @return the result returned by the function given as argument.
   * @param <R> the result type of the visitor.
   */
  public static <R> R treatAsPasswordMessage(ByteBuf buffer, Function<ByteBuf, R> consumePassword) {
    return consumePassword.apply(buffer);
  }

  /**
   * @param buffer the buffer containing the data, where the 'p' and the length has been already read.
   *               {@link ByteBuf#readerIndex()} must be the first byte after length field and
   *               {@link ByteBuf#readableBytes()} must be exactly the value of length. This buffer will be consumed.
   * @param consumeData A function that that receives SASL mechanism specific message data
   * @return the result returned by the function given as argument.
   * @param <R> the result type of the visitor.
   */
  public static <R> R treatAsSASLInitialResponse(ByteBuf buffer, Function<ByteBuf, R> consumeData) {
    return consumeData.apply(buffer);
  }

  /**
   * @param buffer the buffer containing the data, where the 'p' and the length has been already read.
   *               {@link ByteBuf#readerIndex()} must be the first byte after length field and
   *               {@link ByteBuf#readableBytes()} must be exactly the value of length. This buffer will be consumed.
   * @param onSASLInitialResponse A function that that receives the name of the SASL authentication mechanism that the
   *                              client selected as first argument and the initial Response (which may be null) as
   *                              the second.
   * @return the result returned by the function given as argument.
   * @param <R> the result type of the visitor.
   */
  public static <R> R treatAsSASLInitialResponse(ByteBuf buffer, BiFunction<String, ByteBuf, R> onSASLInitialResponse) {
    String method = AbstractPostgresHandler.readNullEndedString(buffer);
    ByteBuf data = AbstractPostgresHandler.readSliceFromLength(buffer);
    return onSASLInitialResponse.apply(method, data);
  }
}
