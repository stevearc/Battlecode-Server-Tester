package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.ClientRepr;

import common.Config;
import common.Match;

public class ConnectionsServlet extends AbstractServlet {
	private static final long serialVersionUID = 2147508188812654640L;
	public static final String NAME = "connections.html";

	public ConnectionsServlet() {
		super(NAME);
	}

	@Override
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

		out.println("<div id=\"tablewrapper\">");
		out.println("<div id=\"tableheader\">" +
				"<div class=\"search\">" +
		"<select id=\"coldid\" onchange=\"conn_sorter.search('query')\"></select>");
		out.println("<input type=\"text\" id=\"query\" onkeyup=\"conn_sorter.search('query')\" />");
		out.println("</div>");
		out.println("<span class=\"details\">" +
				"<div>Records <span id=\"startrecord\"></span>-<span id=\"endrecord\"></span> of " +
		"<span id=\"totalrecords\"></span></div>");
		out.println("<div><a href=\"javascript:conn_sorter.reset()\">reset</a></div>" +
		"</span>");
		out.println("</div>");
		out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" id=\"conn_table\" class=\"tinytable\">" +
		"<thead>");
		out.println("<tr>" +
				"<th class='desc'><h3>Client</h3></th>" +
				"<th class='desc'><h3>Map</h3></th>" +
		"</tr>");
		out.println("</thead>");
		out.println("<tbody>");
		for (ClientRepr c: Config.getServer().getConnections()) {
			out.println("<tr>");
			out.println("<td>" + c.toHTML() + "</td>");
			out.println("<td>&nbsp;");
			StringBuilder sb = new StringBuilder();
			for (Match m: c.getRunningMatches()) {
				sb.append(m.toMapString() + ", ");
			}
			if (sb.length() > 2)
			out.println(sb.substring(0, sb.length() - 2));
			out.println("</td>");
			out.println("</tr>");
		}
		out.println("</tbody>");
		out.println("</table>");
		out.println("<div id=\"tablefooter\">");
		out.println("<div id=\"tablenav\">");
		out.println("<div>");
		out.println("<img src=\"images/first.gif\" width=\"16\" height=\"16\" alt=\"First Page\" " +
		"onclick=\"conn_sorter.move(-1,true)\" />");
		out.println("<img src=\"images/previous.gif\" width=\"16\" height=\"16\" alt=\"Previous Page\" " +
		"onclick=\"conn_sorter.move(-1)\" />");
		out.println("<img src=\"images/next.gif\" width=\"16\" height=\"16\" alt=\"Next Page\" " +
		"onclick=\"conn_sorter.move(1)\" />");
		out.println("<img src=\"images/last.gif\" width=\"16\" height=\"16\" alt=\"Last Page\" " +
		"onclick=\"conn_sorter.move(1,true)\" />");
		out.println("</div>");
		out.println("<div>");
		out.println("<select id=\"pagedropdown\"></select>");
		out.println("</div>");
		out.println("<div>");
		out.println("<a href=\"javascript:conn_sorter.showall()\">view all</a>");
		out.println("</div>");
		out.println("</div>");
		out.println("<div id=\"tablelocation\">");
		out.println("<div>");
		out.println("<select onchange=\"conn_sorter.size(this.value)\">");
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
		out.println("<script type=\"text/javascript\" src=\"js/connections_init_table.js\"></script>");
		out.println("</body>" +
		"</html>");
	}
}
