
### public class RequestMappingHandlerAdapter extends AbstractHandlerMethodAdapter implements BeanFactoryAware, InitializingBean 

```
private List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

		// Annotation-based argument resolution
		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));//@RequestParam并且参数类型不是Map
		resolvers.add(new RequestParamMapMethodArgumentResolver());//@RequestParam并且参数类型是Map
		resolvers.add(new PathVariableMethodArgumentResolver());//@PathVariable并且参数类型不是Map
		resolvers.add(new PathVariableMapMethodArgumentResolver());//@PathVariable并且参数类型是Map
		resolvers.add(new MatrixVariableMethodArgumentResolver());//@MatrixVariable并且参数类型不是Map
		resolvers.add(new MatrixVariableMapMethodArgumentResolver());//@MatrixVariable并且参数类型是Map
		resolvers.add(new ServletModelAttributeMethodProcessor(false));//true 表示 : 即使不使用注解 @ModelAttribute,非简单类型的方法参数和返回值也会被认为是 model attribute
		resolvers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));//@RequestBody,@ResponseBody
		resolvers.add(new RequestPartMethodArgumentResolver(getMessageConverters(), this.requestResponseBodyAdvice));//@RequestPart
		resolvers.add(new RequestHeaderMethodArgumentResolver(getBeanFactory()));//@RequestHeader并且参数类型不是Map
		resolvers.add(new RequestHeaderMapMethodArgumentResolver());//@RequestHeader并且参数类型是Map
		resolvers.add(new ServletCookieValueMethodArgumentResolver(getBeanFactory()));//@CookieValue
		resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));//@Value
		resolvers.add(new SessionAttributeMethodArgumentResolver());//@SessionAttribute
		resolvers.add(new RequestAttributeMethodArgumentResolver());//@RequestAttribute

		// Type-based argument resolution
		resolvers.add(new ServletRequestMethodArgumentResolver());//WebRequest,ServletRequest,MultipartRequest,HttpSession,PushBuilder,Principal,InputStream, Reader,
                                                                  // HttpMethod,Locale, TimeZone,java.time.ZoneId                                                                                                                                                                                                                                                                                                                                    
		resolvers.add(new ServletResponseMethodArgumentResolver());//ServletResponse,OutputStream,Writer
		resolvers.add(new HttpEntityMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));//HttpEntity,RequestEntity,ResponseEntity
		resolvers.add(new RedirectAttributesMethodArgumentResolver());//RedirectAttributes
		resolvers.add(new ModelMethodProcessor());//Model类型的参数和返回值
		resolvers.add(new MapMethodProcessor());//Map类型的参数和返回值
		resolvers.add(new ErrorsMethodArgumentResolver());//Errors
		resolvers.add(new SessionStatusMethodArgumentResolver());//SessionStatus
		resolvers.add(new UriComponentsBuilderMethodArgumentResolver());//UriComponentsBuilder

		// Custom arguments
		if (getCustomArgumentResolvers() != null) {
			resolvers.addAll(getCustomArgumentResolvers());
		}

		// Catch-all
		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));
		resolvers.add(new ServletModelAttributeMethodProcessor(true));

		return resolvers;
	}

```
