package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;

import backend.WebPollHandler;

import common.Config;

public class CometServlet extends AbstractServlet {
	private static final long serialVersionUID = 3140389708838079253L;
	public static final String NAME = "comet";
	private WebPollHandler wph;

	public CometServlet() {
		super(NAME);
		wph = Config.getWebPollHandler();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		poll(request, response);
	}

	private synchronized void poll(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();

		Object r = request.getAttribute("response");
		if (r == null) {
			Continuation continuation = ContinuationSupport.getContinuation(request);
			if (continuation.isInitial()) {
				continuation.suspend();
				String channel = request.getParameter("channel");
				String lastheard = request.getParameter("lastheard");
				wph.addContinuation(channel, continuation, Integer.parseInt(lastheard));
				continuation.undispatch();
			}
		} else {
			response.setContentType("text/json;charset=utf-8");
			out.print(r.toString());
		}
	}
}
