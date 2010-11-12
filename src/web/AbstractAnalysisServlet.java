package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractAnalysisServlet extends AbstractServlet {
	private static final long serialVersionUID = 5929611248935884353L;

	public AbstractAnalysisServlet(String name) {
		super(name);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String version = request.getParameter("version");
		try {
			out.println("<html><head>");
			out.println("<title>Battlecode Tester</title>");
			out.println("<link rel=\"stylesheet\" href=\"css/tinytable.css\" />");
			out.println("<link rel=\"stylesheet\" href=\"css/tabs.css\" />");
			out.println("</head>");
			out.println("<body>");

			WebUtil.writeTabs(response, out);

			// Javascript to do HTTP GET
			out.println("<script type=\"text/Javascript\">\n" +
					"function doGet(version) {\n" +
					"var loc = document.location.toString();" + 
					"\tvar index = loc.indexOf(\"?\");\n" +
					"\tif (index == -1) { index = loc.length }\n" +
					"\tvar url = loc.substr(0, index);" +
					"\tdocument.location = url + \"?version=\" + version\n" +
					"}" +
			"</script>");

			// Team selector
			ResultSet r = db.query("SELECT team_a, team_b FROM runs");
			HashSet<String> teams = new HashSet<String>();
			while (r.next()) {
				teams.add(r.getString("team_a"));
				teams.add(r.getString("team_b"));
			}
			String[] teamArray = teams.toArray(new String[teams.size()]);
			Arrays.sort(teamArray);
			int index = 0;
			if (version == null)
				version = teamArray[0];
			index = Arrays.binarySearch(teamArray, version);
			out.println("<div id=\"tablewrapper\">");
			out.println("<p>" +
			"<select id='selector' name='version' onChange='doGet(document.getElementById(\"selector\").value)' " +
			"style='margin:0 auto;'>");
			for (String s: teamArray) {
				out.println("<option name='" + s + "'>" + s + "</option>");
			}
			out.println("</select>");
			out.println("</p><br />");
			out.println("<script type=\"text/Javascript\">" +
					"document.getElementById('selector').selectedIndex=" + index + 
			"</script>");
			r.close();

			PreparedStatement stmt = db.prepare("SELECT * FROM runs WHERE (team_a LIKE ? OR team_b LIKE ?) AND status = 2");
			stmt.setString(1, version);
			stmt.setString(2, version);
			ResultSet rs = db.query(stmt);

			out.println("<div id=\"tableheader\" class='removed'>" +
					"<div class=\"search\">" +
			"<select id=\"coldid\" onchange=\"analysis_sorter.search('analysis_query')\"></select>");
			out.println("<input type=\"text\" id=\"analysis_query\" onkeyup=\"analysis_sorter.search('analysis_query')\" />");
			out.println("</div>");
			out.println("<div><a href=\"javascript:analysis_sorter.reset()\">reset</a></div>" +
			"</span>");
			out.println("</div>");
			out.println("<h1>" + version + "</h1><br />");
			out.println("<table id=\"analysis_table\" class=\"tinytable\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
			out.println("<thead>");
			writeTableHead(out);
			out.println("</thead>");
			out.println("<tbody>");

			while (rs.next()) {
				int runid = rs.getInt("id");
				String team_a = rs.getString("team_a");
				String team_b = rs.getString("team_b");
				boolean reverse = team_a.equals(version);
				String row_team = (reverse ? team_b : team_a);
				writeTableRow(out, runid, row_team, reverse);
			}
			rs.close();
			out.println("</tbody>");
			out.println("</table>");
			out.println("</div>");

			out.println("<script type=\"text/javascript\" src=\"js/script.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"js/analysis_init_table.js\"></script>");
			out.println("</body></html>");
		} catch (Exception e) {
			e.printStackTrace(out);
		}
	}

	protected abstract void writeTableHead(PrintWriter out);

	protected abstract void writeTableRow(PrintWriter out, int runid, String row_team, boolean reverse) throws Exception;

	protected String getFormattedMapResults(int[] results) {
		return "<font color='red'>" + results[0] + "</font>/<font color='blue'>" + results[1] + 
		"</font>/<font color='green'>" + results[2] + "</font>";
	}

	protected int[] getMapResults(int runid, HashSet<String> maps, boolean reverse) throws SQLException {
		int[] results = new int[3];
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

	protected int getMapResult(int runid, String map) throws SQLException {
		PreparedStatement stmt = db.prepare("SELECT SUM(win) as wins FROM matches WHERE run_id = ? AND map LIKE ?");
		stmt.setInt(1, runid);
		stmt.setString(2, map);
		ResultSet rs = db.query(stmt);
		rs.next();
		int wins = rs.getInt("wins");
		rs.close();
		return wins;
	}
}
