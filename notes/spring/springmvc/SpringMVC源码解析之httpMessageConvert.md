### HttpMessageConvert

```
Spring框架中,HttpMessageConverter是一个策略接口,用于定义从HTTP请求读取消息或者往HTTP响应中写入消息的消息转换工具。

该接口有不同的实现。不同的实现逻辑体现了不同的"转换("converter)语义。每个实现可以仅仅支持从HTTP请求读取消息,或者仅仅支持往HTTP响应中写入消息，或者二者都支持
```
### AbstractMessageConverterMethodProcessor

```
	protected List<MediaType> getProducibleMediaTypes(
			HttpServletRequest request, Class<?> valueClass, @Nullable Type targetType) {

		Set<MediaType> mediaTypes =
				(Set<MediaType>) request.getAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
		if (!CollectionUtils.isEmpty(mediaTypes)) {
			return new ArrayList<>(mediaTypes);
		}
		else if (!this.allSupportedMediaTypes.isEmpty()) {
			List<MediaType> result = new ArrayList<>();
			for (HttpMessageConverter<?> converter : this.messageConverters) {
				if (converter instanceof GenericHttpMessageConverter && targetType != null) {
					if (((GenericHttpMessageConverter<?>) converter).canWrite(targetType, valueClass, null)) {
						result.addAll(converter.getSupportedMediaTypes());
					}
				}
				else if (converter.canWrite(valueClass, null)) {
					result.addAll(converter.getSupportedMediaTypes());
				}
			}
			return result;
		}
		else {
			return Collections.singletonList(MediaType.ALL);
		}
	}
	
	
	
	private List<MediaType> getAcceptableMediaTypes(HttpServletRequest request)
    			throws HttpMediaTypeNotAcceptableException {
    
    		return this.contentNegotiationManager.resolveMediaTypes(new ServletWebRequest(request));
    	}
    	
    	
   protected <T> void writeWithMessageConverters(@Nullable T value, MethodParameter returnType,
   			ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage)
   			throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {
   
   		Object body;
   		Class<?> valueType;
   		Type targetType;
   
   		if (value instanceof CharSequence) {
   			body = value.toString();
   			valueType = String.class;
   			targetType = String.class;
   		}
   		else {
   			body = value;
   			valueType = getReturnValueType(body, returnType);
   			targetType = GenericTypeResolver.resolveType(getGenericType(returnType), returnType.getContainingClass());
   		}
   
   		if (isResourceType(value, returnType)) {
   			outputMessage.getHeaders().set(HttpHeaders.ACCEPT_RANGES, "bytes");
   			if (value != null && inputMessage.getHeaders().getFirst(HttpHeaders.RANGE) != null &&
   					outputMessage.getServletResponse().getStatus() == 200) {
   				Resource resource = (Resource) value;
   				try {
   					List<HttpRange> httpRanges = inputMessage.getHeaders().getRange();
   					outputMessage.getServletResponse().setStatus(HttpStatus.PARTIAL_CONTENT.value());
   					body = HttpRange.toResourceRegions(httpRanges, resource);
   					valueType = body.getClass();
   					targetType = RESOURCE_REGION_LIST_TYPE;
   				}
   				catch (IllegalArgumentException ex) {
   					outputMessage.getHeaders().set(HttpHeaders.CONTENT_RANGE, "bytes */" + resource.contentLength());
   					outputMessage.getServletResponse().setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
   				}
   			}
   		}
   
   		MediaType selectedMediaType = null;
   		MediaType contentType = outputMessage.getHeaders().getContentType();
   		if (contentType != null && contentType.isConcrete()) {
   			if (logger.isDebugEnabled()) {
   				logger.debug("Found 'Content-Type:" + contentType + "' in response");
   			}
   			selectedMediaType = contentType;
   		}
   		else {
   			HttpServletRequest request = inputMessage.getServletRequest();
   			List<MediaType> acceptableTypes = getAcceptableMediaTypes(request);
   			List<MediaType> producibleTypes = getProducibleMediaTypes(request, valueType, targetType);
   
   			if (body != null && producibleTypes.isEmpty()) {
   				throw new HttpMessageNotWritableException(
   						"No converter found for return value of type: " + valueType);
   			}
   			List<MediaType> mediaTypesToUse = new ArrayList<>();
   			for (MediaType requestedType : acceptableTypes) {
   				for (MediaType producibleType : producibleTypes) {
   					if (requestedType.isCompatibleWith(producibleType)) {
   						mediaTypesToUse.add(getMostSpecificMediaType(requestedType, producibleType));
   					}
   				}
   			}
   			if (mediaTypesToUse.isEmpty()) {
   				if (body != null) {
   					throw new HttpMediaTypeNotAcceptableException(producibleTypes);
   				}
   				if (logger.isDebugEnabled()) {
   					logger.debug("No match for " + acceptableTypes + ", supported: " + producibleTypes);
   				}
   				return;
   			}
   
   			MediaType.sortBySpecificityAndQuality(mediaTypesToUse);
   
   			for (MediaType mediaType : mediaTypesToUse) {
   				if (mediaType.isConcrete()) {
   					selectedMediaType = mediaType;
   					break;
   				}
   				else if (mediaType.isPresentIn(ALL_APPLICATION_MEDIA_TYPES)) {
   					selectedMediaType = MediaType.APPLICATION_OCTET_STREAM;
   					break;
   				}
   			}
   
   			if (logger.isDebugEnabled()) {
   				logger.debug("Using '" + selectedMediaType + "', given " +
   						acceptableTypes + " and supported " + producibleTypes);
   			}
   		}
   
   		if (selectedMediaType != null) {
   			selectedMediaType = selectedMediaType.removeQualityValue();
   			for (HttpMessageConverter<?> converter : this.messageConverters) {
   				GenericHttpMessageConverter genericConverter = (converter instanceof GenericHttpMessageConverter ?
   						(GenericHttpMessageConverter<?>) converter : null);
   				if (genericConverter != null ?
   						((GenericHttpMessageConverter) converter).canWrite(targetType, valueType, selectedMediaType) :
   						converter.canWrite(valueType, selectedMediaType)) {
   					body = getAdvice().beforeBodyWrite(body, returnType, selectedMediaType,
   							(Class<? extends HttpMessageConverter<?>>) converter.getClass(),
   							inputMessage, outputMessage);
   					if (body != null) {
   						Object theBody = body;
   						LogFormatUtils.traceDebug(logger, traceOn ->
   								"Writing [" + LogFormatUtils.formatValue(theBody, !traceOn) + "]");
   						addContentDispositionHeader(inputMessage, outputMessage);
   						if (genericConverter != null) {
   							genericConverter.write(body, targetType, selectedMediaType, outputMessage);
   						}
   						else {
   							((HttpMessageConverter) converter).write(body, selectedMediaType, outputMessage);
   						}
   					}
   					else {
   						if (logger.isDebugEnabled()) {
   							logger.debug("Nothing to write: null body");
   						}
   					}
   					return;
   				}
   			}
   		}
   
   		if (body != null) {
   			throw new HttpMediaTypeNotAcceptableException(this.allSupportedMediaTypes);
   		}
   	}

```