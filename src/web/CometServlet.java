package web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import master.WebPollHandler;
import model.BSUser;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;


import common.Config;

/**
 * Handle all Comet requests.  Communicates with Server via WebPollHandler.
 * @author stevearc
 *
 */
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
		BSUser user = checkLogin(request, response);
		if (user == null){
			redirect(response);
			return;
		}
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
}
