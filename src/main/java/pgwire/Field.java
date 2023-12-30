package pgwire;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class Field {
  private final ByteBuf name;
  /**
   * If the field can be identified as a column of a specific table, the object ID of the table; otherwise zero.
   */
  private final int tableOid;
  /**
   * If the field can be identified as a column of a specific table, the attribute number of the column; otherwise zero.
   */
  private final int columnIdx;
  private final PgType pgType;
  private final int typeModifier;
  private final Format format;

  public Field(ByteBuf name, int tableOid, int columnIdx, PgType pgType, int typeModifier, Format format) {
    this.name = name;
    this.tableOid = tableOid;
    this.columnIdx = columnIdx;
    this.pgType = pgType;
    this.typeModifier = typeModifier;
    this.format = format;
  }

  public ByteBuf getName() {
    return name;
  }

  /**
   * If the field can be identified as a column of a specific table, the object ID of the table; otherwise zero.
   */
  public int getTableOid() {
    return tableOid;
  }

  /**
   * If the field can be identified as a column of a specific table, the attribute number of the column; otherwise zero.
   */
  public int getColumnIdx() {
    return columnIdx;
  }

  public PgType getPgType() {
    return pgType;
  }

  /**
   * The type modifier (see Postgres pg_attribute.atttypmod). The meaning of the modifier is type-specific.
   */
  public int getTypeModifier() {
    return typeModifier;
  }

  public Format getFormat() {
    return format;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Field field2 = (Field) o;
    return tableOid == field2.tableOid && columnIdx == field2.columnIdx && typeModifier == field2.typeModifier
        && Objects.equals(name, field2.name) && Objects.equals(pgType, field2.pgType) && format == field2.format;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, tableOid, columnIdx, pgType, typeModifier, format);
  }

  @Override
  public String toString() {
    CharSequence nameStr = name.getCharSequence(name.readerIndex(), name.readableBytes(), StandardCharsets.UTF_8);
    return "Field2{" + "name=" + nameStr + ", tableOid=" + tableOid + ", columnIdx=" + columnIdx + ", pgType=" + pgType
        + ", typeModifier=" + typeModifier + ", format=" + format + '}';
  }
}
