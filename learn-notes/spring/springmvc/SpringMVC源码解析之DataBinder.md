### DataBinder 数据绑定

```
public class DataBinder implements PropertyEditorRegistry, TypeConverter {

	/** Default object name used for binding: "target". */
	public static final String DEFAULT_OBJECT_NAME = "target";
	/** Default limit for array and collection growing: 256. */
	public static final int DEFAULT_AUTO_GROW_COLLECTION_LIMIT = 256;

	@Nullable
	private final Object target;
	private final String objectName; // 默认值是target

	// BindingResult：绑定错误、失败的时候会放进这里来
	@Nullable
	private AbstractPropertyBindingResult bindingResult;

	//类型转换器，会注册最为常用的那么多类型转换Map<Class<?>, PropertyEditor> defaultEditors
	@Nullable
	private SimpleTypeConverter typeConverter;

	// 默认忽略不能识别的字段
	private boolean ignoreUnknownFields = true;
	// 不能忽略非法的字段
	private boolean ignoreInvalidFields = false;
	// 默认是支持级联的
	private boolean autoGrowNestedPaths = true;

	private int autoGrowCollectionLimit = DEFAULT_AUTO_GROW_COLLECTION_LIMIT;

	// 这三个参数  都可以自己指定：允许的字段、不允许的、必须的
	@Nullable
	private String[] allowedFields;
	@Nullable
	private String[] disallowedFields;
	@Nullable
	private String[] requiredFields;

	// 转换器ConversionService
	@Nullable
	private ConversionService conversionService;
	// 状态码处理器
	@Nullable
	private MessageCodesResolver messageCodesResolver;
	// 绑定出现错误的处理器
	private BindingErrorProcessor bindingErrorProcessor = new DefaultBindingErrorProcessor();
	// 校验器
	private final List<Validator> validators = new ArrayList<>();

	// objectName没有指定，就用默认的
	public DataBinder(@Nullable Object target) {
		this(target, DEFAULT_OBJECT_NAME);
	}
	public DataBinder(@Nullable Object target, String objectName) {
		this.target = ObjectUtils.unwrapOptional(target);
		this.objectName = objectName;
	}
	// 提供一些列的初始化方法,供给子类使用 或者外部使用,下面两个初始化方法是互斥的
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
	// 注意：它支持xxx*,*xxx,*xxx*这样的通配符  支持[]这样子来写~
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
	public void setValidator(@Nullable Validator validator) {
		// 判断逻辑在下面：你的validator至少得支持这种类型呀
		assertValidators(validator);
		// 因为自己手动设置了，所以先清空  再加进来
		// 这步你会发现，即使validator是null
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
	// 此方法是protected的，子类WebDataBinder有复写加强了一下
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
	// protected 方法，给target赋值~~~~
	protected void applyPropertyValues(MutablePropertyValues mpvs) {
		try {
			// 可以看到最终赋值 是委托给PropertyAccessor去完成的
			getPropertyAccessor().setPropertyValues(mpvs, isIgnoreUnknownFields(), isIgnoreInvalidFields());

		// 抛出异常，交给BindingErrorProcessor一个个处理~~~
		} catch (PropertyBatchUpdateException ex) {
			for (PropertyAccessException pae : ex.getPropertyAccessExceptions()) {
				getBindingErrorProcessor().processPropertyAccessException(pae, getInternalBindingResult());
			}
		}
	}

	// 执行校验，此处就和BindingResult 关联上了，校验失败的消息都会放进去（不是直接抛出异常哦~ ）
	public void validate() {
		Object target = getTarget();
		Assert.state(target != null, "No target to validate");
		BindingResult bindingResult = getBindingResult();
		// 每个Validator都会执行~~~~
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