package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.BSUser;

import common.HibernateUtil;

/**
 * Admin panel for confirming new users
 * @author stevearc
 *
 */
public class AdminServlet extends HttpServlet {
	private static final long serialVersionUID = 1272536670071318079L;
	public static final String NAME = "/admin.html";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BSUser user = WebUtil.getUserFromCookie(request, response);
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"/css/table.css\" />");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(request, response, out, NAME);	
		out.println("<script src='js/jquery.dataTables.min.js'></script>");

		if (user == null || user.getPrivs() != BSUser.PRIVS.ADMIN) {
			out.println("<h1>You are not an admin</h1>");
			out.println("</body></html>");
			return;
		}

		out.println("<div style='clear:both; width:100%; text-align:center'><button id='download'>Download worker</button></div>");
		out.println("<div style='float:left'>");
		out.println("<table id='new_user_table' class='datatable' style='width:470px'>");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<th>New User</th>");
		out.println("<th>&nbsp;</th>");
		out.println("<th>&nbsp;</th>");
		out.println("</tr>");
		out.println("</thead><tbody>");


		EntityManager em = HibernateUtil.getEntityManager();
		List<BSUser> pendingUsers = em.createQuery("from BSUser user where user.privs = ?", BSUser.class).setParameter(1, BSUser.PRIVS.PENDING).getResultList();
		for (BSUser pendingUser: pendingUsers) {
			out.println("<tr>");
			out.println("<td>" + pendingUser.getUsername() + "</td>");
			out.println("<td><input type='button' value='Accept' onClick='manageUser(\"new_user_table" + 
					"\", \"" + pendingUser.getUsername() + "\", \"" + pendingUser.getId() + "\", \"accept\", " + pendingUser.getId() + ")'></td>");
			out.println("<td><input type='button' value='Deny' onClick='manageUser(\"new_user_table" + 
					"\", \"" + pendingUser.getUsername() + "\", \"" + pendingUser.getId() + "\", \"delete\", " + pendingUser.getId() + ")'></td>");
			out.println("</tr>");
		}

		out.println("</tbody></table>");
		out.println("</div>");
		
		out.println("<div style='float:right'>");
		out.println("<table id='existing_user_table' class='datatable' style='width:470px'>");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<th>User</h3></th>");
		out.println("<th>Status</th>");
		out.println("<th>&nbsp;</th>");
		out.println("<th>&nbsp;</th>");
		out.println("</tr>");
		out.println("</thead><tbody>");

		// TODO: pass user ids instead of usernames
		List<BSUser> users = em.createQuery("from BSUser user where user.privs != ?", BSUser.class).setParameter(1, BSUser.PRIVS.PENDING).getResultList();
		for (BSUser regUser: users) {
			out.println("<tr>");
			out.println("<td>" + regUser.getUsername() + "</td>");
			switch (regUser.getPrivs()) {
			case USER:
				out.println("<td>normal</td>");
				out.println("<td><input type='button' value='Promote' onClick='manageUser(\"existing_user_table" + 
						"\", \"" + regUser.getUsername() + "\", \"" + regUser.getId() + "\", \"make_admin\")'></td>");
				out.println("<td><input type='button' value='Delete user' onClick='manageUser(\"existing_user_table" + 
						"\", \"" + regUser.getUsername() + "\", \"" + regUser.getId() + "\", \"delete\")'></td>");
				break;
			case ADMIN:
				out.println("<td>admin</td>");
				out.println("<td><input type='button' value='Demote' onClick='manageUser(\"existing_user_table" + 
						"\", \"" + regUser.getUsername() + "\", \"" + regUser.getId() + "\", \"remove_admin\")'></td>");
				out.println("<td><input type='button' value='Delete user' onClick='manageUser(\"existing_user_table" + 
						"\", \"" + regUser.getUsername() + "\", \"" + regUser.getId() + "\", \"delete\")'></td>");
				break;
			}
			out.println("</tr>");
		}
		out.println("</tbody></table>");
		out.println("</div>");
		out.println("<script type=\"text/javascript\" src=\"js/admin.js\"></script>");
		out.println("</body></html>");
	}
	
}
