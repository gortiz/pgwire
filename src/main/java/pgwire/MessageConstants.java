package pgwire;

public class MessageConstants {

  public final static char BIND = 'B';

  public static final char COMMAND_COMPLETE = 'C';
  public static final char DATA_ROW = 'D';
  public static final char ERROR_RESPONSE = 'E';
  public static final char PARAMETER_STATUS = 'S';
  public static final char READY_FOR_QUERY = 'Z';
  public static final char ROW_DESCRIPTION = 'T';
  public static final char PARSE = 'P';
  public static final char DESCRIBE = 'D';
  public static final char QUERY = 'Q';
  public static final char PASSWORD_MESSAGE = 'p';
  public static final char GSS_RESPONSE = 'p';
  public static final char EXECUTE = 'E';
  public static final char FLUSH = 'H';
  public static final char SYNC = 'S';
  public static final char CLOSE = 'C';
  public static final char TERMINATE = 'X';
  public static final char PARSE_COMPLETE = '1';
  public static final char BIND_COMPLETE = '2';
  public static final char CLOSE_COMPLETE = '3';
  public static final char NO_DATA = 'n';
  public static final char PARAMETER_DESCRIPTION = 't';
  public static final char EMPTY_QUERY_RESPONSE = 'I';
  public static final char PORTAL_SUSPENDED = 's';

  public static final char AUTHENTICATION_OK = 'R';
  public static final char BACKEND_KEY_DATA = 'K';
  public static final char NEGOTIATE_PROTOCOL_VERSION = 'v';
  public static final char AUTHENTICATION_PROTOCOL_REQUESTED = 'R';
  public static final char FUNCTION_CALL_RESPONSE = 'V';
  public static final char NOTICE_RESPONSE = 'N';
  public static final char NOTIFICATION_RESPONSE = 'A';
  public static final char COPY_DATA = 'd';
  public static final char COPY_DONE = 'c';
  public static final char COPY_IN_RESPONSE = 'G';
  public static final char COPY_OUT_RESPONSE = 'H';
  public static final char COPY_BOTH_RESPONSE = 'W';
  public static final char AUTHENTICATION_SASL = 'R';
  public static final char COPY_FAIL = 'f';
  public static final char FUNCTION_CALL = 'F';
}
