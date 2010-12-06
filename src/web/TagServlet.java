package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Page that allows svn users to tag revisions of their code
 * @author stevearc
 *
 */
public class TagServlet extends AbstractServlet {
	private static final long serialVersionUID = 2078692431361452598L;
	public static final String NAME="tags.html";

	public TagServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = checkLogin(request, response);
		if (username == null) {
			redirect(response);
			return;
		}

		if (request.getParameter("revs") != null) {
			doUpdate(request, response);
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
		out.println("<script type=\"text/javascript\" src=\"js/tags.js\"></script>");
		out.println("</head>");
		out.println("<body>");

		WebUtil.writeTabs(response, out, name);	

		out.println("<div id='tablewrapper'>");
		out.println("<table id='tag_table' class='tinytable' style='width:300px'>");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<th><h3>Revision</h3></th>");
		out.println("<th><h3>Tag</h3></th>");
		out.println("</tr>");
		out.println("</thead><tbody>");

		try {
			ResultSet rs = db.query("SELECT * FROM tags ORDER BY tag");
			int i = 0;
			while (rs.next()) {
				out.println("<tr>");
				out.println("<td>" + rs.getString("tag") + "</td>");
				String alias = rs.getString("alias");
				alias = (alias == null ? " " : alias);
				out.println("<td><input id='box" + i + "' type='text' width='10' value='" + alias + "' " +
						"onFocus='document.getElementById(\"box" + i + "\").select()' " +
						"onClick='setTimeout(\"document.getElementById(\\\"box" + i + "\\\").select()\", 50)' " +
				"onkeypress='keyDown(" + i + ")'></td>");
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
		out.println("<input type='button' value='Reset' onClick='document.location.reload(true)'>&nbsp;&nbsp;&nbsp;");
		out.println("<input id='save_tags' type='button' value='Save changes' onClick='saveTags()'>");
		out.println("</div>");

		out.println("</body></html>");
	}

	/**
	 * When receive the GET request with new info, update the DB
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void doUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/json");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		String revs = request.getParameter("revs");
		String aliases = request.getParameter("aliases");
		String[] revArr = revs.split(",");
		String[] aliasArr = aliases.split(",");
		try {
			for (int i = 0; i < revArr.length; i++) {
				if (!"".equals(aliasArr[i].trim())) {
					PreparedStatement stmt = db.prepare("UPDATE tags SET alias = ? WHERE tag LIKE ?");
					stmt.setString(1, aliasArr[i]);
					stmt.setString(2, revArr[i]);
					db.update(stmt, true);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace(out);
		}
	}

}
