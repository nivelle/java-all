
### public class StandardWrapper extends ContainerBase implements ServletConfig, Wrapper, NotificationEmitter

- allocate() //获取Servlet 实例，这里会对应 Spring Web 的 DispatcherServlet

```
public Servlet allocate() throws ServletException {

        // If we are currently unloading this servlet, throw an exception
        if (unloading) {
            throw new ServletException(sm.getString("standardWrapper.unloading", getName()));
        }

        boolean newInstance = false;

        // If not SingleThreadedModel, return the same instance every time
        if (!singleThreadModel) {
            // Load and initialize our instance if necessary
            if (instance == null || !instanceInitialized) {
                synchronized (this) {
                    if (instance == null) {
                        try {
                            if (log.isDebugEnabled()) {
                                log.debug("Allocating non-STM instance");
                            }
                            // Note: We don't know if the Servlet implements
                            // SingleThreadModel until we have loaded it.
                            instance = loadServlet();
                            newInstance = true;
                            if (!singleThreadModel) {
                                // For non-STM, increment here to prevent a race
                                // condition with unload. Bug 43683, test case
                                // #3
                                countAllocated.incrementAndGet();
                            }
                        } catch (ServletException e) {
                            throw e;
                        } catch (Throwable e) {
                            ExceptionUtils.handleThrowable(e);
                            throw new ServletException(sm.getString("standardWrapper.allocate"), e);
                        }
                    }
                    if (!instanceInitialized) {
                        initServlet(instance);
                    }
                }
            }

            if (singleThreadModel) {
                if (newInstance) {
                    // Have to do this outside of the sync above to prevent a
                    // possible deadlock
                    synchronized (instancePool) {
                        instancePool.push(instance);
                        nInstances++;
                    }
                }
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("  Returning non-STM instance");
                }
                // For new instances, count will have been incremented at the
                // time of creation
                if (!newInstance) {
                    countAllocated.incrementAndGet();
                }
                return instance;
            }
        }

        synchronized (instancePool) {
            while (countAllocated.get() >= nInstances) {
                // Allocate a new instance if possible, or else wait
                if (nInstances < maxInstances) {
                    try {
                        instancePool.push(loadServlet());
                        nInstances++;
                    } catch (ServletException e) {
                        throw e;
                    } catch (Throwable e) {
                        ExceptionUtils.handleThrowable(e);
                        throw new ServletException(sm.getString("standardWrapper.allocate"), e);
                    }
                } else {
                    try {
                        instancePool.wait();
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
            }
            if (log.isTraceEnabled()) {
                log.trace("  Returning allocated STM instance");
            }
            countAllocated.incrementAndGet();
            return instancePool.pop();
        }
    }

```

#### ApplicationFilterChain

