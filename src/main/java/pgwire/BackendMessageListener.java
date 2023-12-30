package pgwire;

import java.util.List;
import java.util.Map;

// TODO: Add proper authentication methods

public interface BackendMessageListener<C, S, B> {

  void onBindComplete(C ctx);

  void onCommandComplete(C ctx, int affectedRows, CommandType type);

  void onNoData(C ctx);

  void onParameterDescription(C ctx, List<PgType> data);

  void onDataRow(C ctx, List<B> cells);

  void onParseComplete(C ctx);

  /**
   * TODO: Change Character with our own class and provide instances for the default protocol error fields
   * See <a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">postgres doc</a> to know
   * defined error field types.
   */
  void onErrorResponse(C ctx, Map<Character, S> fields);

  void onCloseComplete(C ctx);

  void onEmptyQueryResponse(C ctx);

  /**
   * @param data can be null
   */
  void onFunctionCallResponse(C ctx, S data);

  /**
   * TODO: Change Character with our own class and provide instances for the default protocol error fields
   * See <a href="https://www.postgresql.org/docs/current/protocol-error-fields.html">postgres doc</a> to know
   * defined error field types.
   */
  void onNoticeResponse(C ctx, Map<Character, S> fields);

  /**
   *
   * @param processId The process ID of the notifying backend process.
   * @param channel The name of the channel that the notify has been raised on.
   * @param payload The “payload” string passed from the notifying process.
   */
  void onNotificationResponse(C ctx, int processId, S channel, S payload);

  /**
   * @param name The name of the run-time parameter being reported.
   * @param value The current value of the parameter.
   */
  void onParameterStatus(C ctx, S name, S value);

  void onPortalSuspended(C ctx);

  void onReadyForQuery(C ctx, TransactionStatus status);

  void onRowDescription(C ctx, List<Field> fields);

  /**
   * @param bytes Data that forms part of a COPY data stream. Messages sent from the backend will always correspond
   *              to single data rows.
   */
  void onCopyData(C ctx, B bytes);

  void onCopyDone(C ctx);

  /**
   * @param columnFormats at most {@code 2^16} columns can be sent
   */
  void onCopyInResponse(C ctx, Format overallFormat, List<Format> columnFormats);

  /**
   * @param columnFormats at most {@code 2^16} columns can be sent
   */
  void onCopyOutResponse(C ctx, Format overallFormat, List<Format> columnFormats);

  /**
   * @param columnFormats at most {@code 2^16} columns can be sent
   */
  void onCopyBothResponse(C ctx, Format overallFormat, List<Format> columnFormats);

  /**
   * @param processId The process ID of this backend.
   * @param secretKey The secret key of this backend.
   */
  void onBackendKeyData(C ctx, int processId, int secretKey);

  void onAuthenticationOk(C ctx);

  void onAuthenticationKerberosV5(C ctx);

  void onAuthenticationCleartextPassword(C ctx);

  /**
   * The frontend must now send a PasswordMessage containing the password (with user name) encrypted via MD5, then
   * encrypted again using the 4-byte random salt specified in the AuthenticationMD5Password message. If this is the
   * correct password, the server responds with an AuthenticationOk, otherwise it responds with an ErrorResponse. The
   * actual PasswordMessage can be computed in SQL as concat('md5', md5(concat(md5(concat(password, username)),
   * random-salt))). (Keep in mind the md5() function returns its result as a hex string.)
   * @param salt The salt to use when encrypting the password.
   */
  void onAuthenticationMD5Password(C ctx, int salt);

  void onAuthenticationGSS(C ctx);

  void onAuthenticationGSSContinue(C ctx, B data);

  void onAuthenticationSSPI(C ctx);

  /**
   * @param authMechNames The message body is a list of SASL authentication mechanisms, in the server's order of
   *                      preference
   */
  void onAuthenticationSASL(C ctx, List<S> authMechNames);

  /**
   * @param data SASL data, specific to the SASL mechanism being used.
   */
  void onAuthenticationSASLContinue(C ctx, B data);

  /**
   * @param data SASL outcome "additional data", specific to the SASL mechanism being used.
   */
  void onAuthenticationSASLFinal(C ctx, B data);

  /**
   * @param protocolLatest Newest minor protocol version supported by the server for the major protocol version
   *                       requested by the client.
   * @param unrecognizedProtocolOptions protocol options not recognized by the server.
   */
  void onNegotiateProtocolVersion(C ctx, int protocolLatest, List<S> unrecognizedProtocolOptions);

  public static class Abstract<C, S, B> implements BackendMessageListener<C, S, B> {
    @Override
    public void onBindComplete(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onCommandComplete(C ctx, int affectedRows, CommandType type) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onNoData(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onParameterDescription(C ctx, List<PgType> data) {
      throw new UnsupportedOperationException();
    }

    /**
     * @param cells Cells to send. At most {@code 2^16} cells can be sent.
     */
    @Override
    public void onDataRow(C ctx, List<B> cells) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onParseComplete(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onErrorResponse(C ctx, Map<Character, S> fields) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onCloseComplete(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onEmptyQueryResponse(C ctx) {
      throw new UnsupportedOperationException();
    }

    /**
     * @param data The value of the function result, in the format indicated by the associated format code.
     */
    @Override
    public void onFunctionCallResponse(C ctx, S data) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onNoticeResponse(C ctx, Map<Character, S> fields) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onNotificationResponse(C ctx, int processId, S channel, S payload) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onParameterStatus(C ctx, S name, S value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onPortalSuspended(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onReadyForQuery(C ctx, TransactionStatus status) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onRowDescription(C ctx, List<Field> fields) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onCopyData(C ctx, B bytes) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onCopyDone(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onCopyInResponse(C ctx, Format overallFormat, List<Format> columnFormats) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onCopyOutResponse(C ctx, Format overallFormat, List<Format> columnFormats) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onCopyBothResponse(C ctx, Format overallFormat, List<Format> columnFormats) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onAuthenticationKerberosV5(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onAuthenticationCleartextPassword(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onAuthenticationGSS(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onAuthenticationGSSContinue(C ctx, B data) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onAuthenticationSSPI(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onAuthenticationSASL(C ctx, List<S> authMechNames) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onAuthenticationSASLContinue(C ctx, B data) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onAuthenticationSASLFinal(C ctx, B data) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onBackendKeyData(C ctx, int processId, int secretKey) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onAuthenticationOk(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onAuthenticationMD5Password(C ctx, int salt) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onNegotiateProtocolVersion(C ctx, int protocolLatest, List<S> unrecognizedProtocolOptions) {
      throw new UnsupportedOperationException();
    }
  }
}
