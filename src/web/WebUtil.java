package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSMap;
import model.BSRun;
import model.BSUser;
import model.BSUserPrefs;
import model.STATUS;
import model.TEAM;

import org.apache.log4j.Logger;

import common.HibernateUtil;

/**
 * Utility class with static methods for common web server functions
 * @author stevearc
 *
 */
public class WebUtil {
	private static Logger _log = Logger.getLogger(WebUtil.class);
	public static final double WIN_THRESHOLD = 0.3;
	public static final String COOKIE_NAME = "bs-tester";
	public static final char[] BLACKLIST = {' ', '<', '>', '\'', '"', '`', '\t', '\n'};

	/**
	 * Write the HTML for the tabs at the top of the page
	 * @param response 
	 * @param out
	 * @param current Name of the current tab (to be highlighted)
	 * @throws IOException 
	 */
	public static void writeTabs(HttpServletRequest request, HttpServletResponse response, String current) throws IOException {
		PrintWriter out = response.getWriter();
		out.println("<script src='/js/jquery-1.7.1.min.js'></script>");
		out.println("<script src='/js/jquery-ui-1.8.16.custom.min.js'></script>");
		out.println("<link rel='stylesheet' href='/css/jquery-ui-1.8.16.custom.css' />");
		out.println("<link rel='stylesheet' href='/css/jquery-ui.css' />");
		// Header with tabs
		out.println("<div id=\"tabs\" class='ui-tabs ui-widget ui-widget-content ui-corner-all' style='margin: 30px 0'>");
		if (request.getSession().getAttribute("user") != null) {
			out.println("<a href='" + LogoutServlet.NAME + "' style='float:right; margin: 0 10px'>sign out</a>");
		}
		out.println("<ul class='ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all'>");
		writeTab(out, response, current, IndexServlet.NAME, "Runs");
		writeTab(out, response, current, ScrimmagesServlet.NAME, "Scrimmages");
		writeTab(out, response, current, ConnectionsServlet.NAME, "Connections");
		writeTab(out, response, current, UploadServlet.NAME, "Upload");
		writeTab(out, response, current, AdminServlet.NAME, "Admin");
		out.println("</ul>");
		out.println("</div>");
		out.println("<div id='alerts' style='text-align: center; width:100%'></div>");
	}

	private static void writeTab(PrintWriter out, HttpServletResponse response, String current, String url, String title) {
		out.println("<li class='ui-state-default ui-corner-top" + (current.equals(url) ? "ui-tabs-selected ui-state-active" : "") + 
				"'><a href='" + response.encodeURL(url) + "'><span>" + title + "</span></a></li>");
	}

	/**
	 * Gets the HTML for the format of the map results
	 * @param results int[3] of {red team wins, ties, blue team wins}
	 * @return
	 */
	public static String getFormattedMapResults(int[] results) {
		return "<font color='red'>" + results[0] + "</font>/<font color='gray'>" + results[1] + 
		"</font>/<font color='blue'>" + results[2] + "</font>";
	}

	/**
	 * Get the win/tie/loss results for a particular run
	 * @param runid
	 * @param maps List of maps to get results for (null if all maps)
	 * @param reverse If true, then swap team A and team B
	 * @return int[3] of {red team wins, ties, blue team wins}
	 * @throws SQLException
	 */
	public static int[] getMapResults(BSRun run, List<BSMap> maps, boolean reverse) {
		EntityManager em = HibernateUtil.getEntityManager();
		int[] results = new int[3];
		if (maps == null) {
			maps = em.createQuery("from BSMap", BSMap.class).getResultList();
		}
		int index;
		for (BSMap m: maps) {
			index = getMapResult(run, m);
			if (index >= 0) {
				results[index]++;
			}
		}
		if (reverse) {
			int swap = results[0];
			results[0] = results[2];
			results[2] = swap;
		}
		em.close();
		return results;
	}

	private static int getMapResult(BSRun run, BSMap map) {
		float ratio = getWinPercentage(run, map);
		// These are the thresholds for determining which team "won" on a map
		if (ratio < 0) {
			return -1;
		}
		else if (ratio < WIN_THRESHOLD)
			return 2;
		else if (ratio < 1 - WIN_THRESHOLD)
			return 1;
		else
			return 0;
	}

	/**
	 * Get the win percentage of team A on a given map for a given run
	 * @param runid
	 * @param map
	 * @return
	 */
	public static float getWinPercentage(BSRun run, BSMap map) {
		EntityManager em = HibernateUtil.getEntityManager();
		List<Object[]> valuePairs = em.createQuery("select match.result.winner, count(*) from BSMatch match where match.run = ? and " +
				"match.map = ? and match.status = ? group by match.result.winner", Object[].class)
				.setParameter(1, run)
				.setParameter(2, map)
				.setParameter(3, STATUS.COMPLETE)
				.getResultList();
		long teamAWins = 0;
		long teamBWins = 0;
		for (Object[] valuePair: valuePairs) {
			if (valuePair[0] == TEAM.A) {
				teamAWins = (Long) valuePair[1];
			} else if (valuePair[0] == TEAM.B) {
				teamBWins = (Long) valuePair[1];
			}
		}
		em.close();
		if (teamAWins == 0 && teamBWins == 0) {
			return -1;
		}
		return (float) teamAWins/ (float) (teamAWins + teamBWins);
	}

