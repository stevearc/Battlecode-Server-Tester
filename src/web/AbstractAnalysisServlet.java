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
			out.println("<h1><font color='blue'>" + version + "</font></h1>");

			PreparedStatement stmt = db.prepare("SELECT * FROM runs WHERE (team_a LIKE ? OR team_b LIKE ?) AND status = 2");
			stmt.setString(1, version);
			stmt.setString(2, version);
			ResultSet rs = db.query(stmt);

			out.println("<div id=\"tableheader\">" +
					"<div class=\"search\">" +
			"<select id=\"coldid\" onchange=\"analysis_sorter.search('query')\"></select>");
			out.println("<input type=\"text\" id=\"query\" onkeyup=\"analysis_sorter.search('query')\" />");
			out.println("</div>");
			out.println("<span class=\"details\">" +
					"<div>Records <span id=\"startrecord\"></span>-<span id=\"endrecord\"></span> of " +
			"<span id=\"totalrecords\"></span></div>");
			out.println("<div><a href=\"javascript:analysis_sorter.reset()\">reset</a></div>" +
			"</span>");
			out.println("</div>");
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
			out.println("<div id=\"tablefooter\">");
			out.println("<div id=\"tablenav\">");
			out.println("<div>");
			out.println("<img src=\"images/first.gif\" width=\"16\" height=\"16\" alt=\"First Page\" " +
			"onclick=\"analysis_sorter.move(-1,true)\" />");
			out.println("<img src=\"images/previous.gif\" width=\"16\" height=\"16\" alt=\"Previous Page\" " +
			"onclick=\"analysis_sorter.move(-1)\" />");
			out.println("<img src=\"images/next.gif\" width=\"16\" height=\"16\" alt=\"Next Page\" " +
			"onclick=\"analysis_sorter.move(1)\" />");
			out.println("<img src=\"images/last.gif\" width=\"16\" height=\"16\" alt=\"Last Page\" " +
			"onclick=\"analysis_sorter.move(1,true)\" />");
			out.println("</div>");
			out.println("<div>");
			out.println("<select id=\"pagedropdown\"></select>");
			out.println("</div>");
			out.println("<div>");
			out.println("<a href=\"javascript:analysis_sorter.showall()\">view all</a>");
			out.println("</div>");
			out.println("</div>");
			out.println("<div id=\"tablelocation\">");
			out.println("<div>");
			out.println("<select onchange=\"analysis_sorter.size(this.value)\">");
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
			out.println("<script type=\"text/javascript\" src=\"js/analysis_init_table.js\"></script>");
			out.println("</body></html>");
		} catch (Exception e) {
			e.printStackTrace(out);
		}
	}

	protected abstract void writeTableHead(PrintWriter out);

	protected abstract void writeTableRow(PrintWriter out, int runid, String row_team, boolean reverse) throws Exception;

}
