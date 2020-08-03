### handlerMapping

- RequestMappingHandlerMapping

```
//1. 创建配置对象confing:设置运行时所需要的各种参数到该配置对象 2. 检测所有的控制器方法
public void afterPropertiesSet() {
		this.config = new RequestMappingInfo.BuilderConfiguration();
		this.config.setUrlPathHelper(getUrlPathHelper());
		this.config.setPathMatcher(getPathMatcher());
		this.config.setSuffixPatternMatch(this.useSuffixPatternMatch);
		this.config.setTrailingSlashMatch(this.useTrailingSlashMatch);
		this.config.setRegisteredSuffixPatternMatch(this.useRegisteredSuffixPatternMatch);
		this.config.setContentNegotiationManager(getContentNegotiationManager());

		super.afterPropertiesSet();
	}

// 判断一个bean类型师beanType 是否是一个控制器类型，判断条件：beanType带有注解@Controller 或者 @RequestMapping
protected boolean isHandler(Class<?> beanType) {
		return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class) ||AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
	}	
	
// 使用方法或者类层面的注解@RequestMaping信息创建相应的一个RequestMappingInfo对象，如果指定的方法上没有注解@RequestMaping,则不创建RequestMapingInfo，而是返回null
protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		RequestMappingInfo info = createRequestMappingInfo(method);
		if (info != null) {
			RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
			if (typeInfo != null) {
				info = typeInfo.combine(info);
			}
			String prefix = getPathPrefix(handlerType);
			if (prefix != null) {
				info = RequestMappingInfo.paths(prefix).build().combine(info);
			}
		}
		return info;
	}

private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
		RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
		RequestCondition<?> condition = (element instanceof Class ?
				getCustomTypeCondition((Class<?>) element) : getCustomMethodCondition((Method) element));
		return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
	}
	
	
// 根据 RequestMapping 注解 创建RequestMapingInfo 	
protected RequestMappingInfo createRequestMappingInfo(RequestMapping requestMapping, @Nullable RequestCondition<?> customCondition) {
		RequestMappingInfo.Builder builder = RequestMappingInfo
				.paths(resolveEmbeddedValuesInPatterns(requestMapping.path()))
				.methods(requestMapping.method())
				.params(requestMapping.params())
				.headers(requestMapping.headers())
				.consumes(requestMapping.consumes())
				.produces(requestMapping.produces())
				.mappingName(requestMapping.name());
		if (customCondition != null) {
			builder.customCondition(customCondition);
		}
		return builder.options(this.config).build();
	}
	
	
// 判断指定请求request和路径匹配模式pattern，匹配的话构造一个requestMatchResult并返回。不匹配的话返回null
public RequestMatchResult match(HttpServletRequest request, String pattern) {
		RequestMappingInfo info = RequestMappingInfo.paths(pattern).options(this.config).build();
		RequestMappingInfo matchingInfo = info.getMatchingCondition(request);
		if (matchingInfo == null) {
			return null;
		}
		Set<String> patterns = matchingInfo.getPatternsCondition().getPatterns();
		String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
		//根据匹配到的请求匹配条件对象中的第一个(最优的那个)，和当前请求的查找路径lookupPath
		//构造一个RequestMatchResult对象并返回
		return new RequestMatchResult(patterns.iterator().next(), lookupPath, getPathMatcher());
	}	
```