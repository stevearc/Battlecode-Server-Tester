package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.BSUser;
import db.HibernateUtil;

/**
 * Removes the user's cookies and deletes their session from the DB
 * @author stevearc
 *
 */
public class LogoutServlet extends AbstractServlet {
	private static final long serialVersionUID = 2965899260728547098L;
	public static final String NAME = "logout.html";

	public LogoutServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BSUser user = checkLogin(request, response);
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			response.setHeader("Location", response.encodeURL("/" + LoginServlet.NAME));
			return;
		}
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		EntityManager em = HibernateUtil.getEntityManager();
		user.setSession(null);
		em.merge(user);
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
		em.close();

		out.println("<script type='text/javascript'>\n" +
				"document.cookie = '" + COOKIE_NAME + "=; expires=Fri, 27 Jul 2001 00:00:00 UTC; path=/';\n" +
				"document.location='" + response.encodeURL(LoginServlet.NAME) + "';\n" +
		"</script>");
	}
}
