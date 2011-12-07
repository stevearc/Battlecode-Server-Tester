package web;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginFilter implements Filter {

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest hRequest = (HttpServletRequest) request;
		HttpServletResponse hResponse = (HttpServletResponse) response;
		if (!hRequest.getRequestURI().equals("/" + LoginServlet.NAME) && hRequest.getSession(true).getAttribute("user") == null) {
	        hResponse.sendRedirect(LoginServlet.NAME); // Not logged in, redirect to login page.
	    } else {
	        chain.doFilter(request, response); // Logged in, just continue request.
	    }
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

	
}
