package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.Config;

public class DebugServlet extends AbstractServlet {
	private static final long serialVersionUID = 5540409128136287730L;
	public static final String NAME = "debug.html";
	
	public DebugServlet() {
		super(NAME);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter out = response.getWriter();
		out.println("<html><head>");
		out.println("<title>Battlecode Tester</title>");
		out.println("</head>");
		out.println("<body>");
		out.println(Config.getServer().debugDump().replaceAll("\n", "<br />"));
		out.println("</body>" +
		"</html>");
		
		

	}
}
