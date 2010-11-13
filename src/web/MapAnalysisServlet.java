package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.BattlecodeMap;

public class MapAnalysisServlet extends AbstractServlet {
	private static final long serialVersionUID = 5352256649398067730L;
	public static final String NAME = "map_analysis.html";
	
	public MapAnalysisServlet() {
		super(NAME);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/tinytable.css\" />");
		out.println("<link rel=\"stylesheet\" href=\"css/tabs.css\" />");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(response, out);

		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id</body></html>");
			return;
		}
		int id = Integer.parseInt(strId);
		try {
			out.println("<div id=\"tablewrapper\">");
			PreparedStatement st = db.prepare("SELECT id, team_a, t1.alias a_nick, team_b, t2.alias b_nick " +
					"FROM runs r LEFT JOIN tags t1 ON r.team_a = t1.tag " +
			"LEFT JOIN tags t2 ON r.team_b = t2.tag WHERE id = ?");
			st.setInt(1, id);
			ResultSet rs = db.query(st);
			rs.next();
			String team_a = rs.getString("team_a");
			String team_b = rs.getString("team_b");
			out.println("<h2><font color='red'>" + team_a + "</font> vs. <font color='blue'>" + team_b + "</font></h2>");
			out.println("<h3>" + WebUtil.getFormattedMapResults(WebUtil.getMapResults(id, null, false)) + "</h3>");
			out.println("<br />");
			out.println("<div class='tabbutton'>");
			out.println("<a onClick='document.location=\"" + response.encodeURL(MatchesServlet.NAME) + "?id=" + id + "\"' " +
			"style='cursor:pointer;'><span>View by match</span></a>");
			out.println("<p>&nbsp;</p>");
			out.println("<p>&nbsp;</p>");
			out.println("<p>&nbsp;</p>");
			out.println("</div>");

			st.close();

			out.println("<div id=\"tableheader\">" +
					"<div class=\"search\">" +
			"<select id=\"coldid\" onchange=\"matches_sorter.search('query')\"></select>");
			out.println("<input type=\"text\" id=\"query\" onkeyup=\"matches_sorter.search('query')\" />");
			out.println("</div>");
			out.println("<span class=\"details\">" +
					"<div>Records <span id=\"startrecord\"></span>-<span id=\"endrecord\"></span> of " +
			"<span id=\"totalrecords\"></span></div>");
			out.println("<div><a href=\"javascript:matches_sorter.reset()\">reset</a></div>" +
			"</span>");
			out.println("</div>");
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" id=\"matches_table\" class=\"tinytable\">" +
					"<thead>" + 
					"<tr>" +
					"<th class='desc'><h3>Map</h3></th>" +
					"<th class='desc'><h3>Win %</h3></th>" +
					"<th class='desc'><h3>Size</h3></th>" +
					"</tr>" +
					"</thead>" +
			"<tbody>");
			PreparedStatement st3 = db.prepare("SELECT * FROM matches WHERE run_id = ? AND win IS NOT NULL");
			st3.setInt(1, id);
			ResultSet rs3 = db.query(st3);
			HashSet<String> seenMaps = new HashSet<String>();
			while (rs3.next()) {
				if (seenMaps.contains(rs3.getString("map"))) 
					continue;
				seenMaps.add(rs3.getString("map"));
				BattlecodeMap map = new BattlecodeMap(rs3.getString("map"), rs3.getInt("height"), rs3.getInt("width"), 
						rs3.getInt("rounds"), rs3.getInt("points"));
				out.println("<tr>");
				out.println("<td>" + map.map + "</td>");
				out.println("<td>" + WebUtil.getFormattedWinPercentage(WebUtil.getWinPercentage(id, map.map)) + "</td>");
				out.println("<td>" + map.getSize() + "</td>");
				out.println("</tr>");
			}
			st3.close();
			out.println("</tbody>");
			out.println("</table>");
			out.println("<div id=\"tablefooter\">");
			out.println("<div id=\"tablenav\">");
			out.println("<div>");
			out.println("<img src=\"images/first.gif\" width=\"16\" height=\"16\" alt=\"First Page\" " +
			"onclick=\"matches_sorter.move(-1,true)\" />");
			out.println("<img src=\"images/previous.gif\" width=\"16\" height=\"16\" alt=\"Previous Page\" " +
			"onclick=\"matches_sorter.move(-1)\" />");
			out.println("<img src=\"images/next.gif\" width=\"16\" height=\"16\" alt=\"Next Page\" " +
			"onclick=\"matches_sorter.move(1)\" />");
			out.println("<img src=\"images/last.gif\" width=\"16\" height=\"16\" alt=\"Last Page\" " +
			"onclick=\"matches_sorter.move(1,true)\" />");
			out.println("</div>");
			out.println("<div>");
			out.println("<select id=\"pagedropdown\"></select>");
			out.println("</div>");
			out.println("<div>");
			out.println("<a href=\"javascript:matches_sorter.showall()\">view all</a>");
			out.println("</div>");
			out.println("</div>");
			out.println("<div id=\"tablelocation\">");
			out.println("<div>");
			out.println("<select onchange=\"matches_sorter.size(this.value)\">");
			out.println("<option value=\"5\">5</option>");
			out.println("<option value=\"10\" selected=\"selected\">10</option>");
			out.println("<option value=\"20\">20</option>");
			out.println("<option value=\"50\">50</option>");
			out.println("</select>");
			out.println("<span>Entries Per Page</span>");
			out.println("</div>");
			out.println("<div class=\"page\">Page <span id=\"currentpage\"></span> of <span id=\"totalpages\"></span></div>");
			out.println("</div></div></div>");

			out.println("<script type=\"text/javascript\" src=\"js/script.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"js/matches_init_table.js\"></script>");
			out.println("</body></html>");
		} catch (SQLException e) {
			e.printStackTrace(out);
		}
	}
}
