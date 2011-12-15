package web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import master.MasterMethodCaller;
import model.BSPlayer;
import model.BSRun;

import common.Config;
import common.HibernateUtil;


/**
 * Upload players
 * @author stevearc
 *
 */
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 2147508188812654640L;
	public static final String NAME = "upload.html";

	private void warn(HttpServletResponse response, String warning) throws IOException {
		response.getWriter().println("<p class=\"warning\">" + warning + "</p>");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"css/tinytable.css\" />");
		out.println("<link rel=\"stylesheet\" href=\"css/tabs.css\" />");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(response, out, toString());
		out.println("<div id='instructions' style='margin:10px; font-size:14px'>Compile your player using");
		out.println("<div class='code'>ant jar -Dteam=teamXXX</div>");
		out.println("where teamXXX is your team number.  Then upload it here.</div>");

		if (request.getParameter("submit-player") != null) {
			File player = (File) request.getAttribute("player");
			String playerName = request.getParameter("player_name").trim();
			if (player == null || !player.exists()) {
				// file doesn't exist
				warn(response, "Please select a valid file");
			} else if (player.isDirectory()) {
				// what?
				warn(response, "Please select a valid file");
			} else if (playerName == null || playerName.isEmpty() || playerName.contains("/") || playerName.contains(" ")) {
				warn(response, "Please enter a valid name for your player");
			} else if (playerName.length() > 45) {
				warn(response, "Player name is too long");
			} else {
				FileInputStream istream = new FileInputStream(player);
				FileOutputStream ostream = new FileOutputStream(Config.getConfig().install_dir + "/battlecode/teams/" + playerName + ".jar");
				byte[] buffer = new byte[1000];
				int len = 0;
				while ((len = istream.read(buffer)) != -1) {
					ostream.write(buffer, 0, len);
				}
				istream.close();
				ostream.close();
				BSPlayer bsPlayer = new BSPlayer();
				bsPlayer.setPlayerName(playerName);
				EntityManager em = HibernateUtil.getEntityManager();
				em.persist(bsPlayer);
				em.getTransaction().begin();
				try {
					em.flush();
					out.println("<p>Successfully uploaded player: " + playerName + "</p>");
				} catch (PersistenceException e) {
					// player name already exists
					warn(response, "Player name already exists");
				}
				if (em.getTransaction().getRollbackOnly()) {
					em.getTransaction().rollback();
				} else {
					em.getTransaction().commit();
				}
				em.close();
			}
		} else if (request.getParameter("submit-update") != null) {
			File idata = (File) request.getAttribute("idata");
			File battlecode_server = (File) request.getAttribute("battlecode-server");
			if (idata == null || !idata.exists() || idata.isDirectory()) {
				// file doesn't exist
				warn(response, "Please select a valid idata file");
			} else if (battlecode_server == null || !battlecode_server.exists() || battlecode_server.isDirectory()) {
				// file doesn't exist
				warn(response, "Please select a valid battlecode-server.jar file");
			} else {
				MasterMethodCaller.updateBattlecodeFiles(battlecode_server, idata);
				EntityManager em = HibernateUtil.getEntityManager();
				Long numRunning = em.createQuery("select count(*) from BSRun run where run.status = ?", Long.class)
				.setParameter(1, BSRun.STATUS.RUNNING)
				.getSingleResult();
				if (numRunning == 0) {
					out.println("<p>Successfully updated battlecode version!</p>");
				} else {
					out.println("<p>Successfully updated battlecode version!  Changes will take place after current run finishes</p>");
				}
				em.close();
			}
		}

		// Form for uploading your player
		out.println("<div style='margin:20px 10px 10px 10px; font-size:12px'>");
		out.println("<form action=\"" + NAME + "\" method=\"post\" enctype=\"multipart/form-data\">");
		out.println("<table>");
		out.println("<tr><th colspan='2'>Upload your player</th></tr>");
		out.println("<tr>" +
				"<td style='text-align:right'>Player jar:</td>" +
				"<td><input type=\"file\" name=\"player\"></td>" +
				"</tr>");
		out.println("<tr>" +
				"<td style='text-align:right'>Player Name:</td>" +
				"<td><input type=\"text\" name=\"player_name\" size=\"20\" /></td>" +
				"</tr>");
		out.println("<tr><td></td>" +
				"<td><input type=\"submit\" name=\"submit-player\" value=\"Upload\"/></td>" +
				"</tr>");
		out.println("</table>");
		out.println("</form>");
		out.println("</div>");
		
		// Form for uploading a new battlecode version
		out.println("<div style='margin:40px 10px 10px 10px; font-size:12px'>");
		out.println("<form action=\"" + NAME + "\" method=\"post\" enctype=\"multipart/form-data\">");
		out.println("<table>");
		out.println("<tr><th colspan='2'>Upload new battlecode files</th></tr>");
		out.println("<tr>" +
				"<td style='text-align:right'>battlecode-server.jar file:</td>" +
				"<td><input type=\"file\" name=\"battlecode-server\"></td>" +
				"</tr>");
		out.println("<tr>" +
				"<td style='text-align:right'>idata file:</td>" +
				"<td><input type=\"file\" name=\"idata\"></td>" +
				"</tr>");
		out.println("<tr><td></td>" +
				"<td><input type=\"submit\" name=\"submit-update\" value=\"Upload\"/></td>" +
				"</tr>");
		out.println("</table>");
		out.println("</form>");
		out.println("</div>");

		out.println("</body>" +
		"</html>");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	@Override
	public String toString() {
		return NAME;
	}
}
