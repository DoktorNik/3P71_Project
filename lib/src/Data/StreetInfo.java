/**
 * Nikolas Fraser  * 5538939
 * Dec 19, 2025
 * COSC 3P71
 */

package Data;

import lib.PathManager;

/**
 * street info class for reasons of super modular code
 */
public final class StreetInfo {

	private final String name;
	private final PathManager.StreetType type;

	public StreetInfo(String name, PathManager.StreetType type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public PathManager.StreetType getType() {
		return type;
	}
}