package pgwire;

import io.netty.buffer.ByteBuf;


public enum Format {
  TEXT((byte) 0), BINARY((byte) 1);

  private final byte _id;

  Format(byte id) {
    _id = id;
  }

  public byte getId() {
    return _id;
  }

  public static Format fromId(int id) {
    for (Format format : Format.values()) {
      if (format.getId() == id) {
        return format;
      }
    }
    throw new IllegalArgumentException("Id " + id + " is not a valid id");
  }
}
