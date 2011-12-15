package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.HibernateUtil;

import model.BSUser;


/**
 * Handles queries from admin that modify the database
 * @author stevearc
 *
 */
public class AdminActionServlet extends HttpServlet {
	private static final long serialVersionUID = 46170148422590931L;
	public static final String NAME = "admin_action";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BSUser user = (BSUser) request.getSession().getAttribute("user");
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

	private int getNumAdmins() {
		EntityManager em = HibernateUtil.getEntityManager();
		Long numAdmins = em.createQuery("select count(*) from BSUser users where users.privs = ?", Long.class)
		.setParameter(1, BSUser.PRIVS.ADMIN)
		.getSingleResult();
		em.close();
		return numAdmins.intValue();
	}
	
	@Override
	public String toString() {
		return NAME;
	}
}
