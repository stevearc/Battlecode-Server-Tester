package web;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import common.BattlecodeMap;
import common.Config;

public class MapAnalysisServlet extends AbstractAnalysisServlet {
	public static final String NAME = "map_analysis.html";
	private static final long serialVersionUID = 5833958478703546254L;
	private HashMap<String, HashSet<String>> maps = new HashMap<String, HashSet<String>>();

	public MapAnalysisServlet(){
		super(NAME);
	}

	@Override
	protected void writeTableHead(PrintWriter out) {
		maps.clear();
		for (BattlecodeMap m: Config.getServer().getMaps()) {
			String size = m.getSize();
			if (!maps.containsKey(size)) 
				maps.put(size, new HashSet<String>());
			maps.get(size).add(m.map);
		}
		out.println("<tr>" +
				"<th class='desc'><h3>Team</h3></th>");
		String[] keys = maps.keySet().toArray(new String[maps.keySet().size()]);
		Arrays.sort(keys);
		for (String size: keys) {
			out.println("<th class='desc'><h3>" + size + " (" + maps.get(size).size() + ")</h3></th>");
		}
		out.println("</tr>");
	}

	@Override
	protected void writeTableRow(PrintWriter out, int runid, String row_team, boolean reverse) throws Exception {
		out.println("<tr onClick='document.location=\"matches.html?id=" + runid + "\";' " +
				"style='cursor:pointer'>");
		String td = "<td>";
		out.println(td + row_team + "</td>");
		String[] keys = maps.keySet().toArray(new String[maps.keySet().size()]);
		Arrays.sort(keys);
		for (String size: keys) {
			out.println("<td>" + getFormattedMapResults(getMapResults(runid, maps.get(size), reverse)) + "</td>");
		}
		out.println("</tr>");
	}

}
