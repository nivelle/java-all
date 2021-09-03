### DataBinder 数据绑定

- 它位于spring-context这个工程的org.springframework.validation包内，所以需要再次明确的是：它是Spring提供的能力而非web提供的

```
public class DataBinder implements PropertyEditorRegistry, TypeConverter {

	/** Default object name used for binding: "target". */
	public static final String DEFAULT_OBJECT_NAME = "target";
	/** Default limit for array and collection growing: 256. */
	public static final int DEFAULT_AUTO_GROW_COLLECTION_LIMIT = 256;

	@Nullable
	private final Object target;
	private final String objectName; // 默认值是target

	// BindingResult：绑定错误、失败的时候会放进这里来~
	@Nullable
	private AbstractPropertyBindingResult bindingResult;

	//类型转换器，会注册常用的类型转换Map<Class<?>, PropertyEditor> defaultEditors
	@Nullable
	private SimpleTypeConverter typeConverter;

	// 默认忽略不能识别的字段
	private boolean ignoreUnknownFields = true;
	// 不能忽略非法的字段（比如我要Integer，你给传aaa，那肯定就不让绑定了，抛错）
	private boolean ignoreInvalidFields = false;
	// 默认是支持级联的
	private boolean autoGrowNestedPaths = true;

	private int autoGrowCollectionLimit = DEFAULT_AUTO_GROW_COLLECTION_LIMIT;

	// 这三个参数  都可以自己指定:允许的字段、不允许的、必须的
	@Nullable
	private String[] allowedFields;
	@Nullable
	private String[] disallowedFields;
	@Nullable
	private String[] requiredFields;

	// 转换器ConversionService
	@Nullable
	private ConversionService conversionService;
	// 状态码处理器~
	@Nullable
	private MessageCodesResolver messageCodesResolver;
	// 绑定出现错误的处理器~
	private BindingErrorProcessor bindingErrorProcessor = new DefaultBindingErrorProcessor();
	// 校验器（这个非常重要）
	private final List<Validator> validators = new ArrayList<>();

	//  objectName没有指定，就用默认的
	public DataBinder(@Nullable Object target) {
		this(target, DEFAULT_OBJECT_NAME);
	}
	public DataBinder(@Nullable Object target, String objectName) {
		this.target = ObjectUtils.unwrapOptional(target);
		this.objectName = objectName;
	}

	// 提供一些列的初始化方法，供给子类使用 或者外部使用;initBeanPropertyAccess 和 initDirectFieldAccess 是互斥的
	public void initBeanPropertyAccess() {
		Assert.state(this.bindingResult == null, "DataBinder is already initialized - call initBeanPropertyAccess before other configuration methods");
		this.bindingResult = createBeanPropertyBindingResult();
	}
	protected AbstractPropertyBindingResult createBeanPropertyBindingResult() {
		BeanPropertyBindingResult result = new BeanPropertyBindingResult(getTarget(), getObjectName(), isAutoGrowNestedPaths(), getAutoGrowCollectionLimit());
		if (this.conversionService != null) {
			result.initConversion(this.conversionService);
		}
		if (this.messageCodesResolver != null) {
			result.setMessageCodesResolver(this.messageCodesResolver);
		}
		return result;
	}
	// 你会发现，初始化DirectFieldAccess的时候，校验的也是bindingResult
	public void initDirectFieldAccess() {
		Assert.state(this.bindingResult == null, "DataBinder is already initialized - call initDirectFieldAccess before other configuration methods");
		this.bindingResult = createDirectFieldBindingResult();
	}
	protected AbstractPropertyBindingResult createDirectFieldBindingResult() {
		DirectFieldBindingResult result = new DirectFieldBindingResult(getTarget(), getObjectName(), isAutoGrowNestedPaths());
		if (this.conversionService != null) {
			result.initConversion(this.conversionService);
		}
		if (this.messageCodesResolver != null) {
			result.setMessageCodesResolver(this.messageCodesResolver);
		}
		return result;
	}

	...
	// 把属性访问器返回，PropertyAccessor(默认直接从结果里拿)，子类MapDataBinder有复写
	protected ConfigurablePropertyAccessor getPropertyAccessor() {
		return getInternalBindingResult().getPropertyAccessor();
	}

	// 可以看到简单的转换器也是使用到了conversionService的，可见conversionService它的效用
	protected SimpleTypeConverter getSimpleTypeConverter() {
		if (this.typeConverter == null) {
			this.typeConverter = new SimpleTypeConverter();
			if (this.conversionService != null) {
				this.typeConverter.setConversionService(this.conversionService);
			}
		}
		return this.typeConverter;
	}
	
	// 设置指定的可以绑定的字段，默认是所有字段
	// 例如，在绑定HTTP请求参数时，限制这一点以避免恶意用户进行不必要的修改。
	// 简单的说：我可以控制只有指定的一些属性才允许你修改
	// 注意：它支持xxx*,*xxx,*xxx*这样的通配符  支持[]这样子来写
	public void setAllowedFields(@Nullable String... allowedFields) {
		this.allowedFields = PropertyAccessorUtils.canonicalPropertyNames(allowedFields);
	}
	public void setDisallowedFields(@Nullable String... disallowedFields) {
		this.disallowedFields = PropertyAccessorUtils.canonicalPropertyNames(disallowedFields);
	}

	// 注册每个绑定进程所必须的字段。
	public void setRequiredFields(@Nullable String... requiredFields) {
		this.requiredFields = PropertyAccessorUtils.canonicalPropertyNames(requiredFields);
		if (logger.isDebugEnabled()) {
			logger.debug("DataBinder requires binding of required fields [" + StringUtils.arrayToCommaDelimitedString(requiredFields) + "]");
		}
	}
	// 注意：这个是set方法，后面是有add方法的~
	// 注意：虽然是set，但是引用是木有变的
	public void setValidator(@Nullable Validator validator) {
		// 判断逻辑在下面：你的validator至少得支持这种类型呀 
		assertValidators(validator);
		// 因为自己手动设置了，所以先清空  再加进来
		// 这步你会发现，即使validator是null，也是会clear的哦~  符合语意
		this.validators.clear();
		if (validator != null) {
			this.validators.add(validator);
		}
	}
	private void assertValidators(Validator... validators) {
		Object target = getTarget();
		for (Validator validator : validators) {
			if (validator != null && (target != null && !validator.supports(target.getClass()))) {
				throw new IllegalStateException("Invalid target for Validator [" + validator + "]: " + target);
			}
		}
	}
	public void addValidators(Validator... validators) {
		assertValidators(validators);
		this.validators.addAll(Arrays.asList(validators));
	}
	// 效果同set
	public void replaceValidators(Validator... validators) {
		assertValidators(validators);
		this.validators.clear();
		this.validators.addAll(Arrays.asList(validators));
	}
	
	// 返回一个，也就是primary默认的校验器
	@Nullable
	public Validator getValidator() {
		return (!this.validators.isEmpty() ? this.validators.get(0) : null);
	}
	// 只读视图
	public List<Validator> getValidators() {
		return Collections.unmodifiableList(this.validators);
	}

	// since Spring 3.0
	public void setConversionService(@Nullable ConversionService conversionService) {
		Assert.state(this.conversionService == null, "DataBinder is already initialized with ConversionService");
		this.conversionService = conversionService;
		if (this.bindingResult != null && conversionService != null) {
			this.bindingResult.initConversion(conversionService);
		}
	}

	// =============下面它提供了非常多的addCustomFormatter()方法  注册进PropertyEditorRegistry里=====================
	public void addCustomFormatter(Formatter<?> formatter);
	public void addCustomFormatter(Formatter<?> formatter, String... fields);
	public void addCustomFormatter(Formatter<?> formatter, Class<?>... fieldTypes);

	// 实现接口方法
	public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor);
	public void registerCustomEditor(@Nullable Class<?> requiredType, @Nullable String field, PropertyEditor propertyEditor);
	...
	// 实现接口方法
	// 统一委托给持有的TypeConverter或者是getInternalBindingResult().getPropertyAccessor();这里面的
	@Override
	@Nullable
	public <T> T convertIfNecessary(@Nullable Object value, @Nullable Class<T> requiredType,
			@Nullable MethodParameter methodParam) throws TypeMismatchException {

		return getTypeConverter().convertIfNecessary(value, requiredType, methodParam);
	}


	// ===========上面的方法都是开胃小菜，下面才是本类最重要的方法==============

	// 该方法就是把提供的属性值们，绑定到目标对象target里去~~~
	public void bind(PropertyValues pvs) {
		MutablePropertyValues mpvs = (pvs instanceof MutablePropertyValues ? (MutablePropertyValues) pvs : new MutablePropertyValues(pvs));
		doBind(mpvs);
	}
	// 此方法是protected的，子类WebDataBinder有复写
	protected void doBind(MutablePropertyValues mpvs) {
		// 前面两个check就不解释了，重点看看applyPropertyValues(mpvs)这个方法~
		checkAllowedFields(mpvs);
		checkRequiredFields(mpvs);
		applyPropertyValues(mpvs);
	}

	// allowe允许的 并且还是没有在disallowed里面的 这个字段就是被允许的
	protected boolean isAllowed(String field) {
		String[] allowed = getAllowedFields();
		String[] disallowed = getDisallowedFields();
		return ((ObjectUtils.isEmpty(allowed) || PatternMatchUtils.simpleMatch(allowed, field)) &&
				(ObjectUtils.isEmpty(disallowed) || !PatternMatchUtils.simpleMatch(disallowed, field)));
	}
	...
	// protected 方法，给target赋值
	protected void applyPropertyValues(MutablePropertyValues mpvs) {
		try {
			// 可以看到最终赋值 是委托给PropertyAccessor去完成的
			getPropertyAccessor().setPropertyValues(mpvs, isIgnoreUnknownFields(), isIgnoreInvalidFields());

		// 抛出异常，交给BindingErrorProcessor一个个处理
		} catch (PropertyBatchUpdateException ex) {
			for (PropertyAccessException pae : ex.getPropertyAccessExceptions()) {
				getBindingErrorProcessor().processPropertyAccessException(pae, getInternalBindingResult());
			}
		}
	}

	// 执行校验，此处就和BindingResult 关联上了，校验失败的消息都会放进去
	public void validate() {
		Object target = getTarget();
		Assert.state(target != null, "No target to validate");
		BindingResult bindingResult = getBindingResult();
		// 每个Validator都会执行
		for (Validator validator : getValidators()) {
			validator.validate(target, bindingResult);
		}
	}

	// 带有校验提示的校验器。SmartValidator
	// @since 3.1
	public void validate(Object... validationHints) { ... }

	// 这一步也挺有意思：实际上就是若有错误，就抛出异常
	// 若没错误  就把绑定的Model返回~~~(可以看到BindingResult里也能拿到最终值哦~~~)
	// 此方法可以调用，但一般较少使用~
	public Map<?, ?> close() throws BindException {
		if (getBindingResult().hasErrors()) {
			throw new BindException(getBindingResult());
		}
		return getBindingResult().getModel();
	}
}


```

