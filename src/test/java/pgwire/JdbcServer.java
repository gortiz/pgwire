package pgwire;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.pinot.client.PinotDriver;


public class JdbcServer extends PostgresServer {

  private final DataSource dataSource;

  public static void main(String[] args)
      throws InterruptedException {

    DataSource dataSource = new PinotDataSource("jdbc:pinot://localhost:9000");
    JdbcServer server = new JdbcServer("localhost", 5432, dataSource);
    server.start();
  }

  public JdbcServer(String host, int port, DataSource dataSource) {
    super(host, port);
    this.dataSource = dataSource;
  }

  @Override
  protected FrontendMessageListener<ChannelHandlerContext, ByteBuf, ByteBuf> createMessageListener() {
    BackendMessageSender sender = new BackendMessageSender();

    return new FrontendMessageListener.Abstract<>() {
      private Connection connection;

      @Override
      public void onSslRequest(ChannelHandlerContext ctx) {
        ByteBuf byteBuf = ctx.alloc().buffer(1).writeByte('N');
        ctx.writeAndFlush(byteBuf);
      }

      @Override
      public void onStartup(ChannelHandlerContext ctx, int version, Map<ByteBuf, ByteBuf> parameters) {
        try {
          connection = dataSource.getConnection();
          sender.onAuthenticationOk(ctx);
          sender.onBackendKeyData(ctx, 123, 123);
          sender.onReadyForQuery(ctx, TransactionStatus.IDLE);
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onTerminate(ChannelHandlerContext ctx) {
        try {
          connection.close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public void onQuery(ChannelHandlerContext ctx, ByteBuf query) {
        String queryStr = query.toString(StandardCharsets.UTF_8);
        try {
          ResultSet resultSet = connection.createStatement().executeQuery(queryStr);
          sendMetadata(ctx, resultSet);
          ValueConverter<ByteBufAllocator, ByteBuf>[] converters = createConverters(resultSet);

          ArrayList<ByteBuf> cells = new ArrayList<>(resultSet.getMetaData().getColumnCount());
          while (resultSet.next()) {
            prepareRow(ctx, resultSet, converters, cells);
            sender.onDataRow(ctx, cells);
          }

        } catch (SQLException e) {
          throw new RuntimeException(e);
        }

        sender.onCommandComplete(ctx, 3, CommandType.SELECT);
        sender.onReadyForQuery(ctx, TransactionStatus.IDLE);
      }

      private void sendMetadata(ChannelHandlerContext ctx, ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        ArrayList<Field> fields = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) { // important: jdbc indexes start in 1, not 0
          String columnName = metaData.getColumnName(i);
          PgType pgType = getPgType(metaData.getColumnType(i));
          fields.add(new Field(bufWithText(ctx, columnName), 0, 0, pgType, -1, Format.TEXT));
        }
        sender.onRowDescription(ctx, fields);
      }

      private void prepareRow(ChannelHandlerContext ctx, ResultSet resultSet,
          ValueConverter<ByteBufAllocator, ByteBuf>[] converters, List<ByteBuf> row)
          throws SQLException {
        row.clear();
        for (int i = 0; i < converters.length; i++) {
          row.add(converters[i].convert(ctx.alloc(), resultSet.getObject(i + 1)));
        }
      }

      private PgType getPgType(int columnType) {
        switch (columnType) {
          case Types.INTEGER:
            return PgType.StandardTypes.INT4;
          case Types.VARCHAR:
            return PgType.StandardTypes.VARCHAR;
          default:
            throw new IllegalArgumentException("Unsupported column type: " + columnType);
        }
      }

      private ValueConverter<ByteBufAllocator, ByteBuf>[] createConverters(ResultSet resultSet) {
        try {
          ResultSetMetaData metaData = resultSet.getMetaData();
          int columnCount = metaData.getColumnCount();
          ValueConverter<ByteBufAllocator, ByteBuf>[] converters = new ValueConverter[columnCount];
          for (int i = 1; i <= columnCount; i++) {
            int columnType = metaData.getColumnType(i);
            switch (columnType) {
              case Types.INTEGER:
                converters[i - 1] = new ValueConverter.IntToText();
                break;
              case Types.VARCHAR:
                converters[i - 1] = new ValueConverter.StringToText();
                break;
              default:
                throw new IllegalArgumentException("Unsupported column type: " + columnType);
            }
          }
          return converters;
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }

      private ByteBuf bufWithText(ChannelHandlerContext ctx, String text) {
        ByteBuf buf = ctx.alloc().buffer(ByteBufUtil.utf8MaxBytes(text.length()));
        buf.writeCharSequence(text, StandardCharsets.UTF_8);
        return buf;
      }
    };
  }

  private static class PinotDataSource implements DataSource {
    private final PinotDriver driver = new PinotDriver();
    private final String jdbcUrl;

    public PinotDataSource(String jdbcUrl) {
      this.jdbcUrl = jdbcUrl;
    }

    @Override
    public Connection getConnection()
        throws SQLException {
      return driver.connect(jdbcUrl, new Properties());
    }

    @Override
    public Connection getConnection(String username, String password)
        throws SQLException {
      return null;
    }

    @Override
    public PrintWriter getLogWriter()
        throws SQLException {
      return null;
    }

    @Override
    public void setLogWriter(PrintWriter out)
        throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds)
        throws SQLException {

    }

    @Override
    public int getLoginTimeout()
        throws SQLException {
      return 0;
    }

    @Override
    public <T> T unwrap(Class<T> iface)
        throws SQLException {
      return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface)
        throws SQLException {
      return false;
    }

    @Override
    public Logger getParentLogger()
        throws SQLFeatureNotSupportedException {
      return null;
    }
  }
}
