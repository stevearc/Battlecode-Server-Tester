package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import master.WebPollHandler;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;

import common.Config;

/**
 * Handle all Comet requests.  Communicates with Server via WebPollHandler.
 * @author stevearc
 *
 */
public class CometServlet extends HttpServlet {
	private static final long serialVersionUID = 3140389708838079253L;
	public static final String NAME = "comet";
	private WebPollHandler wph;

	public CometServlet() {
		super();
		wph = Config.getWebPollHandler();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		poll(request, response);
	}

	/**
	 * Uses Jetty Continuations to do an HTTP long poll.  When Server has information, it will send response to client
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private synchronized void poll(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();

		Object r = request.getAttribute("response");
		// If this is the request, create a continuation and subscribe the client to a channel
		if (r == null) {
			Continuation continuation = ContinuationSupport.getContinuation(request);
			if (continuation.isInitial()) {
				continuation.suspend();
				String channel = request.getParameter("channel");
				String lastheard = request.getParameter("lastheard");
				wph.subscribe(channel, continuation, Integer.parseInt(lastheard));
				continuation.undispatch();
			}
		} // Otherwise, this is the Server sending a response.  Send the client data.
		else {
			response.setContentType("text/json;charset=utf-8");
			out.print(r.toString());
		}
	}
	
	@Override
	public String toString() {
		return NAME;
	}
}
