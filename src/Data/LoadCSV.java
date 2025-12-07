package Data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class LoadCSV {
	private final ArrayList<Point> points = new ArrayList<>();

	ArrayList<Point> getPoints() {
		return points;
	}

	void LoadData(String fileName) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;

			// skip header
			br.readLine();

			while ((line = br.readLine()) != null) {
				// split on comma
				String[] row = line.split(",", -1);  // -1 keeps empty fields

				if (row.length < 10) continue; // safety check

				String panoId = row[1];
				double lat = Double.parseDouble(row[2]);
				double lon = Double.parseDouble(row[3]);
				String userLabel = row[9]; // may be empty

				points.add(new Point(panoId, lat, lon, userLabel, 1.0)); // 2do: actual condition here
			}
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException(e.getMessage());
		} catch (IOException e) {
			throw new IOException(e.getMessage());
		}

	}

	LoadCSV(String fileName) throws IOException {
		LoadData(fileName);
	}
}
