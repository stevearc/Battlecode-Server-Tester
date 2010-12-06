package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Config;

/**
 * Framework for one format of analysis on the match data
 * @author stevearc
 *
 */
public abstract class AbstractAnalysisServlet extends AbstractServlet {
	private static final long serialVersionUID = 5929611248935884353L;

	public AbstractAnalysisServlet(String name) {
		super(name);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = checkLogin(request, response);
		if (username == null) {
			redirect(response);
			return;
		}
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

			WebUtil.writeTabs(response, out, name);

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
				PreparedStatement st = db.prepare("SELECT * FROM tags WHERE tag LIKE ? OR tag LIKE ?");
				st.setString(1, r.getString("team_a"));
				st.setString(2, r.getString("team_b"));
				ResultSet nameSet = db.query(st);
				while (nameSet.next()) {
					String name = nameSet.getString("alias");
					name = (name == null ? nameSet.getString("tag") : name);
					teams.add(name);
				}
			}
			String[] teamArray = teams.toArray(new String[teams.size()]);
			if (teamArray.length > 0) {
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
				out.println("<h1><font color='blue'>" + version + "</font></h1>");

				PreparedStatement stmt = db.prepare("SELECT id, team_a, team_b, t1.alias a_nick, t2.alias b_nick FROM runs r " +
						"LEFT JOIN tags t1 ON r.team_a = t1.tag " +
				"LEFT JOIN tags t2 ON r.team_b = t2.tag WHERE (r.team_a LIKE ? OR r.team_b LIKE ? OR t1.alias LIKE ? OR t2.alias LIKE ?) AND status = " + Config.STATUS_COMPLETE);
				stmt.setString(1, version);
				stmt.setString(2, version);
				stmt.setString(3, version);
				stmt.setString(4, version);
				ResultSet rs = db.query(stmt);

				WebUtil.printTableHeader(out, "analysis_sorter");
				out.println("<table id=\"analysis_table\" class=\"tinytable\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
				out.println("<thead>");
				writeTableHead(out);
				out.println("</thead>");
				out.println("<tbody>");

				while (rs.next()) {
					int runid = rs.getInt("id");
					String team_a = rs.getString("a_nick");
					String team_a_raw = (team_a == null ? rs.getString("team_a") : team_a);
					team_a = (team_a == null ? rs.getString("team_a") : "<a title='" + rs.getString("team_a") + "'>" + team_a + "</a>");
					String team_b = rs.getString("b_nick");
					team_b = (team_b == null ? rs.getString("team_b") : "<a title='" + rs.getString("team_b") + "'>" + team_b + "</a>");
					boolean reverse = team_a_raw.equals(version);
					String row_team = (reverse ? team_b : team_a);
					writeTableRow(out, runid, row_team, reverse);
				}
				rs.close();
				out.println("</tbody>");
				out.println("</table>");
				WebUtil.printTableFooter(out, "analysis_sorter");
				out.println("</div>");
			}

			out.println("<script type=\"text/javascript\" src=\"js/script.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"js/analysis_init_table.js\"></script>");
			out.println("</body></html>");
		} catch (Exception e) {
			e.printStackTrace(out);
		}
	}

	protected abstract void writeTableHead(PrintWriter out);

	protected abstract void writeTableRow(PrintWriter out, int runid, String row_team, boolean reverse) throws Exception;

}