- 把属性值PropertyValues绑定到target上（bind()方法，依赖于PropertyAccessor实现）
- 提供校验的能力：提供了public方法validate()对各个属性使用Validator执行校验~
- 提供了注册属性编辑器（PropertyEditor）和对类型进行转换的能力（TypeConverter）

### webDataBinder

````
// @since 1.2
public class WebDataBinder extends DataBinder {

	// 此字段意思是：字段标记  比如name -> _name; 这对于HTML复选框和选择选项特别有用。
	public static final String DEFAULT_FIELD_MARKER_PREFIX = "_";
	// !符号是处理默认值的，提供一个默认值代替空值
	public static final String DEFAULT_FIELD_DEFAULT_PREFIX = "!";
	
	@Nullable
	private String fieldMarkerPrefix = DEFAULT_FIELD_MARKER_PREFIX;
	@Nullable
	private String fieldDefaultPrefix = DEFAULT_FIELD_DEFAULT_PREFIX;
	// 默认也会绑定空的文件流
	private boolean bindEmptyMultipartFiles = true;

	// 完全沿用父类的两个构造
	public WebDataBinder(@Nullable Object target) {
		super(target);
	}
	public WebDataBinder(@Nullable Object target, String objectName) {
		super(target, objectName);
	}

	// 在父类的基础上，增加了对_和!的处理
	@Override
	protected void doBind(MutablePropertyValues mpvs) {
		checkFieldDefaults(mpvs);
		checkFieldMarkers(mpvs);
		super.doBind(mpvs);
	}

