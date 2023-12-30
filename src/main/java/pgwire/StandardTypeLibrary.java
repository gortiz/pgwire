package pgwire;

import java.util.List;
import java.util.function.IntFunction;


public class StandardTypeLibrary implements IntFunction<PgType> {
  private final PgType[] _types;

  public StandardTypeLibrary() {
    List<PgType> pgTypes = PgType.StandardTypes.allTypes();
    int maxOid = pgTypes.stream().mapToInt(PgType::getOid).max().getAsInt();
    _types = new PgType[maxOid + 1];
    for (PgType pgType : pgTypes) {
      _types[pgType.getOid()] = pgType;
    }
  }

  @Override
  public PgType apply(int oid) {
    return _types[oid];
  }
}
