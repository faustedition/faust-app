package de.faustedition.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @see <a
 *      href="http://stackoverflow.com/questions/132052/servlet-for-serving-static-content">Problem
 *      with Jetty vs. Tomcat default servlet</a>
 * @author gregor
 * 
 */
public class StaticWrapperServlet extends HttpServlet
{
	private static final String DEFAULT_SERVLET = "default";

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		getServletContext().getNamedDispatcher(DEFAULT_SERVLET).forward(new HttpServletRequestWrapper(req)
		{
			public String getServletPath()
			{
				return "";
			}
		}, resp);
	}
}
