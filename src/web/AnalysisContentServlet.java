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
import model.MatchResult;
import model.ScrimmageMatchResult;

import common.HibernateUtil;

/**
 * This is viewed in an iframe inside of the MatchesServlet
 * @author stevearc
 *
 */
public class AnalysisContentServlet extends HttpServlet {
	private static final long serialVersionUID = -3373145024382759806L;
	public static final String NAME = "/analysis_content.html";
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<script src='/js/jquery-1.7.1.min.js'></script>");
		out.println("<script src='/js/jquery-ui-1.8.16.custom.min.js'></script>");
		out.println("<link rel='stylesheet' href='/css/jquery-ui-1.8.16.custom.css' />");
		out.println("<link rel='stylesheet' href='/css/jquery-ui.css' />");
		out.println("</head>");
		out.println("<body style='background:none'>");
		String strId = request.getParameter("id");
		if (strId == null || !strId.matches("\\d+")) {
			out.println("Invalid id</body></html>");
			return;
		}
		long id = new Long(Integer.parseInt(strId));
		EntityManager em = HibernateUtil.getEntityManager();
		if (request.getParameter("scrimmage") != null) {
			ScrimmageMatchResult result = em.find(ScrimmageMatchResult.class, id);
			printContent(request, response, result, result.getMap(), null);
		} else {
			BSMatch match = em.find(BSMatch.class, id);
			printContent(request, response, match.getResult(), match.getMap().getMapName(), ""+match.getSeed());
		}
	}

	public static void printContent(HttpServletRequest request, HttpServletResponse response, MatchResult result, String mapName, String seed) throws ServletException, IOException {
		BSUser user = (BSUser) request.getSession().getAttribute("user");
		PrintWriter out = response.getWriter();
		out.println("<link rel=\"stylesheet\" href=\"css/jquery.jqplot.min.css\" />");
		out.println("<script src='js/jquery.jqplot.min.js'></script>");
		out.println("<script src='js/jqplot.cursor.min.js'></script>");
		out.println(user.getPrefs().toJavascript());

		out.println("<h2 style='text-align:center'>" + mapName + (seed != null ? " (" + seed + ")" : "") + "</h2>");
		
		out.println("<script type='text/javascript'>" +
				"var dataMap = [];" +
				"</script>");
		printArray(out, "aFluxIncome", result.getaResult().getFluxIncome());
		printArray(out, "aFluxDrain", result.getaResult().getFluxDrain());
		printArray(out, "aFluxReserve", result.getaResult().getFluxReserve());
		printArray(out, "aActiveRobots", result.getaResult().getActiveRobots());
		
		printArray(out, "bFluxIncome", result.getbResult().getFluxIncome());
		printArray(out, "bFluxDrain", result.getbResult().getFluxDrain());
		printArray(out, "bFluxReserve", result.getbResult().getFluxReserve());
		printArray(out, "bActiveRobots", result.getbResult().getActiveRobots());
		out.print("<script type='text/javascript'>" +
				"var rounds = " + result.getRounds() + 
				"</script>");
		
		out.println("<div id='buttonWrapper' style='height:70px; text-align:center'>");
		out.println("<div id='aViewButtons' style='margin-left:15px; float:left'>" +
				"<input type='radio' id='aFluxIncome' name='aFluxIncome' /><label for='aFluxIncome'>Flux Income</label>" +
				"<input type='radio' id='aFluxDrain' name='aFluxDrain' /><label for='aFluxDrain'>Flux Drain</label>" +
				"<input type='radio' id='aFluxReserve' name='aFluxReserve' /><label for='aFluxReserve'>Flux Reserve</label>" +
				"<input type='radio' id='aActiveRobots' name='aActiveRobots' /><label for='aActiveRobots'>Active Robots</label>" +
				" </div>");
		out.println("<div id='bViewButtons' style='margin-left:20px; float:right'>" +
				"<input type='radio' id='bFluxIncome' name='bFluxIncome' /><label for='bFluxIncome'>Flux Income</label>" +
				"<input type='radio' id='bFluxDrain' name='bFluxDrain' /><label for='bFluxDrain'>Flux Drain</label>" +
				"<input type='radio' id='bFluxReserve' name='bFluxReserve' /><label for='bFluxReserve'>Flux Reserve</label>" +
				"<input type='radio' id='bActiveRobots' name='bActiveRobots' /><label for='bActiveRobots'>Active Robots</label>" +
				" </div>");
		out.println("<button id='resetZoom' style='margin-top:10px'>Reset zoom</button>");
		out.println("</div>");
		out.println("<div id='chart' style='height: 400px; width:100%'></div>");
		
		
		out.println("<script src='js/analysis.js'></script>");
		out.println("</body></html>");
	}
	
	private static void printArray(PrintWriter out, String name, Object[] array) {
		out.print("<script type='text/javascript'>" +
				"dataMap['" + name + "'] = [");
		for (int i = 0; i < array.length; i++) {
			out.print("[" + i + "," + array[i] + "],");
		}
		out.println("];" +
				"</script>");
	}
	
}
