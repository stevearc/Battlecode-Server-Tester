package web;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import common.BattlecodeMap;
import common.Config;

/**
 * View results of one team against all teams ever run against broken down by map size
 * @author stevearc
 *
 */
public class AnalysisServlet extends AbstractAnalysisServlet {
	public static final String NAME = "analysis.html";
	private static final long serialVersionUID = 5833958478703546254L;
	String[] keys;

	public AnalysisServlet(){
		super(NAME);
	}

	@Override
	protected void writeTableHead(PrintWriter out) {
		HashSet<String> maps = new HashSet<String>();
		for (BattlecodeMap m: Config.getMaster().getMaps()) {
			String size = m.getSizeClass();
			if (!maps.contains(size)) 
				maps.add(size);
		}
		out.println("<tr>" +
		"<th class='desc'><h3>Opponent</h3></th>");
		keys = maps.toArray(new String[maps.size()]);
		Arrays.sort(keys);
		for (String size: keys) {
			out.println("<th class='desc'><h3>" + size + "</h3></th>");
		}
		out.println("</tr>");
	}

	@Override
	protected void writeTableRow(PrintWriter out, int runid, String row_team, boolean reverse) throws Exception {
		out.println("<tr onClick='document.location=\"matches.html?id=" + runid + "\";' " +
		"style='cursor:pointer'>");
		out.println("<td><font color='red'>" + row_team + "</font></td>");
		HashMap<String, HashSet<String>> maps = new HashMap<String, HashSet<String>>();
		ResultSet rs = db.query("SELECT * FROM matches WHERE run_id = " + runid);
		while (rs.next()) {
			BattlecodeMap map = new BattlecodeMap(rs.getString("map"), rs.getInt("height"), rs.getInt("width"), 
					rs.getInt("rounds"), rs.getInt("points"));
			if (!maps.containsKey(map.getSizeClass()))
				maps.put(map.getSizeClass(), new HashSet<String>());
			maps.get(map.getSizeClass()).add(map.map);
		}
		for (String size: keys) {
			HashSet<String> toRun = maps.get(size);
			if (toRun == null)
				toRun = new HashSet<String>();
			out.println("<td>" + WebUtil.getFormattedMapResults(WebUtil.getMapResults(runid, toRun, reverse)) + "</td>");
		}
		out.println("</tr>");
	}

}
