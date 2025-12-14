package Data;

final class StreetInfo {

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
