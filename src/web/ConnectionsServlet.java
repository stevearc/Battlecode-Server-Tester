package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import master.AbstractMaster;
import master.WorkerRepr;
import model.BSScrimmageSet;

import common.NetworkMatch;

/**
 * Display current connected workers
 * @author stevearc
 *
 */
public class ConnectionsServlet extends HttpServlet {
	private static final long serialVersionUID = 2147508188812654640L;
	public static final String NAME = "/connections.html";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/table.css\" />");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(request, response, NAME);
		out.println("<script src='js/jquery.dataTables.min.js'></script>");

		out.println("<table id=\"conn_table\" class='datatable'>" +
		"<thead>");
		out.println("<tr>" +
				"<th>Worker</th>" +
				"<th>Maps</th>" +
				"<th></th>" +
		"</tr>");
		out.println("</thead>");
		out.println("<tbody>");
		for (WorkerRepr c: AbstractMaster.getConnectionsStatic()) {
			out.println("<tr>");
			out.println("<td>" + c.toHTML() + "</td>");
			out.print("<td>");
			StringBuilder sb = new StringBuilder();
			for (NetworkMatch m: c.getRunningMatches()) {
				sb.append(m.toMapString() + ", ");
			}
			for (BSScrimmageSet s: c.getAnalyzingMatches()) {
				sb.append(s.getFileName() + ", ");
			}
			if (sb.length() > 0) 
				out.print(sb.substring(0, sb.length() - 2));
			else
				out.print("&nbsp;");
			out.println("</td>");
			out.println("<td><input type='button' onClick='restart(" + c.getId() + ")' value='Force Restart' /></td>");
			out.println("</tr>");
		}
		out.println("</tbody>");
		out.println("</table>");

		out.println("<script type=\"text/javascript\" src=\"js/bsUtil.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/connections.js\"></script>");
		out.println("</body>" +
		"</html>");
	}
	
}
