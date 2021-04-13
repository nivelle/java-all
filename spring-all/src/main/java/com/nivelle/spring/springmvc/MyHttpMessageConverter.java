package com.nivelle.spring.springmvc;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * 自定义 properties 类型参数转换
 * <p>
 * HttpMessageConverter虽然功能上也表现为HttpMessage与任意类型的转换，但其接口和Convert SPI并没有继承关系。
 * <p>
 * HttpMessageConverter属于spring-web。
 * <p>
 * HttpMessage是SpringMVC对Servlet规范中HttpServletRequest和HttpServletResponse的包装，因此接受请求时需要把HttpMessage转换成用户需要的数据，在生成响应时需要把用户生成的数据转换成HttpMessage。
 *
 * 如果用户在XML的<mvc:message-converters>中没有指定register-defaults=false，SpringMVC默认至少会注册一些自带的HttpMessageConvertor
 * <p>
 * 从先后顺序排列分别为:
 * <p>
 * 1. ByteArrayHttpMessageConverter
 * 2. StringHttpMessageConverter
 * 3. ResourceHttpMessageConverter
 * 4. SourceHttpMessageConverter
 * 5. AllEncompassingFormHttpMessageConverter
 */
//若不指定消息转换器，则优先使用后定义后加入的消息转换器。 通过 Accept 消息头来指定
public class MyHttpMessageConverter extends AbstractGenericHttpMessageConverter<Properties> {

    public MyHttpMessageConverter() {
        super(new MediaType("text", "properties"));
        System.out.println("MyHttpMessageConverter construt");
    }

    @Override
    public boolean supports(Class<?> clazz) {
        System.out.println("MyHttpMessageConverter supports class name:" + clazz.getName());
        if (clazz.getName().equals(Properties.class.getName())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        System.out.println("MyHttpMessageConverter canRead:" + (type instanceof Class ? canRead((Class<?>) type, mediaType) : canRead(mediaType))
                + "type:" + type + " " + "contextClass:" + contextClass + " " + "mediaType:" + " " + mediaType);
        return (type instanceof Class ? canRead((Class<?>) type, mediaType) : canRead(mediaType));
    }

    @Override
    public boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType) {
        System.out.println("MyHttpMessageConverter canWrite:" + (type instanceof Class ? canRead((Class<?>) type, mediaType) : canRead(mediaType))
                + "  type:" + type + " " + "clazz:" + clazz + " " + "mediaType:" + mediaType);
        return canWrite(clazz, mediaType);
    }

    @Override
    protected void writeInternal(Properties properties, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        System.out.println("MyHttpMessageConverter =>writeInternal,properties:" + properties + "\n" + "type:" + type + "\n HttpOutputMessage:" + outputMessage);
        // 获取请求头
        HttpHeaders headers = outputMessage.getHeaders();
        System.out.println("MyHttpMessageConverter headers: " + headers);
        // 获取 content-type
        MediaType contentType = headers.getContentType();
        System.out.println("MyHttpMessageConverter contentType: " + contentType);

        // 获取编码
        Charset charset = null;
        if (contentType != null) {
            charset = contentType.getCharset();
        }
        charset = charset == null ? Charset.forName("UTF-8") : charset;
        // 获取请求体
        OutputStream body = outputMessage.getBody();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(body, charset);
        properties.store(outputStreamWriter, "PropertiesHttpMessageConverter#writeInternal");
    }

    @Override
    protected Properties readInternal(Class<? extends Properties> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        System.out.println("MyHttpMessageConverter =>readInternal,clazz:" + clazz + "\n" + "inputMessage:" + inputMessage);

        Properties properties = new Properties();
        // 获取请求头
        HttpHeaders headers = inputMessage.getHeaders();
        System.out.println("MyHttpMessageConverter readInternal headers: " + headers);

        // 获取 content-type
        MediaType contentType = headers.getContentType();
        // 获取编码
        Charset charset = null;
        if (contentType != null) {
            charset = contentType.getCharset();
        }
        System.out.println("MyHttpMessageConverter readInternal contentType: " + contentType);
        charset = charset == null ? Charset.forName("UTF-8") : charset;
        // 获取请求体
        InputStream body = inputMessage.getBody();
        InputStreamReader inputStreamReader = new InputStreamReader(body, charset);
        properties.load(inputStreamReader);
        return properties;
    }

    @Override
    public Properties read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return readInternal(null, inputMessage);
    }
}
