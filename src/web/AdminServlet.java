package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminServlet extends AbstractServlet {
	private static final long serialVersionUID = 1272536670071318079L;
	public static final String NAME = "admin.html";

	public AdminServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = checkLogin(request, response);
		if (username == null) {
			redirect(response);
			return;
		}

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("<link rel=\"stylesheet\" href=\"/css/tinytable.css\" />");
		out.println("<link rel=\"stylesheet\" href=\"/css/tabs.css\" />");
		out.println("<script type=\"text/javascript\" src=\"js/async.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"js/admin.js\"></script>");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(response, out, name);	

		// TODO: option to change password

		if (getStatus(request, response) != 2) {
			out.println("<div id='tablewrapper'><h1>You are not an admin</h1></div>");
			out.println("</body></html>");
			return;
		}

		out.println("<div id='tablewrapper'>");
		String new_user_table = "new_user_table";
		out.println("<table id='" + new_user_table + "' class='tinytable' style='width:300px'>");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<th><h3>New User</h3></th>");
		out.println("<th><h3>&nbsp;</h3></th>");
		out.println("<th><h3>&nbsp;</h3></th>");
		out.println("</tr>");
		out.println("</thead><tbody>");

		try {
			ResultSet rs = db.query("SELECT username FROM users WHERE status = 0 ORDER BY username");
			int i = 1;
			while (rs.next()) {
				String user = rs.getString("username");
				out.println("<tr>");
				out.println("<td>" + user + "</td>");
				out.println("<td><input type='button' value='Accept' onClick='manageUser(\"" + new_user_table + 
						"\", \"" + user + "\", \"accept\", " + i + ")'></td>");
				out.println("<td><input type='button' value='Deny' onClick='manageUser(\""+ new_user_table + 
						"\", \"" + user + "\", \"delete\", " + i + ")'></td>");
				out.println("</tr>");
				i++;
			}

		} catch (SQLException e) {
			e.printStackTrace(out);
		}
		out.println("</tbody></table>");
		out.println("<p>&nbsp;</p>");
		out.println("<p>&nbsp;</p>");
		out.println("<p>&nbsp;</p>");

		String existing_user_table = "existing_user_table";
		out.println("<table id='" + existing_user_table + "' class='tinytable' style='width:300px'>");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<th><h3>User</h3></th>");
		out.println("<th><h3>Status</h3></th>");
		out.println("<th><h3>&nbsp;</h3></th>");
		out.println("<th><h3>&nbsp;</h3></th>");
		out.println("</tr>");
		out.println("</thead><tbody>");

		try {
			ResultSet rs = db.query("SELECT username, status FROM users WHERE status > 0 ORDER BY username");
			while (rs.next()) {
				String user = rs.getString("username");
				int status = rs.getInt("status");
				out.println("<tr>");
				out.println("<td>" + user + "</td>");
				if (status == 1) {
					out.println("<td>normal</td>");
					out.println("<td><input type='button' value='Promote' onClick='manageUser(\"" + existing_user_table + 
							"\", \"" + user + "\", \"make_admin\")'></td>");
					out.println("<td><input type='button' value='Delete user' onClick='manageUser(\""+ existing_user_table + 
							"\", \"" + user + "\", \"delete\")'></td>");
				} 
				else if (status == 2) {
					out.println("<td>admin</td>");
					out.println("<td><input type='button' value='Demote' onClick='manageUser(\"" + existing_user_table + 
							"\", \"" + user + "\", \"remove_admin\")'></td>");
					out.println("<td><input type='button' value='Delete user' onClick='manageUser(\""+ existing_user_table + 
							"\", \"" + user + "\", \"delete\")'></td>");
				}
				out.println("</tr>");
			}

		} catch (SQLException e) {
			e.printStackTrace(out);
		}
		out.println("</tbody></table>");
		out.println("</div>");

		out.println("</body></html>");
	}
}
