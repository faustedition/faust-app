package com.bradmcevoy.http;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * 
 * MiltonServlet is a thin wrapper around HttpManager. It takes care of initialisation
 * and delegates requests to the HttpManager
 * 
 * The servlet API is hidden by the Milton API, however you can get access to
 * the underlying request and response objects from the static request and response
 * methods which use ThreadLocal variables
 *
 * This spring aware servlet will load the spring context from a classpath
 * resource named /applicationContext.xml
 *
 * It will then load a bean named milton.http.manager which must be of type
 * HttpManager.
 *
 * An example applicationContext.xml might look like this
 *
 * <beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.0.xsd">

    <bean id="milton.resource.factory" class="com.ettrema.http.fs.FileSystemResourceFactory">
        <property name="securityManager" ref="milton.fs.security.manager" />
        <property name="maxAgeSeconds" value="3600" />
    </bean>

    <bean id="milton.fs.security.manager" class="com.ettrema.http.fs.NullSecurityManager" >
        <property name="realm" value="aRealm" />
    </bean>

    <bean id="milton.response.handler" class="com.bradmcevoy.http.DefaultResponseHandler" />

    <bean id="milton.http.manager" class="com.bradmcevoy.http.HttpManager">
        <constructor-arg ref="milton.resource.factory" />
        <constructor-arg ref="milton.response.handler" />
    </bean>
</beans>
 *
 * @author brad
 */
public class SpringAwareMiltonServlet implements Servlet{
    
    private Logger log = LoggerFactory.getLogger(SpringAwareMiltonServlet.class);
    
    ServletConfig config;
    ApplicationContext context;
    HttpManager httpManager;
    
    private static final ThreadLocal<HttpServletRequest> originalRequest = new ThreadLocal<HttpServletRequest>();
    private static final ThreadLocal<HttpServletResponse> originalResponse = new ThreadLocal<HttpServletResponse>();

    public static HttpServletRequest request() {
        return originalRequest.get();
    }
    
    public static HttpServletResponse response() {
        return originalResponse.get();
    }
    
    public static void forward(String url) {
        try {
            request().getRequestDispatcher(url).forward(originalRequest.get(),originalResponse.get());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ServletException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void init(ServletConfig config) throws ServletException {
        try {
            this.config = config;
            context = new ClassPathXmlApplicationContext(new String[] {"applicationContext.xml"});
            httpManager = (HttpManager) context.getBean("milton.http.manager");
        } catch (Throwable ex) {
            log.error("Exception starting milton servlet",ex);
            throw new RuntimeException(ex);
        }        
    }
    
    public void service(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        try {
            originalRequest.set(req);
            originalResponse.set(resp);
            Request request = new ServletRequest(req);
            Response response = new ServletResponse(resp);
            httpManager.process(request, response);
        } finally {
            originalRequest.remove();
            originalResponse.remove();
            servletResponse.getOutputStream().flush();            
            servletResponse.flushBuffer();
        }
    }

    public String getServletInfo() {
        return "SpringAwareMiltonServlet";
    }

    public ServletConfig getServletConfig() {
        return config;
    }

    public void destroy() {
        
    }
}