    // 处理！ 开头属性的逻辑
	protected void checkFieldDefaults(MutablePropertyValues mpvs) {
		String fieldDefaultPrefix = getFieldDefaultPrefix();
		if (fieldDefaultPrefix != null) {
			PropertyValue[] pvArray = mpvs.getPropertyValues();
			for (PropertyValue pv : pvArray) {

				// 若你给定的PropertyValue的属性名确实是以!打头的  那就做处理如下：
				// 1. 如果JavaBean的该属性可写 同时 mpvs不存在去掉!后的同名属性，那就添加进来表示后续可以使用了（毕竟是默认值，没有精确匹配的高的）
				// 2. 然后把带!的给移除掉（因为默认值以已经转正了）其实这里就是说你可以使用！来给个默认值。比如!name表示若找不到name这个属性的时，就取它的值 也就是说你request里若有传!name保底，也就不怕出现null值了
				// 3. 也就是 @Required(required=true ,default = defaultVale) 
				if (pv.getName().startsWith(fieldDefaultPrefix)) {
					String field = pv.getName().substring(fieldDefaultPrefix.length());
					if (getPropertyAccessor().isWritableProperty(field) && !mpvs.contains(field)) {
						mpvs.add(field, pv.getValue());
					}
					mpvs.removePropertyValue(pv);
				}
			}
		}
	}

