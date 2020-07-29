
### HandlerExecutionChain

#### boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response)
```
boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
			for (int i = 0; i < interceptors.length; i++) {
				HandlerInterceptor interceptor = interceptors[i];
				//如果某个拦截器调用失败则返回调用失败,同时调用 triggerAfterCompletion
				if (!interceptor.preHandle(request, response, this.handler)) {
					triggerAfterCompletion(request, response, null);
					return false;
				}
				this.interceptorIndex = i;
			}
		}
		return true;
	}
	
```

#### void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv)

```
void applyPostHandle(HttpServletRequest request, HttpServletResponse response, @Nullable ModelAndView mv)
			throws Exception {

		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
			for (int i = interceptors.length - 1; i >= 0; i--) {
				HandlerInterceptor interceptor = interceptors[i];
				interceptor.postHandle(request, response, this.handler, mv);
			}
		}
	}

```

#### void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex)

```
void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, @Nullable Exception ex)throws Exception {

		HandlerInterceptor[] interceptors = getInterceptors();
		if (!ObjectUtils.isEmpty(interceptors)) {
		    //只处理 interceptor.preHandle(request, response, this.handler) 返回true的拦截器
			for (int i = this.interceptorIndex; i >= 0; i--) {
				HandlerInterceptor interceptor = interceptors[i];
				try {
					interceptor.afterCompletion(request, response, this.handler, ex);
				}
				catch (Throwable ex2) {
					logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
				}
			}
		}
	}
```