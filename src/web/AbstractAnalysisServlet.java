package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSPlayer;
import dataAccess.HibernateUtil;

/**
 * Framework for one format of analysis on the match data
 * @author stevearc
 *
 */
public abstract class AbstractAnalysisServlet extends HttpServlet {
	private static final long serialVersionUID = 5929611248935884353L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String version = request.getParameter("version");
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/tinytable.css\" />");
		out.println("<link rel=\"stylesheet\" href=\"css/tabs.css\" />");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(response, out, toString());

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
		EntityManager em = HibernateUtil.getEntityManager();
		List<BSPlayer> players = em.createQuery("from BSPlayer player order by player.playerName", BSPlayer.class).getResultList();
		BSPlayer currentPlayer;
		if (!players.isEmpty()) {
			if (version == null) {
				currentPlayer = players.get(0);
			} else {
				List<BSPlayer> candidates = em.createQuery("from BSPlayer player where player.playerName = ?", BSPlayer.class)
				.setParameter(1, version)
				.getResultList();
				if (candidates.size() == 0) {
					currentPlayer = players.get(0);
				} else {
					currentPlayer = candidates.get(0);
				}
			}
			out.println("<div id=\"tablewrapper\">");
			out.println("<p>" +
					"<select id='selector' name='version' onChange='doGet(document.getElementById(\"selector\").value)' " +
			"style='margin:0 auto;'>");
			for (BSPlayer player: players) {
				out.println("<option name='" + player.getPlayerName() + "'>" + player.getPlayerName() + "</option>");
			}
			out.println("</select>");
			out.println("</p><br />");
			out.println("<script type=\"text/Javascript\">" +
					"document.getElementById('selector').selectedIndex=" + players.indexOf(currentPlayer) + 
			"</script>");
			out.println("<h1><font color='blue'>" + currentPlayer.getPlayerName() + "</font></h1>");

			WebUtil.printTableHeader(out, "analysis_sorter");
			out.println("<table id=\"analysis_table\" class=\"tinytable\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
			out.println("<thead>");
			writeTableHead(out);
			out.println("</thead>");
			out.println("<tbody>");
			writeTable(out, currentPlayer);
			out.println("</tbody>");
			out.println("</table>");
			WebUtil.printTableFooter(out, "analysis_sorter");
			out.println("</div>");
		}
		em.close();

		out.println("<script type=\"text/javascript\" src=\"js/script.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/analysis_init_table.js\"></script>");
		out.println("</body></html>");
	}
	
	protected abstract void writeTable(PrintWriter out, BSPlayer currentPlayer);

	protected abstract void writeTableHead(PrintWriter out);

}
