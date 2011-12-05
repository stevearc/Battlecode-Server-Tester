package web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.BSUser;
import db.HibernateUtil;


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
		BSUser user = checkLogin(request, response);
		if (user == null) {
			return;
		}
		Long userid = new Long(Integer.parseInt(request.getParameter("userid")));
		String cmd = request.getParameter("cmd");
		response.setContentType("text/json");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		EntityManager em = HibernateUtil.getEntityManager();
		BSUser targetUser = em.find(BSUser.class, userid);
		if (targetUser == null) {
			return;
		}
		// check for admin privs
		if (user.getPrivs() != BSUser.PRIVS.ADMIN) {
			out.print("Unauthorized access");
			return;
		}
		if ("accept".equals(cmd)) {
			targetUser.setPrivs(BSUser.PRIVS.USER);
			em.merge(targetUser);
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
			out.print("success");
		} 
		else if ("delete".equals(cmd)) {
			if (getNumAdmins() <= 1 && targetUser.getPrivs() == BSUser.PRIVS.ADMIN) {
				out.print("admin_limit");
				return;
			}
			em.remove(targetUser);
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
			out.print("success");
		} 
		else if ("make_admin".equals(cmd)) {
			targetUser.setPrivs(BSUser.PRIVS.ADMIN);
			em.merge(targetUser);
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
			out.print("success");
		}
		else if ("remove_admin".equals(cmd)) {
			if (getNumAdmins() <= 1 && targetUser.getPrivs() == BSUser.PRIVS.ADMIN) {
				out.print("admin_limit");
				return;
			}
			targetUser.setPrivs(BSUser.PRIVS.USER);
			em.merge(targetUser);
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
			out.print("success");
		}
		else if ("change_pass".equals(cmd)) {

		}
		else {
			out.print("Unknown error");
		}
		em.close();
	}

	@SuppressWarnings("unchecked")
	private int getNumAdmins() {
		// TODO: rowcount
		EntityManager em = HibernateUtil.getEntityManager();
		List<BSUser> users = em.createQuery("from BSUser users where users.privs = ?")
		.setParameter(1, BSUser.PRIVS.ADMIN)
		.getResultList();
		return users.size();
	}
}
