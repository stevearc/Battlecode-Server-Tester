package web;

import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import javax.servlet.http.HttpServletResponse;

import common.Config;

import db.Database;

public class WebUtil {
	private static Database db;

	public static void writeTabs(HttpServletResponse response, PrintWriter out) {
		// Header with tabs
		out.println("<div id=\"tabs\"><h2>");
		out.println("<ul>" +
				"<li><a href='" + response.encodeURL(IndexServlet.NAME) + "'><span>Home</span></a></li>" +
				"<li><a href='" + response.encodeURL(ConnectionsServlet.NAME) + "'><span>Connections</span></a></li>" +
				"<li><a href='" + response.encodeURL(SizeAnalysisServlet.NAME) + "'><span>Size Analysis</span></a></li>" +
		"</ul>");
		out.println("</h2></div>");
		out.println("<p>&nbsp;</p>");
		out.println("<p>&nbsp;</p>");
	}

	public static String getFormattedMapResults(int[] results) {
		return "<font color='red'>" + results[0] + "</font>/<font color='gray'>" + results[1] + 
		"</font>/<font color='blue'>" + results[2] + "</font>";
	}

	public static int[] getMapResults(int runid, HashSet<String> maps, boolean reverse) throws SQLException {
		if (db == null)
			db = Config.getDB();
		int[] results = new int[3];
		if (maps == null) {
			maps = new HashSet<String>();
			ResultSet mapSet = db.query("SELECT map FROM matches WHERE run_id = " + runid + " AND win IS NOT NULL GROUP BY map");
			while (mapSet.next())
				maps.add(mapSet.getString("map"));
		}
		for (String m: maps) {
			results[getMapResult(runid, m)]++;
		}
		if (reverse) {
			int swap = results[0];
			results[0] = results[2];
			results[2] = swap;
		}
		return results;
	}

	private static int getMapResult(int runid, String map) throws SQLException {
		float ratio = getWinPercentage(runid, map);
		if (ratio < 0.3)
			return 2;
		else if (ratio < 0.7)
			return 1;
		else
			return 0;
	}

	public static float getWinPercentage(int runid, String map) throws SQLException {
		PreparedStatement stmt = db.prepare("SELECT SUM(win) AS wins, COUNT(*) AS total FROM matches WHERE run_id = ? AND win IS NOT NULL AND map LIKE ?");
		stmt.setInt(1, runid);
		stmt.setString(2, map);
		ResultSet rs = db.query(stmt);
		rs.next();
		int wins = rs.getInt("wins");
		int total = rs.getInt("total");
		rs.close();
		return (float) wins/ (float) total;
	}

	public static String getFormattedWinPercentage(float percent) {
		if (percent < 0.3)
			return "<font color='blue'>" + (int) (100*percent) + "</font>";
		else if (percent < 0.7)
			return "<font color='gray'>" + (int) (100*percent) + "</font>";
		else 
			return "<font color='red'>" + (int) (100*percent) + "</font>";
	}
}
