package pgwire;

import java.util.List;
import java.util.Map;

public interface FrontendMessageListener<C, S, B> {

  /**
   * Special parameters:
   * <ol>
   *   <li>user: The database user name to connect as. Required; there is no default.</li>
   *   <li>database: The database to connect to. Defaults to the user name.</li>
   *   <li>options: Command-line arguments for the backend.
   *   (This is deprecated in favor of setting individual run-time parameters.)</li>
   *   <li>replication: Used to connect in streaming replication mode, where a small set of replication commands can be
   *   issued instead of SQL statements. Value can be true, false, or database, and the default is false.
   *   See <a href="https://www.postgresql.org/docs/current/protocol-replication.html">Section 55.4</a> for details.</li>
   * </ol>
   * @param parameters
   */
  void onStartup(C ctx, int version, Map<S, S> parameters);

  void onDescribe(C ctx, TargetType type, S name);

  /**
   * @param name The name of the prepared statement or portal to close
   *             (an empty string selects the unnamed prepared statement or portal).
   */
  void onClose(C ctx, TargetType type, S name);

  void onSslRequest(C ctx);

  void onBind(C ctx, S portalName, S preparedStatementName, List<ParameterBound<S>> params, List<Format> resultFormats);

  void onCancelRequest(C ctx, int processId, int secretKey);

  /**
   * @param bytes Data that forms part of a COPY data stream. Contrary to messages sent from the backend,
   *              messages sent by frontends might divide the data stream arbitrarily.
   */
  void onCopyData(C ctx, B bytes);

  void onCopyDone(C ctx);

  void onCopyFail(C ctx, S message);

  void onExecute(C ctx, S name, int rowLimit);

  void onFlush(C ctx);

  void onFunctionCall(C ctx, int funcOid, List<ParameterBound<S>> parameters,  Format resultFormat);

  /**
   * @param query the query to execute. It may contain several queries separated by semi-colon {@code ;}.
   */
  void onQuery(C ctx, S query);

  void onSync(C ctx);

  void onTerminate(C ctx);

  /**
   * Request to create a prepared statement.
   *
   * Contrary to {@link #onQuery(Object, Object)}, the parsed query must be a single expression and it may contain
   * parameters (named as $1, $2, $3, etc).
   *
   * A number of parameters can optionally be sent. Note that this is not an indication of the number of parameters
   * that might appear in the query string, only the number that the frontend wants to prespecify types for.
   * Placing a zero in one of the parameters is equivalent to leaving the type unspecified.
   *
   * @param name The name of the destination prepared statement (an empty string selects the unnamed prepared statement).
   * @param query The query string to be parsed. Must be a single query and may contain variables like $1, $2, etc.
   * @param paramOids The params to prespecify. Not all parameters in query must be prespecified.
   */
  void onParse(C ctx, S name, S query, List<Integer> paramOids);

  /**
   * Messages PasswordMessage, SASLInitialResponse, SASLResponse and GSSResponse can only be distinguished by the
   * context. Use {@link PasswordLikeMessageUtil} to decode the bytes as the specific message required by the context,
   * although it is only used in the context of a SASL authentication.
   */
  void onPasswordLikeMessage(C ctx, B bytes);

  void onGSSENCRequest(C ctx);

  /**
   * @param data GSSAPI/SSPI specific message data.
   */
  void onGSSResponse(C ctx, B data);

  /**
   * @param saslName Name of the SASL authentication mechanism that the client selected.
   * @param initialResponse SASL mechanism specific "Initial Response", which may be empty.
   */
  void onSASLInitialResponse(C ctx, S saslName, B initialResponse);

  /**
   * @param data SASL mechanism specific message data.
   */
  void onSASLResponse(C ctx, B data);

  abstract class Abstract<C, S, B> implements FrontendMessageListener<C, S, B> {
    @Override
    public void onStartup(C ctx, int version, Map<S, S> parameters) {
      throw new UnsupportedOperationException();
    }

    /**
     * @param name The name of the prepared statement or portal to describe
     *             (an empty string selects the unnamed prepared statement or portal).
     */
    @Override
    public void onDescribe(C ctx, TargetType type, S name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onClose(C ctx, TargetType type, S name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onSslRequest(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onBind(C ctx, S portalName, S preparedStatementName, List<ParameterBound<S>> params,
        List<Format> resultFormats) {
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
    public void onCopyFail(C ctx, S message) {
      throw new UnsupportedOperationException();
    }

    /**
     * @param name The name of the portal to execute (an empty string selects the unnamed portal).
     * @param rowLimit Maximum number of rows to return, if portal contains a query that returns rows
     *                 (ignored otherwise). Zero denotes “no limit”.
     */
    @Override
    public void onExecute(C ctx, S name, int rowLimit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onFlush(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onQuery(C ctx, S query) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onSync(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onTerminate(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onParse(C ctx, S name, S query, List<Integer> paramOids) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onCancelRequest(C ctx, int processId, int secretKey) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onFunctionCall(C ctx, int funcOid, List<ParameterBound<S>> parameters, Format resultFormat) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onPasswordLikeMessage(C ctx, B bytes) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onGSSENCRequest(C ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onGSSResponse(C ctx, B data) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onSASLInitialResponse(C ctx, S saslName, B initialResponse) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void onSASLResponse(C ctx, B data) {
      throw new UnsupportedOperationException();
    }
  }
}
