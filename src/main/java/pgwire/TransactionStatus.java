package pgwire;

public enum TransactionStatus {
  /**
   * Not in a transaction block.
   */
  IDLE('I'),
  /**
   * In a transaction block.
   */
  STARTED('T'),
  /**
   * In a failed transaction block (queries will be rejected until block is ended).
   */
  FAILED('E');

  private final char id;

  TransactionStatus(char id) {
    this.id = id;
  }

  public char getId() {
    return id;
  }

  public static TransactionStatus fromValue(int id) {
    for (TransactionStatus value : values()) {
      if (value.id == (char) id) {
        return value;
      }
    }
    throw new IllegalArgumentException("Value " + id + " is not a valid transaction status id");
  }
}
