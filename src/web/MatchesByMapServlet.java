package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSMap;
import model.BSMatch;
import model.BSRun;
import dataAccess.HibernateUtil;

/**
 * Displays matches for a given run aggregated by map
 * @author stevearc
 *
 */
public class MatchesByMapServlet extends HttpServlet {
	private static final long serialVersionUID = 5352256649398067730L;
	public static final String NAME = "matches_by_map.html";
	
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

		WebUtil.writeTabs(response, out, toString());

		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id</body></html>");
			return;
		}
		Long id = new Long(Integer.parseInt(strId));
		EntityManager em = HibernateUtil.getEntityManager();
		BSRun run = em.find(BSRun.class, id);
			out.println("<div id=\"tablewrapper\">");
			out.println("<h2><font color='red'>" + run.getTeamA().getPlayerName() + "</font> vs. <font color='blue'>" + 
					run.getTeamB().getPlayerName() + "</font></h2>");
			out.println("<h3>" + WebUtil.getFormattedMapResults(WebUtil.getMapResults(run, null, false)) + "</h3>");
			out.println("<br />");
			out.println("<div class='tabbutton'>");
			out.println("<a onClick='document.location=\"" + response.encodeURL(MatchesServlet.NAME) + "?id=" + id + "\"' " +
			"style='cursor:pointer;'><span>View by match</span></a>");
			out.println("<p>&nbsp;</p>");
			out.println("<p>&nbsp;</p>");
			out.println("<p>&nbsp;</p>");
			out.println("</div>");

			WebUtil.printTableHeader(out, "matches_sorter");
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" id=\"matches_table\" class=\"tinytable\">" +
					"<thead>" + 
					"<tr>" +
					"<th class='desc'><h3>Map</h3></th>" +
					"<th class='desc'><h3>Win %</h3></th>" +
					"<th class='desc'><h3>Size</h3></th>" +
					"</tr>" +
					"</thead>" +
			"<tbody>");
			List<Object[]> valuePairs = em.createQuery("select map, match.winner, count(*) from BSMatch match inner join match.map as map where match.run = ?" +
					"and match.status = ? group by map, match.winner order by map, match.winner", Object[].class)
					.setParameter(1, run)
					.setParameter(2, BSMatch.STATUS.FINISHED)
					.getResultList();
			for (int i = 0; i < valuePairs.size(); i++) {
				Object[] valuePair = valuePairs.get(i);
				BSMap map = (BSMap) valuePair[0];
				long aCount = (Long) valuePair[2];
				long bCount = 0;
				if (i + 1 < valuePairs.size() && map.equals(valuePairs.get(i+1)[0])) {
					bCount = (Long) valuePairs.get(i+1)[2];
					i++;
				}
				out.println("<tr>");
				out.println("<td>" + map.getMapName() + "</td>");
				out.println("<td>" + WebUtil.getFormattedWinPercentage((double)aCount/(aCount+bCount)) + "</td>");
				out.println("<td>" + map.getSize() + "</td>");
				out.println("</tr>");
			}
			out.println("</tbody>");
			out.println("</table>");
			WebUtil.printTableFooter(out, "matches_sorter");
			out.println("</div>");

			out.println("<script type=\"text/javascript\" src=\"js/script.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"js/matches_init_table.js\"></script>");
			out.println("</body></html>");
	}
	
	@Override
	public String toString() {
		return NAME;
	}
}