	// 处理"_" 开头的逻辑 
	// 1. 若传入的字段以_打头 JavaBean的这个属性可写 同时 mpvs没有去掉_后的属性名字
	// 2. 则 getEmptyValue(field, fieldType)就是根据Type类型给定默认值。
	// 比如Boolean类型默认给false，数组给空数组[]，集合给空集合，Map给空map  可以参考此类：CollectionFactory 当然，这一切都是建立在你传的属性值是以_打头的基础上的，Spring才会默认帮你处理这些默认值
	protected void checkFieldMarkers(MutablePropertyValues mpvs) {
		String fieldMarkerPrefix = getFieldMarkerPrefix();
		if (fieldMarkerPrefix != null) {
			PropertyValue[] pvArray = mpvs.getPropertyValues();
			for (PropertyValue pv : pvArray) {
				if (pv.getName().startsWith(fieldMarkerPrefix)) {
					String field = pv.getName().substring(fieldMarkerPrefix.length());
					if (getPropertyAccessor().isWritableProperty(field) && !mpvs.contains(field)) {
						Class<?> fieldType = getPropertyAccessor().getPropertyType(field);
						mpvs.add(field, getEmptyValue(field, fieldType));
					}
					mpvs.removePropertyValue(pv);
				}
			}
		}
	}

	// @since 5.0
	@Nullable
	public Object getEmptyValue(Class<?> fieldType) {
		try {
			if (boolean.class == fieldType || Boolean.class == fieldType) {
				// Special handling of boolean property.
				return Boolean.FALSE;
			} else if (fieldType.isArray()) {
				// Special handling of array property.
				return Array.newInstance(fieldType.getComponentType(), 0);
			} else if (Collection.class.isAssignableFrom(fieldType)) {
				return CollectionFactory.createCollection(fieldType, 0);
			} else if (Map.class.isAssignableFrom(fieldType)) {
				return CollectionFactory.createMap(fieldType, 0);
			}
		} catch (IllegalArgumentException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to create default value - falling back to null: " + ex.getMessage());
			}
		}
		// 若不在这几大类型内，就返回默认值null呗
		// 但需要说明的是，若你是简单类型比如int，
		// Default value: null. 
		return null;
	}

	// 单独提供的方法，用于绑定org.springframework.web.multipart.MultipartFile类型的数据到JavaBean属性上~
	// 显然默认是允许MultipartFile作为Bean一个属性  参与绑定的
	// Map<String, List<MultipartFile>>它的key，一般来说就是文件
	protected void bindMultipart(Map<String, List<MultipartFile>> multipartFiles, MutablePropertyValues mpvs) {
		multipartFiles.forEach((key, values) -> {
			if (values.size() == 1) {
				MultipartFile value = values.get(0);
				if (isBindEmptyMultipartFiles() || !value.isEmpty()) {
					mpvs.add(key, value);
				}
			}
			else {
				mpvs.add(key, values);
			}
		});
	}
}


````

- 支持对属性名以"_"打头的默认值处理（自动设置默认值，根据类型来处理 所有的Bool、Collection、Map等）
- 支持对属性名以"!"打头的默认值处理（手动给某个属性赋默认值，自己控制的灵活性很高）
- 提供方法，支持把MultipartFile绑定到JavaBean的属性上

### ServletRequestDataBinder

