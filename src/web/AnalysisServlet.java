package web;

import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;

import model.BSMap;
import model.BSMatch;
import model.BSPlayer;
import model.BSRun;
import dataAccess.HibernateUtil;

/**
 * View results of one team against all teams ever run against broken down by map size
 * @author stevearc
 *
 */
public class AnalysisServlet extends AbstractAnalysisServlet {
	public static final String NAME = "analysis.html";
	private static final long serialVersionUID = 5833958478703546254L;

	@Override
	protected void writeTableHead(PrintWriter out) {
		out.println("<tr>" +
		"<th class='desc'><h3>Opponent</h3></th>");
		for (BSMap.SIZE size: BSMap.SIZE.values()) {
			out.println("<th class='nosort'><h3>" + size + "</h3></th>");
		}
		out.println("<th class='nosort'><h3>Total</h3></th>");
		out.println("</tr>");
	}
	
	@Override
	protected void writeTable(PrintWriter out, BSPlayer currentPlayer) {
		EntityManager em = HibernateUtil.getEntityManager();
		
		List<Object[]> dataSets = em.createQuery("select run, map, match.winner, count(*) from BSMatch match " +
				"inner join match.map map inner join match.run run where " +
				"(match.run.teamA = ? or match.run.teamB = ?) and match.status = ? " +
				"and (run.status = ? or run.status = ?) " + 
				"group by run, map, match.winner " +
				"order by run, map, match.winner", Object[].class)
				.setParameter(1, currentPlayer)
				.setParameter(2, currentPlayer)
				.setParameter(3, BSMatch.STATUS.FINISHED)
				.setParameter(4, BSRun.STATUS.COMPLETE)
				.setParameter(5, BSRun.STATUS.CANCELED)
				.getResultList();
		for (int i = 0; i < dataSets.size(); ) {
			BSRun run = (BSRun) dataSets.get(i)[0];
			out.println("<tr onClick='document.location=\"matches.html?id=" + run.getId() + "\";' " +
			"style='cursor:pointer'>");
			out.println("<td><font color='red'>" + (run.getTeamA().equals(currentPlayer) ? run.getTeamB() : run.getTeamA()).getPlayerName() + "</font></td>");
			long[][] mapSizeResults = new long[BSMap.SIZE.values().length][3];
			
			while (i < dataSets.size() && ((BSRun) dataSets.get(i)[0]).equals(run)) {
				Object[] dataSet = dataSets.get(i);
				BSMap map = (BSMap) dataSet[1];
				long aCount = (Long) dataSet[3];
				long bCount = 0;
				if (i + 1 < dataSets.size() && map.equals(dataSets.get(i+1)[1])) {
					bCount = (Long) dataSets.get(i+1)[3];
					i++;
				}
				int index;
				BSMatch.TEAM winner = WebUtil.getWinner(aCount, bCount);
				if (winner == BSMatch.TEAM.TEAM_A) {
					index = (run.getTeamA().equals(currentPlayer) ? 0 : 2);
					mapSizeResults[map.getSize().ordinal()][index]++;
				} else if (winner == BSMatch.TEAM.TEAM_B) {
					index = (run.getTeamA().equals(currentPlayer) ? 2 : 0);
					mapSizeResults[map.getSize().ordinal()][index]++;
				} else {
					mapSizeResults[map.getSize().ordinal()][1]++;
				}
				i++;
			}
			long[] total = new long[3];
			for (long[] results: mapSizeResults) {
				for (int j = 0; j < 3; j++) {
					total[j] += results[j];
				}
				out.println("<td><font color='blue'>" + results[0] + "</font>/" +
						"<font color='green'>" + results[1] + "</font>/" +
						"<font color='red'>" + results[2] + "</font></td>");
			}
			out.println("<td><font color='blue'>" + total[0] + "</font>/" +
					"<font color='green'>" + total[1] + "</font>/" +
					"<font color='red'>" + total[2] + "</font></td>");
			out.println("</tr>");
		}
		em.close();
	}
	
	@Override
	public String toString() {
		return NAME;
	}

}
