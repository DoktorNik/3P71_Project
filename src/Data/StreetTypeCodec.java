package Data;

import java.util.Locale;

final class StreetTypeCodec {
	private StreetTypeCodec() {}

	static String toDb(PathManager.StreetType t) {
		return (t == null || t == PathManager.StreetType.UNKNOWN) ? null : t.name();
	}

	static PathManager.StreetType fromDb(String s) {
		if (s == null || s.isEmpty()) return PathManager.StreetType.UNKNOWN;
		try {
			return PathManager.StreetType.valueOf(s.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			return PathManager.StreetType.UNKNOWN;
		}
	}
}
