
### RequestCondition 

````
package org.springframework.web.servlet.mvc.condition;

import javax.servlet.http.HttpServletRequest;

import org.springframework.lang.Nullable;

public interface RequestCondition<T> {

	// 和另外一个请求匹配条件合并，具体合并逻辑由实现类提供
	T combine(T other);

	// 检查当前请求匹配条件和指定请求request是否匹配，如果不匹配返回null，
	// 如果匹配，生成一个新的请求匹配条件，该新的请求匹配条件是当前请求匹配条件针对指定请求request的剪裁。
	// 举个例子来讲，如果当前请求匹配条件是一个路径匹配条件，包含多个路径匹配模板，并且其中有些模板和指定请求request匹配，那么返回的新建的请求匹配条件将仅仅
	// 包含和指定请求request匹配的那些路径模板。
	@Nullable
	T getMatchingCondition(HttpServletRequest request);

	// 针对指定的请求对象request比较两个请求匹配条件。
	// 该方法假定被比较的两个请求匹配条件都是针对该请求对象request调用了 #getMatchingCondition方法得到的，这样才能确保对它们的比较是针对同一个请求对象request，这样的比较才有意义(最终用来确定谁是更匹配的条件)。
	int compareTo(T other, HttpServletRequest request);

}

````

### public abstract class AbstractRequestCondition<T extends AbstractRequestCondition<T>> implements RequestCondition<T> 

//RequestCondition 接口的实现 
```
package org.springframework.web.servlet.mvc.condition;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.lang.Nullable;

/**
 * @param <T> the type of objects that this RequestCondition can be combined
 * with and compared to
 */
public abstract class AbstractRequestCondition<T extends AbstractRequestCondition<T>> implements RequestCondition<T> {

	/**
	 * 当前请求匹配条件对象是否内容为空
	 * @return  true if empty; false otherwise
	 */
	public boolean isEmpty() {
		return getContent().isEmpty();
	}

	/**
	 * 一个请求匹配条件可能由多个部分组成，这些组成部分被包装成一个名为 content 的集合
	 * 比如 ：
	 * 对于请求路径匹配条件，可能有多个 URL pattern,
	 * 对于请求方法匹配条件，可能有多个 HTTP request method,
	 * 对于请求参数匹配条件，可能有多个 param 表达式 .
	 * @return a collection of objects, never  null ， 可能为空集合
	 */
	protected abstract Collection<?> getContent();

	/**
	 * The notation to use when printing discrete items of content.
	 * 将该条件作为字符串展示时，各个组成部分之间的中缀标识符。比如 "||" 或者 "&&" 等。
	 * For example {@code " || " for URL patterns or {@code " && "} for param expressions.
	 */
	protected abstract String getToStringInfix();


	// equlas 实现 
	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		return getContent().equals(((AbstractRequestCondition<?>) other).getContent());
	}

	// hashCode 实现
	@Override
	public int hashCode() {
		return getContent().hashCode();
	}

	// toString 实现
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("[");
		for (Iterator<?> iterator = getContent().iterator(); iterator.hasNext();) {
			Object expression = iterator.next();
			builder.append(expression.toString());
			if (iterator.hasNext()) {
				builder.append(getToStringInfix());
			}
		}
		builder.append("]");
		return builder.toString();
	}

}
```

#### 针对某种请求匹配条件具体实现类

- PatternsRequestCondition//路径匹配条件

- RequestMethodsRequestCondition//请求方法匹配条件

- ParamsRequestCondition // 请求参数匹配条件

- HeadersRequestCondition //头部信息匹配条件

- ConsumesRequestCondition //可消费MIME匹配条件

- ProducesRequestCondition // 可生成MIME匹配条件

