package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSMatch;
import model.BSRobotType;
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
		printArray(out, "aTotalRobots", result.getaResult().getTotalRobots());
		printArray(out, "aActiveRobots", result.getaResult().getActiveRobots());
		for (int i = 0; i < BSRobotType.values().length; i++) {
			printArray(out, "aRobots" + i, result.getaResult().getRobotsByType()[i]);
			printArray(out, "aActiveRobots" + i, result.getaResult().getActiveRobotsByType()[i]);
		}
		printArray(out, "aFluxMsg", result.getaResult().getFluxSpentOnMessaging());
		printArray(out, "aFluxSpawn", result.getaResult().getFluxSpentOnSpawning());
		printArray(out, "aFluxMove", result.getaResult().getFluxSpentOnMoving());
		printArray(out, "aFluxUpkeep", result.getaResult().getFluxSpentOnUpkeep());

		printArray(out, "bTotalRobots", result.getbResult().getTotalRobots());
		printArray(out, "bActiveRobots", result.getbResult().getActiveRobots());
		for (int i = 0; i < BSRobotType.values().length; i++) {
			printArray(out, "bRobots" + i, result.getbResult().getRobotsByType()[i]);
			printArray(out, "bActiveRobots" + i, result.getbResult().getActiveRobotsByType()[i]);
		}
		printArray(out, "bFluxMsg", result.getbResult().getFluxSpentOnMessaging());
		printArray(out, "bFluxSpawn", result.getbResult().getFluxSpentOnSpawning());
		printArray(out, "bFluxMove", result.getbResult().getFluxSpentOnMoving());
		printArray(out, "bFluxUpkeep", result.getbResult().getFluxSpentOnUpkeep());
		out.print("<script type='text/javascript'>" +
				"var rounds = " + result.getRounds() + 
				"</script>");

		// TODO: aggregate view for some stats
		out.println("<div id='buttonWrapper' style='height:70px; text-align:center'>");
		out.println("<div id='aViewButtons' style='margin-left:15px; float:left'>" +
				"<input type='radio' id='aTotalRobots' name='aTotalRobots' /><label for='aTotalRobots'>All Robots</label>");
		for (int i = 0; i < BSRobotType.values().length; i++) {
			out.println("<input type='radio' id='aRobots" + i + "' name='aRobots" + i + "' /><label for='aRobots" + i + "'>" + BSRobotType.values()[i] + "s</label>");
			out.println("<input type='radio' id='aActiveRobots" + i + "' name='aActiveRobots" + i + "' /><label for='aActiveRobots" + i + "'>Active " + BSRobotType.values()[i] + "s</label>");
		}
		out.println("<input type='radio' id='aFluxMsg' name='aFluxMsg' /><label for='aFluxMsg'>Flux (messaging)</label>" +
				"<input type='radio' id='aFluxSpawn' name='aFluxSpawn' /><label for='aFluxSpawn'>Flux (spawning)</label>" +
				"<input type='radio' id='aFluxMove' name='aFluxMove' /><label for='aFluxMove'>Flux (moving)</label>" +
				"<input type='radio' id='aFluxUpkeep' name='aFluxUpkeep' /><label for='aFluxUpkeep'>Flux (upkeep)</label>" +
				"</div>");
		
		out.println("<div id='bViewButtons' style='margin-left:20px; float:right'>" +
				"<input type='radio' id='bTotalRobots' name='bTotalRobots' /><label for='bTotalRobots'>Robots</label>");
		for (int i = 0; i < BSRobotType.values().length; i++) {
			out.println("<input type='radio' id='bRobots" + i + "' name='bRobots" + i + "' /><label for='bRobots" + i + "'>" + BSRobotType.values()[i] + "s</label>");
			out.println("<input type='radio' id='bActiveRobots" + i + "' name='bActiveRobots" + i + "' /><label for='bActiveRobots" + i + "'>Active " + BSRobotType.values()[i] + "s</label>");
		}
		out.println("<input type='radio' id='bFluxMsg' name='bFluxMsg' /><label for='bFluxMsg'>Flux (messaging)</label>" +
				"<input type='radio' id='bFluxSpawn' name='bFluxSpawn' /><label for='bFluxSpawn'>Flux (spawning)</label>" +
				"<input type='radio' id='bFluxMove' name='bFluxMove' /><label for='bFluxMove'>Flux (moving)</label>" +
				"<input type='radio' id='bFluxUpkeep' name='bFluxUpkeep' /><label for='bFluxUpkeep'>Flux (upkeep)</label>" +
				"</div>");
		out.println("<button id='resetZoom' style='margin-top:10px'>Reset zoom</button>");
		out.println("</div>");
		out.println("<div id='chart' style='height: 400px; width:100%'></div>");


		out.println("<script src='js/analysis.js'></script>");
		out.println("</body></html>");
	}

	private static void printArray(PrintWriter out, String name, Object[] array) {
		out.print("<script type='text/javascript'>" +
				"dataMap['" + name + "'] = [");
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				out.print("[" + i + "," + array[i] + "],");
			}
		} else {
			out.print("[0,0],");
		}
		out.println("];" +
				"</script>");
	}

}
