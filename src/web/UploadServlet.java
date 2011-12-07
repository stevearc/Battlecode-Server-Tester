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

import model.BSPlayer;

import common.Config;

import dataAccess.HibernateUtil;

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
		out.println("<div>Compile your player using");
		out.println("<p class=\"code\">ant jar -Dteam=teamXXX</p>");
		out.println("where teamXXX is your team number.  Then upload it here.</div><br/>");

		String submit = request.getParameter("submit");
		if (submit != null) {
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
		}

		out.println("<div>");
		out.println("<form action=\"" + toString() + "\" method=\"post\" enctype=\"multipart/form-data\">");
		out.println("<input type=\"file\" name=\"player\"><br/>");
		out.println("Player Name:<input type=\"text\" name=\"player_name\" size=\"20\" /><br/>");
		out.println("<input type=\"submit\" name=\"submit\" value=\"Upload\"/>");
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
