package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import master.MasterMethodCaller;
import model.BSUser;


/**
 * Queue up a new run
 * @author stevearc
 *
 */
public class RunServlet extends AbstractServlet {
	private static final long serialVersionUID = -5024779464960322694L;
	public static final String NAME = "run.html";

	public RunServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BSUser user = checkLogin(request, response);
		if (user == null) {
			redirect(response);
			return;
		}
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String teamAId = request.getParameter("team_a");
		String teamBId = request.getParameter("team_b");
		String seeds = request.getParameter("seeds");
		String maps = request.getParameter("maps");
		if (teamAId == null || !teamAId.matches("-?\\d+")) {
			System.out.println("Error team A: " + teamAId);
			out.print("err team_a");
			return;
		} else if (teamBId == null || !teamBId.matches("-?\\d+")) {
			System.out.println("Error team B: " + teamBId);
			out.print("err team_b");
			return;
		} else if (!maps.matches("\\d+(,\\d+)*")) {
			System.out.println("Error map format: " + maps);
			out.print("err maps");
			return;
		} else if (!seeds.matches("\\d+(,\\d+)*")) {
			System.out.println("Error seed format: " + seeds);
			out.print("err seed");
			return;
		}
		System.out.println(teamAId + ", B:" + teamBId + ", maps:" + maps + ", seeds:" + seeds);
		Long teamAIdLong = new Long(Integer.parseInt(teamAId));
		Long teamBIdLong = new Long(Integer.parseInt(teamBId));
		ArrayList<Long> seedLongs = new ArrayList<Long>();
		for (String seed: seeds.split(",")) {
			seedLongs.add(new Long(Integer.parseInt(seed)));
		}
		ArrayList<Long> mapIds = new ArrayList<Long>();
		for (String mapId: maps.split(",")) {
			mapIds.add(new Long(Integer.parseInt(mapId)));
		}
		try {
			MasterMethodCaller.queueRun(teamAIdLong, teamBIdLong, seedLongs, mapIds);
			out.print("success");
		} catch (Exception e) {
			e.printStackTrace(out);
		}

	}
}
