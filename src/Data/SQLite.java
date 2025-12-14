package Data;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;

class SQLite {
	private final   String      DB_FILE    = "data/points.db";
	private final Connection    connection;

	SQLite() throws SQLException {
		//System.out.println("Working directory: " + Path.of("").toAbsolutePath());

		Path    absolute    = Path.of(DB_FILE).toAbsolutePath();
		String  URL         = "jdbc:sqlite:" + absolute;

		// el oh el what database
		if (!Files.exists(absolute)) {
			throw new IllegalStateException("DB file not found: " + absolute);
		}

		connection = DriverManager.getConnection(URL);
//		System.out.println("Connected: " + (connection != null));
//		System.out.println("JDBC URL: " + URL);
	}

	void error(SQLException e) {
		System.err.println("Select failed: " + e.getMessage()
			+ " | SQLState=" + e.getSQLState()
			+ " | VendorCode=" + e.getErrorCode());
			e.printStackTrace(); // see exact line and cause
			throw new RuntimeException("Select failed", e); // or rethrow; don't silently continue
	}

	ArrayList<Point> selectAllPoints() {
		ArrayList<Point> points = new ArrayList<>();
		String q    = "SELECT id, latitude, longitude, street_name, condition FROM points";

		try (PreparedStatement  ps  = connection.prepareStatement(q);
		     ResultSet rs  = ps.executeQuery())
		{
			while (rs.next()) {
				points.add(new Point(
						rs.getString("id"),
						rs.getDouble("latitude"),
						rs.getDouble("longitude"),
						rs.getString("street_name"),
						rs.getDouble("condition")
					)
				);
			}
		}
		catch (SQLException e) {
			error(e);
		}
		return points;
	}

	// return -1 for no entry
	double getDistance (String fromId, String toId) throws SQLException {
		String q    = "SELECT distance_m FROM edges WHERE from_id = ? AND to_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, fromId);
			ps.setString(2, toId);

			try (ResultSet resultSet = ps.executeQuery()) {
				if (resultSet.next()) {
					double d = resultSet.getDouble(1);
					if (resultSet.wasNull()) return -1;
					return d;
				}
			}
		}
		catch (SQLException e) {
			error(e);
		}
		return -1;
	}

	// can implement bulk update/insert for performance gain on building cache later if necessary
	void upsertDistance (String fromId, String toId, double d) {
		if (d < 0)
			return;

		String q = "INSERT INTO edges(from_id, to_id, distance_m) VALUES (?, ?, ?) ON CONFLICT(from_id, to_id) DO UPDATE SET distance_m = excluded.distance_m";
		try (PreparedStatement preparedStatement = connection.prepareStatement(q)) {
			preparedStatement.setString(1, fromId);
			preparedStatement.setString(2, toId);
			preparedStatement.setDouble(3, d);
			preparedStatement.executeUpdate();
		}
		catch (SQLException e) {
			error(e);
		}
	}
}