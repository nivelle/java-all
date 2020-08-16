
### HandlerMethod

#### 一个HandlerMethod对象,包装了以下信息：

- Object bean: Web控制器方法所在的Web控制器bean. 可以是字符串，代表bean 的名称，也可以是bean实例对象本身

- Class beanType: Web控制器方法所在的Web控制器bean的类型，如果该bean被代理，这里记录的是被代理的用户类信息

- Method method: Web控制器方法

- Method bridgedMethod: 被桥接的Web控制器方法

- MethodParameter[] parameter :Web控制器方法的参数信息，所在类所在方法，参数，索引，参数类型

- HttpStatus responseStatus: 注解@ResponseStatus 的code 属性

- String responseStatusReason: 注解@ResponseStatus 的 reason 属性


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