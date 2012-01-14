package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

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
			if (result == null) {
				out.println("Invalid id</body></html>");
				return;
			}
			printContent(request, response, result, result.getMap(), null);
		} else {
			BSMatch match = em.find(BSMatch.class, id);
			if (match == null) {
				out.println("Invalid id</body></html>");
				return;
			}
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
		printArray(out, "aFluxGained", result.getaResult().getTotalFluxGathered());

		printArray(out, "bTotalRobots", result.getbResult().getTotalRobots());
		printArray(out, "bActiveRobots", result.getbResult().getActiveRobots());
		for (BSRobotType type: BSRobotType.values()) {
			//			printArray(out, "bRobots" + i, result.getbResult().getRobotsByType()[i]);
			printArray(out, "bActiveRobots" + type, result.getbResult().getActiveRobotsByType()[type.ordinal()]);
		}
		printArray(out, "bFluxSpawn", result.getbResult().getFluxSpentOnSpawning());
		printArray(out, "bFluxMove", result.getbResult().getFluxSpentOnMoving());
		printArray(out, "bFluxUpkeep", result.getbResult().getFluxSpentOnUpkeep());
		printArray(out, "bFluxGained", result.getbResult().getTotalFluxGathered());
		
		
		ArrayList<String[]> selections = new ArrayList<String[]>();
		selections.add(new String[] {"TotalRobots", "Total Robots"});
		selections.add(new String[] {"ActiveRobots", "Active Robots"});
		for (BSRobotType bsrt: BSRobotType.values()) {
			selections.add(new String[] {"ActiveRobots" + bsrt, "Active " + bsrt + "s"});
		};
		selections.add(new String[] {"FluxGained", "Total Flux Gained"});
		selections.add(new String[] {"FluxSpawn", "Flux Spent (spawning)"});
		selections.add(new String[] {"FluxMove", "Flux Spent (moving)"});
		selections.add(new String[] {"FluxUpkeep", "Flux Spent (upkeep)"});

		out.println("<script type='text/javascript'>" +
		"var nameMap = {");
		for (String[] keyVal: selections) {
			out.println("a" + keyVal[0] + ": 'A: " + keyVal[1] + "',");
			out.println("b" + keyVal[0] + ": 'B: " + keyVal[1] + "',");
		}
		out.println("};" +
				"var rounds = " + result.getRounds() + 
		"</script>");

		out.println("<div id='buttonWrapper' style='height:70px; text-align:center'>");
		out.println("<div id='aViewButtons' style='margin-left:15px; float:left'>");
		out.println("<div>");
		for (int i = 0; i < 4; i++) {
			out.println("<select onChange='updateChart()'>");
			out.println("<option name='None'>None</option>");
			for (String[] keyVal: selections) {
				out.println("<option name='a" + keyVal[0] + "'>" + keyVal[1] + "</option>");
			}
			out.println("</select>");
			if (i % 2 == 1) {
				out.println("</div><div>");
			}
		}
		out.println("</div>");
		out.println("</div>");

		out.println("<div id='bViewButtons' style='margin-left:20px; float:right'>");
		out.println("<div>");
		for (int i = 0; i < 4; i++) {
			out.println("<select onChange='updateChart()'>");
			out.println("<option name='None'>None</option>");
			for (String[] keyVal: selections) {
				out.println("<option name='b" + keyVal[0] + "'>" + keyVal[1] + "</option>");
			}
			out.println("</select>");
			if (i % 2 == 1) {
				out.println("</div><div>");
			}
		}
		out.println("</div>");
		out.println("</div>");
		out.println("</div>");
		out.println("<div style='text-align:center'>");
		out.println("<button id='resetZoom' style='margin-top:10px'>Reset zoom</button>");
		out.println("</div>");
		out.println("<div id='chart' style='height: 400px; width:100%; top:0px'></div>");
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
