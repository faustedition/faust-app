package com.bradmcevoy.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 * IMPORTANT !!!!!!!!!!!
 * This controller will ONLY work if used in conjunction with DavEnabledDispatcherServlet
 *
 * It WILL NOT work with the standard spring DispatcherServlet because it
 * explicitly forbids the use of webdav methods such as PROPFIND
 *
 * Please see the javadoc for DavEnabledDispatcherServlet for details
 *
 */
public class MiltonController implements Controller {

    private static final Logger log = LoggerFactory.getLogger(MiltonController.class);

    private HttpManager httpManager;

    public MiltonController() {
    }

    public MiltonController(HttpManager httpManager) {
        log.debug("created miltoncontroller");
        this.httpManager = httpManager;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("handleRequest: " + request.getRequestURI() + " method:" + request.getMethod() ) ;
        ServletRequest rq = new ServletRequest(request);
        ServletResponse rs = new ServletResponse(response);
        httpManager.process(rq, rs);
        return null;
    }

    public HttpManager getHttpManager() {
        return httpManager;
    }

    public void setHttpManager(HttpManager httpManager) {
        this.httpManager = httpManager;
    }


}
