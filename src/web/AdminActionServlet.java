package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import master.WebSocketChannelManager;
import model.BSMap;
import model.BSPlayer;
import model.BSUser;

import common.HibernateUtil;


/**
 * Handles queries from admin that modify the database
 * @author stevearc
 *
 */
public class AdminActionServlet extends HttpServlet {
	private static final long serialVersionUID = 46170148422590931L;
	public static final String NAME = "/admin_action";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		BSUser user = WebUtil.getUserFromCookie(request, response);
		String cmd = request.getParameter("cmd");
		response.setContentType("text/plain");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		// check for admin privs
		if (user.getPrivs() != BSUser.PRIVS.ADMIN) {
			out.print("Unauthorized access");
			return;
		}
		EntityManager em = HibernateUtil.getEntityManager();
		if ("toggle_player".equals(cmd)) {
			Long playerid = new Long(Integer.parseInt(request.getParameter("playerid")));
			BSPlayer targetPlayer = em.find(BSPlayer.class, playerid);
			if (targetPlayer == null) {
				return;
			}
			targetPlayer.setInvisible(!targetPlayer.getInvisible());
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
			if (targetPlayer.getInvisible()) {
				WebSocketChannelManager.broadcastMsg("index", "DELETE_PLAYER", targetPlayer.getId() + "");
			} else {
				WebSocketChannelManager.broadcastMsg("index", "ADD_PLAYER", targetPlayer.getId() + "," + targetPlayer.getPlayerName());
			}
			out.print("success");
		} else if ("toggle_map".equals(cmd)) {
			Long mapid = new Long(Integer.parseInt(request.getParameter("mapid")));
			BSMap map = em.find(BSMap.class, mapid);
			if (map == null) {
				return;
			}
			map.setInvisible(!map.getInvisible());
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
			if (map.getInvisible()) {
				WebSocketChannelManager.broadcastMsg("index", "DELETE_MAP", map.getId() + "");
			} else {
				WebSocketChannelManager.broadcastMsg("index", "ADD_MAP", map.getId() + "," + map.getMapName());
			}
			out.print("success");
		} else if ("accept".equals(cmd)) {
			Long userid = new Long(Integer.parseInt(request.getParameter("userid")));
			BSUser targetUser = em.find(BSUser.class, userid);
			if (targetUser == null) {
				return;
			}
			targetUser.setPrivs(BSUser.PRIVS.USER);
			em.merge(targetUser);
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
			out.print("success");
		} 
		else if ("delete".equals(cmd)) {
			Long userid = new Long(Integer.parseInt(request.getParameter("userid")));
			BSUser targetUser = em.find(BSUser.class, userid);
			if (targetUser == null) {
				return;
			}
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
			Long userid = new Long(Integer.parseInt(request.getParameter("userid")));
			BSUser targetUser = em.find(BSUser.class, userid);
			if (targetUser == null) {
				return;
			}
			targetUser.setPrivs(BSUser.PRIVS.ADMIN);
			em.merge(targetUser);
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
			out.print("success");
		}
		else if ("remove_admin".equals(cmd)) {
			Long userid = new Long(Integer.parseInt(request.getParameter("userid")));
			BSUser targetUser = em.find(BSUser.class, userid);
			if (targetUser == null) {
				return;
			}
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

}