	/**
	 * Gets the HTML for displaying the win percentage with appropriate colors
	 * @param percent
	 * @return
	 */
	public static String getFormattedWinPercentage(double percent) {
		if (percent < WIN_THRESHOLD)
			return "<font color='blue'>" + (int) (100*percent) + "</font>";
		else if (percent < 1 - WIN_THRESHOLD)
			return "<font color='gray'>" + (int) (100*percent) + "</font>";
		else 
			return "<font color='red'>" + (int) (100*percent) + "</font>";
	}
	
	public static TEAM getWinner(Long aWins, Long bWins) {
		double percent = aWins.doubleValue()/(aWins + bWins);
		if (percent < WIN_THRESHOLD) {
			return TEAM.B;
		} else if (percent > 1 - WIN_THRESHOLD) {
			return TEAM.A;
		} else {
			return null;
		}
	}

	/**
	 * Print the header for the tinytable
	 * @param out
	 * @param sorter Name of the sorter initialized in the .js file
	 */
	public static void printTableHeader(PrintWriter out, String sorter) {
		out.println("<div id=\"tableheader\">" +
				"<div class=\"search\">" +
				"<select id=\"columns\" onchange=\"" + sorter + ".search('query')\"></select>");
		out.println("<input type=\"text\" id=\"query\" onkeyup=\"" + sorter + ".search('query')\" />");
		out.println("</div>");
		out.println("<span class=\"details\">" +
				"<div>Records <span id=\"startrecord\"></span>-<span id=\"endrecord\"></span> of " +
		"<span id=\"totalrecords\"></span></div>");
		out.println("<div><a href=\"javascript:" + sorter + ".reset()\">reset</a></div>" +
		"</span>");
		out.println("</div>");
	}

	/**
	 * Print the footer for the tinytable
	 * @param out
	 * @param sorter Name of the sorter initialized in the .js file
	 */
	public static void printTableFooter(PrintWriter out, String sorter) {
		out.println("<div id=\"tablefooter\">");
		out.println("<div id=\"tablenav\">");
		out.println("<div>");
		out.println("<img src=\"images/first.gif\" width=\"16\" height=\"16\" alt=\"First Page\" " +
				"onclick=\"" + sorter + ".move(-1,true)\" />");
		out.println("<img src=\"images/previous.gif\" width=\"16\" height=\"16\" alt=\"Previous Page\" " +
				"onclick=\"" + sorter + ".move(-1)\" />");
		out.println("<img src=\"images/next.gif\" width=\"16\" height=\"16\" alt=\"Next Page\" " +
				"onclick=\"" + sorter + ".move(1)\" />");
		out.println("<img src=\"images/last.gif\" width=\"16\" height=\"16\" alt=\"Last Page\" " +
				"onclick=\"" + sorter + ".move(1,true)\" />");
		out.println("</div>");
		out.println("<div>");
		out.println("<select id=\"pagedropdown\"></select>");
		out.println("</div>");
		out.println("<div>");
		out.println("<a href=\"javascript:" + sorter + ".showall()\">view all</a>");
		out.println("</div>");
		out.println("</div>");
		out.println("<div id=\"tablelocation\">");
		out.println("<div>");
		out.println("<select onchange=\"" + sorter + ".size(this.value)\">");
		out.println("<option value=\"5\">5</option>");
		out.println("<option value=\"10\" selected=\"selected\">10</option>");
		out.println("<option value=\"20\">20</option>");
		out.println("<option value=\"50\">50</option>");
		out.println("</select>");
		out.println("<span>Entries Per Page</span>");
		out.println("</div>");
		out.println("<div class=\"page\">Page <span id=\"currentpage\"></span> of <span id=\"totalpages\"></span></div>");
		out.println("</div></div>");
	}

	/**
	 * Checks the session cookie to see if the user is currently logged in; sets the user in the session if so
	 * @param request
	 * @param response
	 * @return True if authenticated, false otherwise
	 * @throws IOException 
	 */
	public static BSUser getUserFromCookie(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String cookieVal = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie c: cookies) {
				if (COOKIE_NAME.equals(c.getName())) {
					cookieVal = c.getValue();
					break;
				}
			}
		}
		if (cookieVal == null) {
			return null;
		}
		// Cookie is encoded as [userid]$[session token]
		Long userId = new Long(-1);
		String token = "";
		try {
			userId = new Long(Integer.parseInt(cookieVal.substring(0, cookieVal.indexOf("$"))));
			token = cookieVal.substring(cookieVal.indexOf("$") + 1);
		} catch (Exception e) {
			_log.warn("Login cookie with bad format: " + cookieVal, e);
		}
		// Check cookie value
		EntityManager em = HibernateUtil.getEntityManager();
		BSUser user = em.find(BSUser.class, userId);
		if (user == null) {
			return null;
		}
		if (token.equals(user.getSession())) {
			request.getSession(true).setAttribute("user", user);
			user.setPrefs(new BSUserPrefs());
			return user;
		}
			
		return null;
	}
	
	public static boolean containsBadChar(String string) {
		for (char c: WebUtil.BLACKLIST) {
			if (string.indexOf(c) != -1) {
				return true;
			}
		}
		return false;
	}

}
