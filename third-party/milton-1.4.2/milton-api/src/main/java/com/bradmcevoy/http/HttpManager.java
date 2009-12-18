package com.bradmcevoy.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpManager {

    private static final Logger log = LoggerFactory.getLogger(HttpManager.class);

    public static String decodeUrl(String s) {
            return Utils.decodePath(s);
    }


    

    private static final ThreadLocal<Request> tlRequest = new ThreadLocal<Request>();
    private static final ThreadLocal<Response> tlResponse = new ThreadLocal<Response>();

    public static Request request() {
        return tlRequest.get();
    }

    public static Response response() {
        return tlResponse.get();
    }


    final OptionsHandler optionsHandler;
    final GetHandler getHandler;
    final PostHandler postHandler;
    final PropFindHandler propFindHandler;
    final MkColHandler mkColHandler;
    final MoveHandler moveHandler;
    final PutHandler putHandler;
    final DeleteHandler deleteHandler;
    final PropPatchHandler propPatchHandler;    
    final CopyHandler copyHandler;
    final HeadHandler headHandler;
    final LockHandler lockHandler;
    final UnlockHandler unlockHandler;
    
    public final Handler[] allHandlers;
    
    Map<Request.Method, Handler> methodFactoryMap = new ConcurrentHashMap<Request.Method, Handler>();
    
    List<Filter> filters = new ArrayList<Filter>();
    List<EventListener> eventListeners = new ArrayList<EventListener>();
    
    final ResourceFactory resourceFactory;
    final ResponseHandler responseHandler;
    final String notFoundPath;
    
    private SessionAuthenticationHandler sessionAuthenticationHandler;

    /**
     * @deprecated
     *
     * Creates the manager with a DefaultResponseHandler. Method handlers are
     * instantiated with createXXXHandler methods
     *
     * The notFoundPath property is deprecated. Instead, you should use an appropriate
     * ResponseHandler
     *
     * @param resourceFactory
     * @param notFoundPath
     */
    public HttpManager(ResourceFactory resourceFactory, String notFoundPath) {
        this(resourceFactory, notFoundPath, new DefaultResponseHandler()); 
    }

    /**
     * Creates the manager with a DefaultResponseHandler
     *
     * @param resourceFactory
     */
    public HttpManager(ResourceFactory resourceFactory) {
        this(resourceFactory, null, null);
    }

    public HttpManager(ResourceFactory resourceFactory, ResponseHandler responseHandler) {
        this(resourceFactory, null, responseHandler);
    }

    /**
     * @deprecated - instead of notFoundPath, use an appropriate ResponseHandler
     *
     * @param resourceFactory
     * @param notFoundPath
     * @param responseHandler
     */
    public HttpManager(ResourceFactory resourceFactory, String notFoundPath, ResponseHandler responseHandler) {
        if( resourceFactory == null ) throw new NullPointerException("resourceFactory cannot be null");
        this.resourceFactory = resourceFactory;
        this.responseHandler = responseHandler;

        this.notFoundPath = notFoundPath;
        optionsHandler = add( createOptionsHandler() );
        getHandler = add( createGetHandler() );
        postHandler = add( createPostHandler() );
        propFindHandler = add( createPropFindHandler() );
        mkColHandler = add( createMkColHandler() );
        moveHandler = add( createMoveFactory() );
        putHandler = add( createPutFactory() );
        deleteHandler = add( createDeleteHandler() );
        copyHandler = add( createCopyHandler() );
        headHandler = add( createHeadHandler() );
        propPatchHandler = add( createPropPatchHandler() );
        lockHandler = add(createLockHandler());
        unlockHandler = add(createUnlockHandler());
        allHandlers = new Handler[]{optionsHandler,getHandler,postHandler,propFindHandler,mkColHandler,moveHandler,putHandler,deleteHandler,propPatchHandler, lockHandler, unlockHandler};

        filters.add(createStandardFilter());
    }
    

    /**
     * @deprecated - instead, use an appropriate ResponseHandler
     * @return
     */
    public String getNotFoundPath() {
        return notFoundPath;
    }
    
    public ResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    public SessionAuthenticationHandler getSessionAuthenticationHandler() {
        return sessionAuthenticationHandler;
    }

    public void setSessionAuthenticationHandler(SessionAuthenticationHandler sessionAuthenticationHandler) {
        this.sessionAuthenticationHandler = sessionAuthenticationHandler;
    }        

    /**
     * 
     * @param request
     * @return - if no SessionAuthenticationHandler has been set returns null. Otherwise,
     *  calls getSessionAuthentication on it and returns the result
     * 
     * 
     */
    public Auth getSessionAuthentication(Request request) {
        if( this.sessionAuthenticationHandler == null ) return null;
        return this.sessionAuthenticationHandler.getSessionAuthentication(request);
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    
    private <T extends Handler> T add(T h) {
        methodFactoryMap.put(h.method(),h);
        return h;
    }
    
    public void process(Request request, Response response) {
        log.debug(request.getMethod() + " :: " + request.getAbsoluteUrl() + " - " + request.getAbsoluteUrl());
        tlRequest.set( request );
        tlResponse.set( response );
        try {
            FilterChain chain = new FilterChain( this );
            chain.process( request, response );
        } finally {
            tlRequest.remove();
            tlResponse.remove();
        }
    }
    
    
    
    protected Filter createStandardFilter() {
        return new StandardFilter();
    }
    
    
    protected OptionsHandler createOptionsHandler() {
        return new OptionsHandler(this);
    }
    
    protected GetHandler createGetHandler() {
        return new GetHandler(this);
    }
    
    protected PostHandler createPostHandler() {
        return new PostHandler(this);
    }
    
    protected DeleteHandler createDeleteHandler() {
        return new DeleteHandler(this);
    }
    
    protected PutHandler createPutFactory() {
        return new PutHandler(this);
    }
    
    protected MoveHandler createMoveFactory() {
        return new MoveHandler(this);
    }
    
    protected MkColHandler createMkColHandler() {
        return new MkColHandler(this);
    }
    
    protected PropFindHandler createPropFindHandler() {
        return new PropFindHandler(this);
    }
    
    protected CopyHandler createCopyHandler() {
        return new CopyHandler(this);
    }
    
    protected HeadHandler createHeadHandler() {
        return new HeadHandler(this);
    }

    protected PropPatchHandler createPropPatchHandler() {
        return new PropPatchHandler(this);
    }
    
    protected LockHandler createLockHandler() {
        return new LockHandler(this);
    }
    
    protected UnlockHandler createUnlockHandler() {
        return new UnlockHandler(this);
    }
        
    public void addFilter(int pos, Filter filter) {
        filters.add(pos,filter);
    }

    public void addEventListener(EventListener l) {
        eventListeners.add(l);
    }
    
    public void removeEventListener(EventListener l) {
        eventListeners.remove(l);
    }
    
    void onProcessResourceFinish(Request request, Response response, Resource resource, long duration) {
        for( EventListener l : eventListeners ) {
            l.onProcessResourceFinish(request, response, resource,duration);
        }
    }

    void onProcessResourceStart(Request request, Response response, Resource resource) {
        for( EventListener l : eventListeners ) {
            l.onProcessResourceStart(request, response, resource);
        }        
    }

    void onPost(Request request, Response response, Resource resource, Map<String, String> params, Map<String, FileItem> files) {
        for( EventListener l : eventListeners ) {
            l.onPost(request, response, resource, params, files);
        }   
    }

    void onGet(Request request, Response response, Resource resource, Map<String, String> params) {
        for( EventListener l : eventListeners ) {
            l.onGet(request, response, resource, params);
        }   
    }

    public List<Filter> getFilters() {
        ArrayList<Filter> col = new ArrayList<Filter>(filters);
        return col;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
        filters.add(new StandardFilter());
    }

    public void setEventListeners(List<EventListener> eventListeners) {
        this.eventListeners = eventListeners;
    }

    public CopyHandler getCopyHandler() {
        return copyHandler;
    }

    public DeleteHandler getDeleteHandler() {
        return deleteHandler;
    }

    public GetHandler getGetHandler() {
        return getHandler;
    }

    public HeadHandler getHeadHandler() {
        return headHandler;
    }

    public LockHandler getLockHandler() {
        return lockHandler;
    }

    public MkColHandler getMkColHandler() {
        return mkColHandler;
    }

    public MoveHandler getMoveHandler() {
        return moveHandler;
    }



    public OptionsHandler getOptionsHandler() {
        return optionsHandler;
    }

    public PostHandler getPostHandler() {
        return postHandler;
    }

    public PropFindHandler getPropFindHandler() {
        return propFindHandler;
    }

    public PropPatchHandler getPropPatchHandler() {
        return propPatchHandler;
    }

    public UnlockHandler getUnlockHandler() {
        return unlockHandler;
    }

    public PutHandler getPutHandler() {
        return putHandler;
    }



}