```
public final class ApplicationFilterChain implements FilterChain {

    // Used to enforce requirements of SRV.8.2 / SRV.14.2.5.1
    private static final ThreadLocal<ServletRequest> lastServicedRequest;
    private static final ThreadLocal<ServletResponse> lastServicedResponse;

    static {
        if (ApplicationDispatcher.WRAP_SAME_OBJECT) {
            lastServicedRequest = new ThreadLocal<>();
            lastServicedResponse = new ThreadLocal<>();
        } else {
            lastServicedRequest = null;
            lastServicedResponse = null;
        }
    }

    public static final int INCREMENT = 10;

    /**
     * Filters. 执行目标Servlet.service()方法前需要经历的过滤器Filter,初始化为0个元素的数组对象
     */
    private ApplicationFilterConfig[] filters = new ApplicationFilterConfig[0];

    /**
     * The int which is used to maintain the current position
     * in the filter chain.
     * 用于记录过滤器链中当前所执行的过滤器的位置,是当前过滤器在filters数组的下标,初始化为0
     */
    private int pos = 0;

    /**
     * The int which gives the current number of filters in the chain.
     * 过滤器链中过滤器的个数(注意:并不是数组filters的长度),初始化为0，和filters数组的初始长度一致
     */
    private int n = 0;


    /**
     * The servlet instance to be executed by this chain.
     * 该过滤器链执行完过滤器后最终要执行的目标Servlet
     */
    private Servlet servlet = null;


    /**
     * Does the associated servlet instance support async processing?
     * 所关联的Servlet实例是否支持异步处理,缺省为 false，表示缺省情况下不支持异步处理。
     */
    private boolean servletSupportsAsync = false;

    /**
     * The string manager for our package.
     */
    private static final StringManager sm = StringManager.getManager(Constants.Package);

    /**
     * Static class array used when the SecurityManager is turned on and
     * <code>doFilter</code> is invoked.
     */
    private static final Class<?>[] classType = new Class[]{ServletRequest.class, ServletResponse.class, FilterChain.class};

    /**
     * Static class array used when the SecurityManager is turned on and
     * <code>service</code> is invoked.
     */
    private static final Class<?>[] classTypeUsedInService = new Class[]{ServletRequest.class, ServletResponse.class};


    /**
     * Invoke the next filter in this chain, passing the specified request
     * and response.  If there are no more filters in this chain, invoke
     * the service() method of the servlet itself.
     * 执行过滤器链中的下一个过滤器Filter。如果链中所有过滤器都执行过，
     * 则调用servlet的service()方法。
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response)throws IOException, ServletException {
		// 下面的if-else分支主要是根据Globals.IS_SECURITY_ENABLED 是true还是false决定
		// 如何调用目标逻辑，但两种情况下，目标逻辑最终都是 internalDoFilter(req,res)
        if( Globals.IS_SECURITY_ENABLED ) {
            final ServletRequest req = request;
            final ServletResponse res = response;
            try {
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedExceptionAction<Void>() {
                        @Override
                        public Void run()
                            throws ServletException, IOException {
                            internalDoFilter(req,res);
                            return null;
                        }
                    }
                );
            } catch( PrivilegedActionException pe) {
                Exception e = pe.getException();
                if (e instanceof ServletException)
                    throw (ServletException) e;
                else if (e instanceof IOException)
                    throw (IOException) e;
                else if (e instanceof RuntimeException)
                    throw (RuntimeException) e;
                else
                    throw new ServletException(e.getMessage(), e);
            }
        } else {
            internalDoFilter(request,response);
        }
    }

    // 执行 Filter 
    private void internalDoFilter(ServletRequest request,ServletResponse response)throws IOException, ServletException {
        // Call the next filter if there is one
        if (pos < n) {
            ApplicationFilterConfig filterConfig = filters[pos++];
            try {
	            // 找到目标 Filter 对象
                Filter filter = filterConfig.getFilter();				
                if (request.isAsyncSupported() && "false".equalsIgnoreCase(filterConfig.getFilterDef().getAsyncSupported())) {
                    request.setAttribute(Globals.ASYNC_SUPPORTED_ATTR, Boolean.FALSE);
                }
                // 执行目标 Filter 对象的 doFilter方法,
                // 注意,这里当前 ApplicationFilterChain 对象被传递到了目标 Filter对象的doFilter方法，而目标Filter对象的doFilter在执行完自己被指定的逻辑之后会反过来调用这个ApplicationFilterChain对象的
                // doFilter方法，只是pos向前推进了一个过滤器。这个ApplicationFilterChain 和 Filter之间反复调用彼此doFilter方法的过程一直持续直到当前链发现所有的
                // Filter都已经被执行
                if( Globals.IS_SECURITY_ENABLED ) {
                    final ServletRequest req = request;
                    final ServletResponse res = response;
                    Principal principal =((HttpServletRequest) req).getUserPrincipal();
                    Object[] args = new Object[]{req, res, this};
                    SecurityUtil.doAsPrivilege ("doFilter", filter, classType, args, principal);
                } else {
                    filter.doFilter(request, response, this);
                }
            } catch (IOException | ServletException | RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                e = ExceptionUtils.unwrapInvocationTargetException(e);
                ExceptionUtils.handleThrowable(e);
                throw new ServletException(sm.getString("filterChain.filter"), e);
            }
            return;
        }

        // We fell off the end of the chain -- call the servlet instance
        // 这里是过滤器链中所有的过滤器都已经被执行的情况，现在需要调用servlet实例本身了。
        // 注意:虽然这里开始调用servlet实例了,但是从当前方法执行堆栈可以看出,过滤器链 和链中过滤器的doFilter方法的执行帧还在堆栈中并未退出,他们会在servlet实例的逻辑
        // 执行完后，分别执行完自己剩余的的逻辑才会逐一结束。
        try {
            if (ApplicationDispatcher.WRAP_SAME_OBJECT) {
                lastServicedRequest.set(request);
                lastServicedResponse.set(response);
            }

            if (request.isAsyncSupported() && !servletSupportsAsync) {
                request.setAttribute(Globals.ASYNC_SUPPORTED_ATTR,Boolean.FALSE);
            }
            // Use potentially wrapped request from this point
            if ((request instanceof HttpServletRequest) &&(response instanceof HttpServletResponse) && Globals.IS_SECURITY_ENABLED ) {
                final ServletRequest req = request;
                final ServletResponse res = response;
                Principal principal =((HttpServletRequest) req).getUserPrincipal();
                Object[] args = new Object[]{req, res};
                SecurityUtil.doAsPrivilege("service",servlet,classTypeUsedInService,args,principal);
            } else {
                servlet.service(request, response);
            }
        } catch (IOException | ServletException | RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            e = ExceptionUtils.unwrapInvocationTargetException(e);
            ExceptionUtils.handleThrowable(e);
            throw new ServletException(sm.getString("filterChain.servlet"), e);
        } finally {
            if (ApplicationDispatcher.WRAP_SAME_OBJECT) {
                lastServicedRequest.set(null);
                lastServicedResponse.set(null);
            }
        }
    }
    /**
     * The last request passed to a servlet for servicing from the current
     * thread.
     *
     * @return The last request to be serviced.
     */
    public static ServletRequest getLastServicedRequest() {
        return lastServicedRequest.get();
    }
    /**
     * The last response passed to a servlet for servicing from the current
     * thread.
     *
     * @return The last response to be serviced.
     */
    public static ServletResponse getLastServicedResponse() {
        return lastServicedResponse.get();
    }

    /**
     * Add a filter to the set of filters that will be executed in this chain.
     * 往当前要执行的过滤器链的过滤器集合filters中增加一个过滤器
     * @param filterConfig The FilterConfig for the servlet to be executed
     */
    void addFilter(ApplicationFilterConfig filterConfig) {

        // Prevent the same filter being added multiple times
        // 去重处理,如果已经添加进来则避免二次添加
        for(ApplicationFilterConfig filter:filters)
            if(filter==filterConfig)
                return;

        if (n == filters.length) {
	        // !!! 请注意：每次需要扩容时并不是增加一个元素空间，而是增加INCREMENT个，
	        // 这个行为的结果是filters数组的长度和数组中过滤器的个数n并不相等
            ApplicationFilterConfig[] newFilters =new ApplicationFilterConfig[n + INCREMENT];
            System.arraycopy(filters, 0, newFilters, 0, n);
            filters = newFilters;
        }
        filters[n++] = filterConfig;
    }
    /**
     * Release references to the filters and wrapper executed by this chain.
     */
    void release() {
        for (int i = 0; i < n; i++) {
            filters[i] = null;
        }
        n = 0;
        pos = 0;
        servlet = null;
        servletSupportsAsync = false;
    }

    /**
     * Prepare for reuse of the filters and wrapper executed by this chain.
     */
    void reuse() {
        pos = 0;
    }
    /**
     * Set the servlet that will be executed at the end of this chain.
     *
     * @param servlet The Wrapper for the servlet to be executed
     */
    void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }
    void setServletSupportsAsync(boolean servletSupportsAsync) {
        this.servletSupportsAsync = servletSupportsAsync;
    }
    /**
     * Identifies the Filters, if any, in this FilterChain that do not support
     * async.
     *
     * @param result The Set to which the fully qualified class names of each
     *               Filter in this FilterChain that does not support async will
     *               be added
     */
    public void findNonAsyncFilters(Set<String> result) {
        for (int i = 0; i < n ; i++) {
            ApplicationFilterConfig filter = filters[i];
            if ("false".equalsIgnoreCase(filter.getFilterDef().getAsyncSupported())) {
                result.add(filter.getFilterClass());
            }
        }
    }
}

```