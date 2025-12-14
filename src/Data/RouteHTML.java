package Data;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

final class RouteHtml {

	public static void writeLeafletHtml(Path htmlFile,
	                                    List<Point> route,
	                                    boolean addNumbers,
	                                    boolean showStreetLabels)
			throws IOException {

		if (route == null || route.isEmpty())
			throw new IllegalArgumentException("Empty route");

		// Build JS array of point objects
		StringBuilder jsPoints = new StringBuilder("[");
		for (int i = 0; i < route.size(); i++) {
			Point p = route.get(i);
			if (i > 0) jsPoints.append(",");
			jsPoints.append("{")
					.append("\"id\":\"").append(escape(p.getId())).append("\",")
					.append("\"street\":\"").append(escape(p.getStreetName())).append("\",")
					.append("\"lat\":").append(p.getLatitude()).append(",")
					.append("\"lon\":").append(p.getLongitude())
					.append("}");
		}
		jsPoints.append("]");

		String html =
				"<!doctype html>\n" +
						"<html>\n" +
						"<head>\n" +
						"  <meta charset=\"utf-8\"/>\n" +
						"  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n" +
						"  <title>Route Preview</title>\n" +
						"  <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\"/>\n" +
						"  <style>\n" +
						"    html,body,#map{height:100%;margin:0}\n" +
						"    .num-label{background:#1f2937;color:#fff;border-radius:10px;padding:2px 6px;"
						+ "font:12px system-ui;border:1px solid rgba(0,0,0,0.25)}\n" +
						"    .street-label{background:rgba(255,255,255,0.85);color:#111;"
						+ "border-radius:6px;padding:2px 6px;font:11px system-ui;"
						+ "border:1px solid rgba(0,0,0,0.2)}\n" +
						"    .start{background:#10b981}\n" +
						"    .end{background:#ef4444}\n" +
						"  </style>\n" +
						"</head>\n" +
						"<body>\n" +
						"  <div id=\"map\"></div>\n" +
						"  <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n" +
						"  <script>\n" +
						"    var pts = " + jsPoints + ";\n" +
						"    var map = L.map('map');\n" +
						"    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
						"      maxZoom: 19,\n" +
						"      attribution: '&copy; OpenStreetMap contributors'\n" +
						"    }).addTo(map);\n" +
						"\n" +
						"    var latlngs = pts.map(function(p){ return [p.lat, p.lon]; });\n" +
						"    var line = L.polyline(latlngs, {weight:4}).addTo(map);\n" +
						"\n" +
						"    // Point markers + tooltips\n" +
						"    pts.forEach(function(p, idx){\n" +
						"      var isStart = idx === 0;\n" +
						"      var isEnd = idx === pts.length - 1;\n" +
						"      var color = isStart ? '#10b981' : (isEnd ? '#ef4444' : '#2563eb');\n" +
						"      L.circleMarker([p.lat, p.lon], {\n" +
						"        radius: isStart || isEnd ? 6 : 4,\n" +
						"        color: color,\n" +
						"        fillColor: color,\n" +
						"        fillOpacity: 0.85\n" +
						"      }).addTo(map)\n" +
						"      .bindTooltip(\n" +
						"        '<b>' + p.street + '</b><br/>' +\n" +
						"        'ID: ' + p.id + '<br/>' +\n" +
						"        '#' + idx + '<br/>' +\n" +
						"        p.lat.toFixed(6) + ', ' + p.lon.toFixed(6)\n" +
						"      );\n" +
						"    });\n" +
						"\n" +
						"    // Optional numbered markers\n" +
						"    if (" + (addNumbers ? "true" : "false") + ") {\n" +
						"      pts.forEach(function(p, idx){\n" +
						"        var cls = 'num-label';\n" +
						"        if (idx === 0) cls += ' start';\n" +
						"        else if (idx === pts.length - 1) cls += ' end';\n" +
						"        var icon = L.divIcon({\n" +
						"          html: '<div class=\"' + cls + '\">' + idx + '</div>',\n" +
						"          className: '',\n" +
						"          iconAnchor: [10,10]\n" +
						"        });\n" +
						"        L.marker([p.lat, p.lon], {icon: icon}).addTo(map);\n" +
						"      });\n" +
						"    }\n" +
						"\n" +
						"    // Optional always-visible street name labels\n" +
						"    if (" + (showStreetLabels ? "true" : "false") + ") {\n" +
						"      pts.forEach(function(p){\n" +
						"        var icon = L.divIcon({\n" +
						"          html: '<div class=\"street-label\">' + p.street + '</div>',\n" +
						"          className: '',\n" +
						"          iconAnchor: [0, -10]\n" +
						"        });\n" +
						"        L.marker([p.lat, p.lon], {icon: icon}).addTo(map);\n" +
						"      });\n" +
						"    }\n" +
						"\n" +
						"    map.fitBounds(line.getBounds(), {padding:[20,20]});\n" +
						"  </script>\n" +
						"</body>\n" +
						"</html>";

		Files.createDirectories(htmlFile.getParent());
		Files.write(htmlFile, html.getBytes());
	}

