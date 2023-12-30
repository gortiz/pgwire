package pgwire;

import java.util.ArrayList;
import java.util.List;


public class PgType {
  private final int oid;
  private final String name;
  private final int arrayType;
  private final int elementType;

  /**
   * The data type size. Note that negative values denote variable-width types.
   */
  private final int byteLength;
  private final Type type;
  private final Category category;

  PgType(int oid, String name, int arrayType, int elementType, int byteLength, Type type, Category category) {
    this.oid = oid;
    this.name = name;
    this.arrayType = arrayType;
    this.elementType = elementType;
    this.byteLength = byteLength;
    this.type = type;
    this.category = category;
  }

  public int getOid() {
    return oid;
  }

  public String getName() {
    return name;
  }

  public int getArrayType() {
    return arrayType;
  }

  public int getElementType() {
    return elementType;
  }

  /**
   * The data type size. Note that negative values denote variable-width types.
   */
  public int getByteLength() {
    return byteLength;
  }

  public Type getType() {
    return type;
  }

  public Category getCategory() {
    return category;
  }

  public static class StandardTypes {

    public static final PgType BOOL = new PgType(16,"bool",1000,0,1, Type.Base, Category.BOOLEAN);
    public static final PgType BYTEA = new PgType(17, "bytea", 1001, 0, -1, Type.Base, Category.USER_DEFINED);
    public static final PgType CHAR = new PgType(18,"char",1002,0,1, Type.Base, Category.STRING);

    public static final PgType NAME = new PgType(19, "name", 1003, 18, 64, Type.Base, Category.STRING);
    public static final PgType INT8 = new PgType(20, "int8", 1016, 0, 8, Type.Base, Category.NUMERIC);
    public static final PgType INT2 = new PgType(21, "int2", 1005, 0, 2, Type.Base, Category.NUMERIC);
    public static final PgType INT2_VECTOR = new PgType(22, "int2vector", 1006, 21, -1, Type.Base, Category.ARRAY);
    public static final PgType INT4 = new PgType(23, "int4", 1007, 0, 4, Type.Base, Category.NUMERIC);
    public static final PgType REG_PROC = new PgType(24, "regproc", 1008, 0, 4, Type.Base, Category.NUMERIC);
    public static final PgType TEXT = new PgType(25, "text", 1009, 0, -1, Type.Base, Category.STRING);
    public static final PgType OID = new PgType(26, "oid", 1028, 0, 4, Type.Base, Category.NUMERIC);

    public static final PgType JSON = new PgType(114, "json", 199, 0, -1, Type.Base, Category.USER_DEFINED);
    public static final PgType XML = new PgType(142, "xml", 143, 0, -1, Type.Base, Category.USER_DEFINED);
    public static final PgType BPCHAR = new PgType(1042, "bpchar", 1014, 0, -1, Type.Base, Category.STRING);
    public static final PgType VARCHAR = new PgType(1043, "varchar", 1015, 0, -1, Type.Base, Category.STRING);
    public static final PgType DATE = new PgType(1082, "date", 1182, 0, 4, Type.Base, Category.DATE_TIME);
    public static final PgType TIME = new PgType(1083, "time", 1183, 0, 8, Type.Base, Category.DATE_TIME);
    public static final PgType TIMESTAMP = new PgType(1114, "timestamp", 1114, 0, 8, Type.Base, Category.DATE_TIME);
    public static final PgType TIMESTAMPTZ = new PgType(1184, "timestamptz", 1184, 0, 8, Type.Base, Category.DATE_TIME);
    public static final PgType INTERVAL = new PgType(1186, "interpublic static final PgType ", 1186, 0, 16, Type.Base, Category.TIMESPAN);
    public static final PgType NUMERIC = new PgType(1231, "numeric", 1231, 0, -1, Type.Base, Category.NUMERIC);
        
    public static List<PgType> allTypes() {
      ArrayList<PgType> result = new ArrayList<>();
      result.add(BOOL);
      result.add(BYTEA);
      result.add(CHAR);
      result.add(NAME);
      result.add(INT8);
      result.add(INT2);
      result.add(INT2_VECTOR);
      result.add(INT4);
      result.add(REG_PROC);
      result.add(TEXT);
      result.add(OID);
      result.add(JSON);
      result.add(XML);
      result.add(BPCHAR);
      result.add(VARCHAR);
      result.add(DATE);
      result.add(TIME);
      result.add(TIMESTAMP);
      result.add(TIMESTAMPTZ);
      result.add(INTERVAL);
      result.add(NUMERIC);
      return result;
    }
  }

  public enum Type {
    Base('b'), Composite('c'), Domain('d'), Enum('e'), Pseudo('p'), Range('r'), Multirange('m');

    private final char id;

    Type(char id) {
      this.id = id;
    }

    public char getId() {
      return id;
    }
  }

  public enum Category {
    ARRAY('A'),
    BOOLEAN('B'),
    COMPOSITE('C'),
    DATE_TIME('D'),
    ENUM('E'),
    GEOMETRIC('G'),
    NETWORK_ADDRESS('I'),
    NUMERIC('N'),
    PSEUDO('P'),
    RANGE('R'),
    STRING('S'),
    TIMESPAN('T'),
    USER_DEFINED('U'),
    BIT_STRING('V'),
    UNKNOWN('X');
    private final char id;

    Category(char id) {
      this.id = id;
    }

    public char getId() {
      return id;
    }
  }
}
