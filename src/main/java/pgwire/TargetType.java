package pgwire;

public enum TargetType {
  STATEMENT('S'), PORTAL('P');

  TargetType(char id) {
    this.id = id;
  }

  private final char id;

  public static TargetType fromId(int id) {
    for (TargetType value : values()) {
      if (value.id == (char) id) {
        return value;
      }
    }
    throw new IllegalArgumentException("Id " + (char) id + " is not a valid target type");
  }
}
