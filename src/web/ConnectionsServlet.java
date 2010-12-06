package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import backend.ClientRepr;

import common.Config;
import common.Match;

/**
 * Display current connected clients
 * @author stevearc
 *
 */
public class ConnectionsServlet extends AbstractServlet {
	private static final long serialVersionUID = 2147508188812654640L;
	public static final String NAME = "connections.html";

	public ConnectionsServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = checkLogin(request, response);
		if (username == null){
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
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(response, out, name);

		out.println("<div id=\"tablewrapper\">");
		WebUtil.printTableHeader(out, "conn_sorter");
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
			out.print("<td>");
			StringBuilder sb = new StringBuilder();
			for (Match m: c.getRunningMatches()) {
				sb.append(m.toMapString() + ", ");
			}
			if (sb.length() > 2) 
				out.print(sb.substring(0, sb.length() - 2));
			else
				out.print("&nbsp;");
			out.println("</td>");
			out.println("</tr>");
		}
		out.println("</tbody>");
		out.println("</table>");
		WebUtil.printTableFooter(out, "conn_sorter");
		out.println("</div>");

		out.println("<script type=\"text/javascript\" src=\"js/script.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/connections_init_table.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/async.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/connections.js\"></script>");
		out.println("</body>" +
		"</html>");
	}
}
