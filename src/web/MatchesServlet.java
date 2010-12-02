package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.BattlecodeMap;

public class MatchesServlet extends AbstractServlet {
	private static final long serialVersionUID = 3122992891626513814L;
	public static final String NAME = "matches.html";
	private static final String[] win_conditions = {"destroy", "points", "time"};

	public MatchesServlet() {
		super(NAME);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = checkLogin(request, response);
		if (username == null) {
			redirect(response);
			return;
		}
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/tinytable.css\" />");
		out.println("<link rel=\"stylesheet\" href=\"css/tabs.css\" />");
		out.println("<script type=\"text/javascript\">");
		// AJAX call to FileServlet
		out.println("function downloadMatch(id) {\n" +
				"document.location = \"" + response.encodeURL(MatchDownloadServlet.NAME) + "?id=\" + id;\n" +
		"}");	
		out.println("</script>");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(response, out, name);

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
			String team_a = rs.getString("a_nick");
			team_a = (team_a == null ? rs.getString("team_a") : "<a title='" + rs.getString("team_a") + "'>" + team_a + "</a>");
			String team_b = rs.getString("b_nick");
			team_b = (team_b == null ? rs.getString("team_b") : "<a title='" + rs.getString("team_b") + "'>" + team_b + "</a>");
			out.println("<h2><font color='red'>" + team_a + "</font> vs. <font color='blue'>" + team_b + "</font></h2>");
			out.println("<h3>" + WebUtil.getFormattedMapResults(WebUtil.getMapResults(id, null, false)) + "</h3>");
			out.println("<br />");
			out.println("<div class='tabbutton'>");
			out.println("<a onClick='document.location=\"" + response.encodeURL(MapAnalysisServlet.NAME) + "?id=" + id + "\"' " +
			"style='cursor:pointer;'><span>View by map</span></a>");
			out.println("<p>&nbsp;</p>");
			out.println("<p>&nbsp;</p>");
			out.println("<p>&nbsp;</p>");
			out.println("</div>");

			st.close();

			WebUtil.printTableHeader(out, "matches_sorter");
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" id=\"matches_table\" class=\"tinytable\">" +
					"<thead>" + 
					"<tr>" +
					"<th class='desc'><h3>Map</h3></th>" +
					"<th class='desc'><h3>Map seed</h3></th>" +
					"<th class='desc'><h3>Winner</h3></th>" +
					"<th class='desc'><h3>Size</h3></th>" +
					"<th class='desc'><h3>Win condition</h3></th>" +
					"<th class='desc'><h3>Points</h3></th>" +
					"<th class='nosort'><h3>&nbsp;</h3></th>" +
					"</tr>" +
					"</thead>" +
			"<tbody>");
			PreparedStatement st3 = db.prepare("SELECT * FROM matches WHERE run_id = ? AND win IS NOT NULL ORDER BY map");
			st3.setInt(1, id);
			ResultSet rs3 = db.query(st3);
			while (rs3.next()) {
				int a_points = rs3.getInt("a_points");
				int b_points = rs3.getInt("b_points");
				BattlecodeMap map = new BattlecodeMap(rs3.getString("map"), rs3.getInt("height"), rs3.getInt("width"), 
						rs3.getInt("rounds"), rs3.getInt("points"));
				out.println("<tr>");
				out.println("<td>" + map.map + "</td>");
				out.println("<td>" + rs3.getInt("seed") + "</td>");
				out.println("<td><font color='" + (rs3.getInt("win") == 1 ? "red'>" + team_a : "blue'>" + team_b) + "</font></td>");
				out.println("<td>" + map.getSize() + "</td>");
				out.println("<td>" + win_conditions[rs3.getInt("win_condition")] + "</td>");
				out.println("<td><font color='red'>" + a_points + "</font>/<font color='blue'>" + 
						b_points + "</font></td>");
				out.println("<td><input type=button value=\"download\" onclick=\"downloadMatch(" + 
						rs3.getString("id") + ")\"></td>");
				out.println("</tr>");
			}
			st3.close();
			out.println("</tbody>");
			out.println("</table>");
			WebUtil.printTableFooter(out, "matches_sorter");

			out.println("<script type=\"text/javascript\" src=\"js/script.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"js/matches_init_table.js\"></script>");
			out.println("</body></html>");
		} catch (SQLException e) {
			e.printStackTrace(out);
		}
	}

}
