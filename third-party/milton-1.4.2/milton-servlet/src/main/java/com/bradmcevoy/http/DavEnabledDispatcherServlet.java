package com.bradmcevoy.http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

/**
 *  Subclasses DispatcherServlet to override logic which filters out requests
 * for webdav methods such as PROPFIND
 *
 * I don't know what the spring guys were thinking when they decided to do that,
 * but at least they made it easy to override.
 *
 * Hope they don't change it in a later release, could easily break this class
 *
 * Note that this class doesnt change the behaviour of the DispatcherServlet in
 * any other way so can be used as a drop in replacement
 */
public class DavEnabledDispatcherServlet extends DispatcherServlet{

    /**
     * Override of the default implementation to enable webdav methods
     *
     * @param req
     * @param resp
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doService(req, resp);
        } catch(ServletException e) {
            throw e;
        } catch(IOException e) {
            throw e;
        } catch (Exception ex) {
            throw new ServletException(ex);
        }

    }

}
