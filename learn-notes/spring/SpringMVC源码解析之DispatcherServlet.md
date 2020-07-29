
### public class DispatcherServlet extends FrameworkServlet

#### 选择handler

```
protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		if (this.handlerMappings != null) {
			for (HandlerMapping mapping : this.handlerMappings) {
                // 返回 HandlerExecutionChain 因为Spring MVC的拦截器机制有可能需要向目标Handler包裹一些HandlerInterceptor。
                //常见的一些HandlerInterceptor有ConversionServiceExposingInterceptor,ResourceUrlProviderExposingInterceptor                                          
				HandlerExecutionChain handler = mapping.getHandler(request);
				if (handler != null) {
					return handler;
				}
			}
		}
		return null;
	}

```

#### 默认返回noHandlerFound

```
protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (pageNotFoundLogger.isWarnEnabled()) {
			pageNotFoundLogger.warn("No mapping for " + request.getMethod() + " " + getRequestUri(request));
		}
        // 如果属性 throwExceptionIfNoHandlerFound 为 true，则抛出异常 NoHandlerFoundException
		if (this.throwExceptionIfNoHandlerFound) {
			throw new NoHandlerFoundException(request.getMethod(), getRequestUri(request),
					new ServletServerHttpRequest(request).getHeaders());
		}
		else {
            //如果属性 throwExceptionIfNoHandlerFound 为 false ， 则 response.sendError(404)
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

```
#### getHandlerAdapter(Object handler)

- DispatcherServlet请求处理过程中，执行Handler处理请求是通过HandlerAdapter完成的，而并非是DispatcherServlet直接调用Handler提供的处理方法

- DispatcherServlet初始化过程中初始化了initHandlerAdapters,SpringMVC默认提供了多种HandlerAdapter (WebMvcConfigurationSupport)

```
protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		if (this.handlerAdapters != null) {
			for (HandlerAdapter adapter : this.handlerAdapters) {
				if (adapter.supports(handler)) {
					return adapter;
				}
			}
		}
		throw new ServletException("No adapter for handler [" + handler +
				"]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
	}

```


#### doDispatch()

```
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpServletRequest processedRequest = request;
		HandlerExecutionChain mappedHandler = null;
		boolean multipartRequestParsed = false;

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

		try {
			ModelAndView mv = null;
			Exception dispatchException = null;

			try {
				processedRequest = checkMultipart(request);
				multipartRequestParsed = (processedRequest != request);
				// 获取
				mappedHandler = getHandler(processedRequest);
                //如果获取请求的Handler失败,则设置 response No handler found -> set appropriate HTTP response status.
				if (mappedHandler == null) {
					noHandlerFound(processedRequest, response);
					return;
				}

				// 获取指定handler的HandlerAdapter
				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

				// Process last-modified header, if supported by the handler.
				String method = request.getMethod();
				boolean isGet = "GET".equals(method);
				if (isGet || "HEAD".equals(method)) {
					long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
					if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
						return;
					}
				}
                //1. mappedHandler 是所找到的Handler，类型为HandlerExecutionChain,相当于多个 HandlerInterceptor 包裹一个Handler            
                //2. 这里 mv 是一个类型为ModeAndView的对象
                //3. 这里ha 是做找到的HandlerAdapter
                //在调用 Handler 处理请求之前,应用各个 HandlerInterceptor 的前置拦截处理逻辑 preHandle;如果拦截器处理失败，直接返回
				if (!mappedHandler.applyPreHandle(processedRequest, response)) {
					return;
				}

				// Actually invoke the handler.
				// 各个HandlerInterceptor#preHandle前置处理逻辑应用完成且都返回true,现在需要通过ha调用相应的Handler了
				mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

				if (asyncManager.isConcurrentHandlingStarted()) {
					return;
				}
                // 如果 ModelAndView 不等于null 并且不含试图名时设置默认名称。(默认视图名为 前缀+lookupPath+后缀)
				applyDefaultViewName(processedRequest, mv);
				// 现在已经使用Handler处理了请求，结果是ModelAndView，并且对默认视图设置了默认视图名。调用各个HandlerInterceptor的后置拦截处理器postHandler
				mappedHandler.applyPostHandle(processedRequest, response, mv);
			}
			catch (Exception ex) {
				dispatchException = ex;
			}
			catch (Throwable err) {
				// As of 4.3, we're processing Errors thrown from handler methods as well,
				// making them available for @ExceptionHandler methods and other scenarios.
				dispatchException = new NestedServletException("Handler dispatch failed", err);
			}
			// 如果有异常或者是 ModelAndView 是返回类型则通过render方法直接返回视图
			processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
		}
		catch (Exception ex) {
			triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
		}
		catch (Throwable err) {
			triggerAfterCompletion(processedRequest, response, mappedHandler,
					new NestedServletException("Handler processing failed", err));
		}
		finally {
			if (asyncManager.isConcurrentHandlingStarted()) {
				// Instead of postHandle and afterCompletion
				if (mappedHandler != null) {
					mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
				}
			}
			else {
				// Clean up any resources used by a multipart request.
				if (multipartRequestParsed) {
					cleanupMultipart(processedRequest);
				}
			}
		}
	}

```

#### private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,@Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,@Nullable Exception exception) throws Exception 

```
private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
			@Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,
			@Nullable Exception exception) throws Exception {
        //记录是否所要渲染的视图是一个错误页面视图，用于一些属性标记和清除动作 
		boolean errorView = false;
        //如果有异常则返回错误视图
		if (exception != null) {
		    //注意这种情况下，此方法调用开始时参数 mv 一定为 null，这是由DispatcherServlet#doDispatch方法的逻辑流程所决定的。
		    //如果是ModelAndViewDefiningException类型异常则直接返回
			if (exception instanceof ModelAndViewDefiningException) {
				logger.debug("ModelAndViewDefiningException encountered", exception);
				mv = ((ModelAndViewDefiningException) exception).getModelAndView();
			}
			//非ModelAndViewDefiningException类型异常，则构造一个异常视图
			else {
				Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
				mv = processHandlerException(request, response, handler, exception);
				errorView = (mv != null);
			}
		}
		//z
		//  当前 mv 也可能是 null，比如控制器方法直接返回 JSON/XML而无需视图渲染的情况，这种情况下当前方法在这里就算结束了，仅为它进入不了下面的 if 语句块
		if (mv != null && !mv.wasCleared()) {
			render(mv, request, response);
			//如果是错误视图
			if (errorView) {
				WebUtils.clearErrorRequestAttributes(request);
			}
		}
		else {
			if (logger.isTraceEnabled()) {
				logger.trace("No view rendering, null ModelAndView returned.");
			}
		}

		if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
			// Concurrent handling started during a forward
			return;
		}

		if (mappedHandler != null) {
			mappedHandler.triggerAfterCompletion(request, response, null);
		}
	}

```