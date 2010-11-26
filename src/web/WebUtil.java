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

	public static void writeTabs(HttpServletResponse response, PrintWriter out) {
		out.println("<a href='" + LogoutServlet.NAME + "'>logout</a>");
		// Header with tabs
		out.println("<div id=\"tabs\"><h2>");
		out.println("<ul>" +
				"<li><a href='" + response.encodeURL(IndexServlet.NAME) + "'><span>Home</span></a></li>");
		if (Config.getConfig().version_control.equals("svn")) {
			out.println("<li><a href='" + response.encodeURL(TagServlet.NAME) + "'><span>Tags</span></a></li>");
		}
		out.println("<li><a href='" + response.encodeURL(ConnectionsServlet.NAME) + "'><span>Connections</span></a></li>" +
				"<li><a href='" + response.encodeURL(SizeAnalysisServlet.NAME) + "'><span>Size Analysis</span></a></li>" +
				"<li><a href='" + response.encodeURL(AdminServlet.NAME) + "'><span>Admin</span></a></li>" +
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
		int[] results = new int[3];
		if (maps == null) {
			maps = new HashSet<String>();
			ResultSet mapSet = Config.getDB().query("SELECT map FROM matches WHERE run_id = " + runid + " AND win IS NOT NULL GROUP BY map");
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
		Database db = Config.getDB();
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

	public static void printTableHeader(PrintWriter out, String sorter) {
		out.println("<div id=\"tableheader\">" +
				"<div class=\"search\">" +
				"<select id=\"columns\" onchange=\"" + sorter + ".search('query')\"></select>");
		out.println("<input type=\"text\" id=\"query\" onkeyup=\"" + sorter + ".search('query')\" />");
		out.println("</div>");
		out.println("<span class=\"details\">" +
				"<div>Records <span id=\"startrecord\"></span>-<span id=\"endrecord\"></span> of " +
		"<span id=\"totalrecords\"></span></div>");
		out.println("<div><a href=\"javascript:" + sorter + ".reset()\">reset</a></div>" +
		"</span>");
		out.println("</div>");
	}

	public static void printTableFooter(PrintWriter out, String sorter) {
		out.println("<div id=\"tablefooter\">");
		out.println("<div id=\"tablenav\">");
		out.println("<div>");
		out.println("<img src=\"images/first.gif\" width=\"16\" height=\"16\" alt=\"First Page\" " +
				"onclick=\"" + sorter + ".move(-1,true)\" />");
		out.println("<img src=\"images/previous.gif\" width=\"16\" height=\"16\" alt=\"Previous Page\" " +
				"onclick=\"" + sorter + ".move(-1)\" />");
		out.println("<img src=\"images/next.gif\" width=\"16\" height=\"16\" alt=\"Next Page\" " +
				"onclick=\"" + sorter + ".move(1)\" />");
		out.println("<img src=\"images/last.gif\" width=\"16\" height=\"16\" alt=\"Last Page\" " +
				"onclick=\"" + sorter + ".move(1,true)\" />");
		out.println("</div>");
		out.println("<div>");
		out.println("<select id=\"pagedropdown\"></select>");
		out.println("</div>");
		out.println("<div>");
		out.println("<a href=\"javascript:" + sorter + ".showall()\">view all</a>");
		out.println("</div>");
		out.println("</div>");
		out.println("<div id=\"tablelocation\">");
		out.println("<div>");
		out.println("<select onchange=\"" + sorter + ".size(this.value)\">");
		out.println("<option value=\"5\">5</option>");
		out.println("<option value=\"10\" selected=\"selected\">10</option>");
		out.println("<option value=\"20\">20</option>");
		out.println("<option value=\"50\">50</option>");
		out.println("</select>");
		out.println("<span>Entries Per Page</span>");
		out.println("</div>");
		out.println("<div class=\"page\">Page <span id=\"currentpage\"></span> of <span id=\"totalpages\"></span></div>");
		out.println("</div></div>");
	}

}