	static void plotPoints(Path htmlFile, List<Point> points, boolean showStreetLabels) throws IOException {

		if (points == null || points.isEmpty())
			throw new IllegalArgumentException("No points");

		// Build JS array of point objects
		StringBuilder jsPoints = new StringBuilder("[");
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (i > 0) jsPoints.append(",");
			jsPoints.append("{")
					.append("\"id\":\"").append(escape(p.getId())).append("\",")
					.append("\"street\":\"").append(escape(p.getStreetName())).append("\",")
					.append("\"lat\":").append(p.getLatitude()).append(",")
					.append("\"lon\":").append(p.getLongitude())
					.append("}");
		}
		jsPoints.append("]");

		String html =
				"<!doctype html>\n" +
						"<html>\n" +
						"<head>\n" +
						"  <meta charset=\"utf-8\"/>\n" +
						"  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n" +
						"  <title>All Points</title>\n" +
						"  <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\"/>\n" +
						"  <style>\n" +
						"    html,body,#map{height:100%;margin:0}\n" +
						"    .street-label{background:rgba(255,255,255,0.85);color:#111;"
						+ "border-radius:6px;padding:2px 6px;font:11px system-ui;"
						+ "border:1px solid rgba(0,0,0,0.2)}\n" +
						"  </style>\n" +
						"</head>\n" +
						"<body>\n" +
						"  <div id=\"map\"></div>\n" +
						"  <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n" +
						"  <script>\n" +
						"    var pts = " + jsPoints + ";\n" +
						"\n" +
						"    var map = L.map('map');\n" +
						"    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
						"      maxZoom: 19,\n" +
						"      attribution: '&copy; OpenStreetMap contributors'\n" +
						"    }).addTo(map);\n" +
						"\n" +
						"    var bounds = [];\n" +
						"\n" +
						"    pts.forEach(function(p){\n" +
						"      bounds.push([p.lat, p.lon]);\n" +
						"      L.circleMarker([p.lat, p.lon], {\n" +
						"        radius: 4,\n" +
						"        color: '#2563eb',\n" +
						"        fillColor: '#2563eb',\n" +
						"        fillOpacity: 0.75\n" +
						"      }).addTo(map)\n" +
						"      .bindTooltip(\n" +
						"        '<b>' + p.street + '</b><br/>' +\n" +
						"        'ID: ' + p.id + '<br/>' +\n" +
						"        p.lat.toFixed(6) + ', ' + p.lon.toFixed(6)\n" +
						"      );\n" +
						"    });\n" +
						"\n" +
						"    // Optional always-visible street labels\n" +
						"    if (" + (showStreetLabels ? "true" : "false") + ") {\n" +
						"      pts.forEach(function(p){\n" +
						"        var icon = L.divIcon({\n" +
						"          html: '<div class=\"street-label\">' + p.street + '</div>',\n" +
						"          className: '',\n" +
						"          iconAnchor: [0, -10]\n" +
						"        });\n" +
						"        L.marker([p.lat, p.lon], {icon: icon}).addTo(map);\n" +
						"      });\n" +
						"    }\n" +
						"\n" +
						"    if (bounds.length > 0) {\n" +
						"      map.fitBounds(bounds, {padding:[20,20]});\n" +
						"    }\n" +
						"  </script>\n" +
						"</body>\n" +
						"</html>";

		Files.createDirectories(htmlFile.getParent());
		Files.write(htmlFile, html.getBytes());
	}

	private static String escape(String s) {
		return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
