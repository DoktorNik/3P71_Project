/**
 * Nikolas Fraser
 * 5538939
 * Dec 19, 2025
 * COSC 3P71
 */

package Geo;

import lib.PathManager;

import java.util.Locale;

/**
 * utility class for ensuring info from and to db is consistent
 */
public class StreetTypeCodec {
	public StreetTypeCodec() {}

	/**
	 * @param t PathManager.StreetType
	 * @return  database safe string of street type
	 */
	public static String toDb(PathManager.StreetType t) {
		return (t == null || t == PathManager.StreetType.UNKNOWN) ? null : t.name();
	}

	/**
	 * @param s street type from database
	 * @return  PathManager.StreetType
	 */
	public static PathManager.StreetType fromDb(String s) {
		if (s == null || s.isEmpty()) return PathManager.StreetType.UNKNOWN;
		try {
			return PathManager.StreetType.valueOf(s.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			return PathManager.StreetType.UNKNOWN;
		}
	}
}
