package pgwire;

public class ParameterBound<B> {
  private final Format format;
  private final B bytes;

  public ParameterBound(Format format, B bytes) {
    this.format = format;
    this.bytes = bytes;
  }

  public Format getFormat() {
    return format;
  }

  public B getBytes() {
    return bytes;
  }
}
