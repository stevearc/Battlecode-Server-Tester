package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSMatch;
import model.BSUser;
import model.ScrimmageMatchResult;

import common.HibernateUtil;

public class AnalysisServlet extends HttpServlet {
	private static final long serialVersionUID = 8020173508294249410L;
	public static final String NAME = "/analysis.html";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/jquery.jqplot.min.css\" />");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(request, response, NAME);
		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id</body></html>");
			return;
		}
		long id = new Long(Integer.parseInt(strId));
		EntityManager em = HibernateUtil.getEntityManager();
		if (request.getParameter("scrimmage") != null) {
			ScrimmageMatchResult result = em.find(ScrimmageMatchResult.class, id);
			out.println("<button id='back' style='margin-left:10px; margin-right:-70px; float:left' name='" + 
					response.encodeURL(ScrimmageViewServlet.NAME) + "?id=" + result.getScrimmageSet().getId() + "'>Back</button>");
			out.println("<h2 style='text-align:center'><font color='red'>" + result.getScrimmageSet().getPlayerA() + "</font> vs. <font color='blue'>" + 
					result.getScrimmageSet().getPlayerB() + "</font></h2>");
			AnalysisContentServlet.printContent(request, response, result, result.getMap(), null);
		} else {
			BSMatch match = em.find(BSMatch.class, id);
			out.println("<button id='back' style='margin-left:10px; margin-right:-70px; float:left' name='" + 
					response.encodeURL(MatchWrapperServlet.NAME) + "?id=" + match.getRun().getId() + "'>Back</button>");
			out.println("<h2 style='text-align:center'><font color='red'>" + match.getRun().getTeamA().getPlayerName() + "</font> vs. <font color='blue'>" + 
					match.getRun().getTeamB().getPlayerName() + "</font></h2>");
			AnalysisContentServlet.printContent(request, response, match.getResult(), match.getMap().getMapName(), ""+match.getSeed());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BSUser user = (BSUser) request.getSession().getAttribute("user");
		int[][] viewLines = user.getPrefs().getAnalysisViewLines();
		String[] segmentedViewLines = request.getParameter("viewLines").split(",");
		int index = 0;
		for (int i = 0; i < viewLines.length; i++) {
			for (int j = 0; j < viewLines[i].length; j++) {
				viewLines[i][j] = Integer.parseInt(segmentedViewLines[index++]);
			}
		}
	}
}
