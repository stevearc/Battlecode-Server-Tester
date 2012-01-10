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
		for (BSRobotType type: BSRobotType.values()) {
//			printArray(out, "aRobots" + i, result.getaResult().getRobotsByType()[i]);
			printArray(out, "aActiveRobots" + type, result.getaResult().getActiveRobotsByType()[type.ordinal()]);
		}
		printArray(out, "aFluxSpawn", result.getaResult().getFluxSpentOnSpawning());
		printArray(out, "aFluxMove", result.getaResult().getFluxSpentOnMoving());
		printArray(out, "aFluxUpkeep", result.getaResult().getFluxSpentOnUpkeep());

		printArray(out, "bTotalRobots", result.getbResult().getTotalRobots());
		printArray(out, "bActiveRobots", result.getbResult().getActiveRobots());
		for (BSRobotType type: BSRobotType.values()) {
//			printArray(out, "bRobots" + i, result.getbResult().getRobotsByType()[i]);
			printArray(out, "bActiveRobots" + type, result.getbResult().getActiveRobotsByType()[type.ordinal()]);
		}
		printArray(out, "bFluxSpawn", result.getbResult().getFluxSpentOnSpawning());
		printArray(out, "bFluxMove", result.getbResult().getFluxSpentOnMoving());
		printArray(out, "bFluxUpkeep", result.getbResult().getFluxSpentOnUpkeep());

		out.println("<script type='text/javascript'>" +
				"var nameMap = {" +
				"aTotalRobots: 'A: Total Robots'," +
				"aActiveRobots: 'A: Active Robots'," +
				"aFluxSpawn: 'A: Flux Spent (spawning)'," +
				"aFluxMove: 'A: Flux Spent (moving)'," +
				"aFluxUpkeep: 'A: Flux Spent (upkeep)'," +
				"aActiveRobots" + BSRobotType.ARCHON + ": 'A: Active " + BSRobotType.ARCHON + "s'," +
				"aActiveRobots" + BSRobotType.SOLDIER + ": 'A: Active " + BSRobotType.SOLDIER + "s'," +
				"aActiveRobots" + BSRobotType.SCOUT + ": 'A: Active " + BSRobotType.SCOUT + "s'," +
				"aActiveRobots" + BSRobotType.DISRUPTER + ": 'A: Active " + BSRobotType.DISRUPTER + "s'," +
				"aActiveRobots" + BSRobotType.SCORCHER + ": 'A: Active " + BSRobotType.SCORCHER + "s'," +
				"aActiveRobots" + BSRobotType.TOWER + ": 'A: Active " + BSRobotType.TOWER + "s'," +
				"" +
				"bTotalRobots: 'B: Total Robots'," +
				"bActiveRobots: 'B: Active Robots'," +
				"bFluxSpawn: 'B: Flux Spent (spawning)'," +
				"bFluxMove: 'B: Flux Spent (moving)'," +
				"bFluxUpkeep: 'B: Flux Spent (upkeep)'," +
				"bActiveRobots" + BSRobotType.ARCHON + ": 'B: Active " + BSRobotType.ARCHON + "s'," +
				"bActiveRobots" + BSRobotType.SOLDIER + ": 'B: Active " + BSRobotType.SOLDIER + "s'," +
				"bActiveRobots" + BSRobotType.SCOUT + ": 'B: Active " + BSRobotType.SCOUT + "s'," +
				"bActiveRobots" + BSRobotType.DISRUPTER + ": 'B: Active " + BSRobotType.DISRUPTER + "s'," +
				"bActiveRobots" + BSRobotType.SCORCHER + ": 'B: Active " + BSRobotType.SCORCHER + "s'," +
				"bActiveRobots" + BSRobotType.TOWER + ": 'B: Active " + BSRobotType.TOWER + "s'," +
				"};" +
				"</script>");
		out.print("<script type='text/javascript'>" +
				"var rounds = " + result.getRounds() + 
				"</script>");

		// TODO: aggregate view for some stats
		out.println("<div id='buttonWrapper' style='height:70px; text-align:center'>");
		out.println("<div id='aViewButtons' style='margin-left:15px; float:left'>" +
				"<div>" +
				"<input type='radio' id='aTotalRobots' name='aTotalRobots' /><label for='aTotalRobots'>All Robots</label>" + 
				"<input type='radio' id='aActiveRobots' name='aActiveRobots' /><label for='aActiveRobots'>All Active Robots</label>" +
				"</div>");
		out.println("<div>");
		int index = 0;
		for (BSRobotType type: BSRobotType.values()) {
//			out.println("<input type='radio' id='aRobots" + i + "' name='aRobots" + i + "' /><label for='aRobots" + i + "'>" + BSRobotType.values()[i] + "s</label>");
			out.println("<input type='radio' id='aActiveRobots" + type + "' name='aActiveRobots" + type + "' />" +
					"<label for='aActiveRobots" + type + "'>Active " + BSRobotType.values()[type.ordinal()] + "s</label>");
			if (++index % 3 == 0) {
				out.println("</div><div>");
			}
		}
		out.println("</div>");
		out.println("<div><span style='font-weight:bold; margin-right:10px'>Flux Spent:</span>" +
				"<input type='radio' id='aFluxSpawn' name='aFluxSpawn' /><label for='aFluxSpawn'>Spawning</label>" +
				"<input type='radio' id='aFluxMove' name='aFluxMove' /><label for='aFluxMove'>Moving</label>" +
				"<input type='radio' id='aFluxUpkeep' name='aFluxUpkeep' /><label for='aFluxUpkeep'>Upkeep</label>");
		out.println("</div>" +
				"</div>");
		
		out.println("<div id='bViewButtons' style='margin-left:20px; float:right'>" +
				"<div>" +
				"<input type='radio' id='bTotalRobots' name='bTotalRobots' /><label for='bTotalRobots'>All Robots</label>" + 
				"<input type='radio' id='bActiveRobots' name='bActiveRobots' /><label for='bActiveRobots'>All Active Robots</label>" +
				"</div>");
		out.println("<div>");
		for (BSRobotType type: BSRobotType.values()) {
//			out.println("<input type='radio' id='bRobots" + i + "' name='bRobots" + i + "' /><label for='bRobots" + i + "'>" + BSRobotType.values()[i] + "s</label>");
			out.println("<input type='radio' id='bActiveRobots" + type + "' name='bActiveRobots" + type + "' />" +
					"<label for='bActiveRobots" + type + "'>Active " + BSRobotType.values()[type.ordinal()] + "s</label>");
			if (++index % 3 == 0) {
				out.println("</div><div>");
			}
		}
		out.println("</div>");
		out.println("<div><span style='font-weight:bold; margin-right:10px'>Flux Spent:</span>" +
				"<input type='radio' id='bFluxSpawn' name='bFluxSpawn' /><label for='bFluxSpawn'>Spawning</label>" +
				"<input type='radio' id='bFluxMove' name='bFluxMove' /><label for='bFluxMove'>Moving</label>" +
				"<input type='radio' id='bFluxUpkeep' name='bFluxUpkeep' /><label for='bFluxUpkeep'>Upkeep</label>");
		out.println("</div>" +
				"</div>");
		out.println("<button id='resetZoom' style='margin-top:10px'>Reset zoom</button>");
		out.println("</div>");
		out.println("<div id='chart' style='height: 400px; width:100%; top:100px'></div>");
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
