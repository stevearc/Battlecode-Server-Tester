package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSRun;

import common.HibernateUtil;

public class MatchWrapperServlet extends HttpServlet {
	private static final long serialVersionUID = 3976005642496907337L;
	public static final String NAME = "/matches.html";

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
		
		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id</body></html>");
			return;
		}
		long id = new Long(Integer.parseInt(strId));
		out.println("<span id='match_id' style='display:none'>" + strId + "</span>");
		EntityManager em = HibernateUtil.getEntityManager();
		BSRun run = em.find(BSRun.class, id);
		out.println("<h2 style='text-align:center'><font color='red'>" + run.getTeamA().getPlayerName() + 
				"</font> vs. <font color='blue'>" + run.getTeamB().getPlayerName() + "</font></h2>");
		out.println("<h3 style='text-align:center'>Wins by map: " + WebUtil.getFormattedMapResults(WebUtil.getMapResults(run, null, false)) + "</h3>");
		out.println("<br />");
		out.println("<div id='viewStyle' style='margin-left:20px'>" +
				"<input type='radio' id='byMatch' name='byMatch' checked='checked' /><label for='byMatch'>By Match</label>" +
				"<input type='radio' id='byMap' name='byMap' /><label for='byMap'>By Map</label>" +
				" </div>");
		out.println("<div id='individual_container'></div>");
		out.println("<div id='map_container'></div>");
		
		out.println("<script src='js/jquery.dataTables.min.js'></script>");
		out.println("<script type=\"text/javascript\" src=\"js/matches.js\"></script>");
		out.println("</body></html>");
	}
}
