package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import master.MasterMethodCaller;


/**
 * Handles queries from admin that modify the database
 * @author stevearc
 *
 */
public class AdminActionServlet extends AbstractServlet {
	private static final long serialVersionUID = 46170148422590931L;
	public static final String NAME = "admin_action";

	public AdminActionServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String myUsername = checkLogin(request, response);
		if (myUsername == null) {
			return;
		}
		String username = request.getParameter("username");
		String cmd = request.getParameter("cmd");
		response.setContentType("text/json");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();

		try {
			// You can update the repo even if not admin
			if ("update".equals(cmd)) {
				MasterMethodCaller.updateRepo();
			} // Otherwise, check for admin privs
			else if (!isUserAdmin(myUsername)) {
				out.print("Unauthorized access");
				return;
			}
			if ("accept".equals(cmd)) {
				PreparedStatement st = db.prepare("UPDATE users SET status = 1 WHERE username LIKE ?");
				st.setString(1, username);
				db.update(st, true);
				out.print("success");
			} 
			else if ("delete".equals(cmd)) {
				if (getNumAdmins() <= 1 && isUserAdmin(username)) {
					out.print("admin_limit");
					return;
				}
				PreparedStatement st = db.prepare("DELETE FROM users WHERE username LIKE ?");
				st.setString(1, username);
				db.update(st, true);
				out.print("success");
			} 
			else if ("make_admin".equals(cmd)) {
				PreparedStatement st = db.prepare("UPDATE users SET status = 2 WHERE username LIKE ?");
				st.setString(1, username);
				db.update(st, true);
				out.print("success");
			}
			else if ("remove_admin".equals(cmd)) {
				ResultSet rs = db.query("SELECT COUNT(*) AS c FROM users WHERE status = 2");
				rs.next();
				if (rs.getInt("c") <= 1) {
					out.print("admin_limit");
					return;
				}
				PreparedStatement st = db.prepare("UPDATE users SET status = 1 WHERE username LIKE ?");
				st.setString(1, username);
				db.update(st, true);
				out.print("success");
			}
			else if ("change_pass".equals(cmd)) {
				
			}
			else {
				out.print("Unknown error");
			}
		} catch (SQLException e) {
			e.printStackTrace(out);
		}
	}

	private int getNumAdmins() throws SQLException {
		ResultSet rs = db.query("SELECT COUNT(*) AS c FROM users WHERE status = 2");
		rs.next();
		return rs.getInt("c");
	}
	
	private boolean isUserAdmin(String username) throws SQLException {
		PreparedStatement st = db.prepare("SELECT status FROM users WHERE username LIKE ?");
		st.setString(1, username);
		ResultSet rs = db.query(st);
		rs.next();
		return rs.getInt("status") == 2;
	}
}