````
public class ServletRequestDataBinder extends WebDataBinder {
	// 沿用父类构造
	// 注意这个可不是父类的方法，是本类增强的,意思就是kv都从request里来,当然内部还是适配成了一个MutablePropertyValues
	public void bind(ServletRequest request) {
		// 内部最核心方法是它：WebUtils.getParametersStartingWith()  把request参数转换成一个Map, request.getParameterNames()
		MutablePropertyValues mpvs = new ServletRequestParameterPropertyValues(request);
		MultipartRequest multipartRequest = WebUtils.getNativeRequest(request, MultipartRequest.class);
	
		// 调用父类的bindMultipart方法，把MultipartFile都放进MutablePropertyValues里去
		if (multipartRequest != null) {
			bindMultipart(multipartRequest.getMultiFileMap(), mpvs);
		}
		// 这个方法是本类流出来的一个扩展点，子类可以复写此方法自己往里继续添加
		// 比如ExtendedServletRequestDataBinder它就复写了这个方法，进行了增强（下面会说）  支持到了uriTemplateVariables的绑定
		addBindValues(mpvs, request);
		doBind(mpvs);
	}

	// 这个方法和父类的close方法类似，很少直接调用
	public void closeNoCatch() throws ServletRequestBindingException {
		if (getBindingResult().hasErrors()) {
			throw new ServletRequestBindingException("Errors binding onto object '" + getBindingResult().getObjectName() + "'", new BindException(getBindingResult()));
		}
	}
}


````

### ExtendedServletRequestDataBinder

- 它是对ServletRequestDataBinder的一个增强，它用于把URI template variables参数添加进来用于绑定。它会去从request的HandlerMapping.class.getName() + ".uriTemplateVariables";这个属性里查找到值出来用于绑定

- 比如我们熟悉的@PathVariable它就和这相关：它负责把参数从url模版中解析出来，然后放在attr上，最后交给ExtendedServletRequestDataBinder进行绑定

````
// @since 3.1
public class ExtendedServletRequestDataBinder extends ServletRequestDataBinder {
	//本类的唯一方法
	@Override
	@SuppressWarnings("unchecked")
	protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
		// 它的值是：HandlerMapping.class.getName() + ".uriTemplateVariables";
		String attr = HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
		// 注意: 此处是attr，而不是parameter
		Map<String, String> uriVars = (Map<String, String>) request.getAttribute(attr);
		if (uriVars != null) {
			uriVars.forEach((name, value) -> {				
				// 若已经存在确切的key了，不会覆盖
				if (mpvs.contains(name)) {
					if (logger.isWarnEnabled()) {
						logger.warn("Skipping URI variable '" + name + "' because request contains bind value with same name.");
					}
				} else {
					mpvs.addPropertyValue(name, value);
				}
			});
		}
	}
}

````

### MapDataBinder

- 专门用于处理target是Map<String, Object>类型的目标对象的绑定，它并非一个public类
  
- 它用的属性访问器是MapPropertyAccessor：一个继承自AbstractPropertyAccessor的私有静态内部类,也支持到了SpEL

### ConfigurableWebBindingInitializer

````
public class ConfigurableWebBindingInitializer implements WebBindingInitializer {
	private boolean autoGrowNestedPaths = true;
	private boolean directFieldAccess = false; // 显然这里是false

	// 下面这些参数，不就是WebDataBinder那些可以配置的属性们吗？
	@Nullable
	private MessageCodesResolver messageCodesResolver;
	@Nullable
	private BindingErrorProcessor bindingErrorProcessor;
	@Nullable
	private Validator validator;
	@Nullable
	private ConversionService conversionService;
	// 此处使用的PropertyEditorRegistrar来管理的，最终都会被注册进PropertyEditorRegistry嘛
	@Nullable
	private PropertyEditorRegistrar[] propertyEditorRegistrars;

	... //  省略所有get/set
	
	// 它做的事无非就是把配置的值都放进去而已~~
	@Override
	public void initBinder(WebDataBinder binder) {
		binder.setAutoGrowNestedPaths(this.autoGrowNestedPaths);
		if (this.directFieldAccess) {
			binder.initDirectFieldAccess();
		}
		if (this.messageCodesResolver != null) {
			binder.setMessageCodesResolver(this.messageCodesResolver);
		}
		if (this.bindingErrorProcessor != null) {
			binder.setBindingErrorProcessor(this.bindingErrorProcessor);
		}
		// 可以看到对校验器这块  内部还是做了容错的
		if (this.validator != null && binder.getTarget() != null && this.validator.supports(binder.getTarget().getClass())) {
			binder.setValidator(this.validator);
		}
		if (this.conversionService != null) {
			binder.setConversionService(this.conversionService);
		}
		if (this.propertyEditorRegistrars != null) {
			for (PropertyEditorRegistrar propertyEditorRegistrar : this.propertyEditorRegistrars) {
				propertyEditorRegistrar.registerCustomEditors(binder);
			}
		}
	}
}


````