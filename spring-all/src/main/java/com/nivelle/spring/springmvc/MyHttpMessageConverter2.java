package com.nivelle.spring.springmvc;

import com.nivelle.spring.pojo.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

public class MyHttpMessageConverter2 extends AbstractGenericHttpMessageConverter<User> {

    public MyHttpMessageConverter2() {
        super(new MediaType("application", "myContentType",Charset.forName("UTF-8")));
        System.out.println("MyHttpMessageConverter2 constroct");
    }

    @Override
    public boolean supports(Class<?> clazz) {
        System.out.println("MyHttpMessageConverter2 supports class name:" + clazz.getName()+";supports:"+User.class.isAssignableFrom(clazz));
        return User.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        System.out.println("MyHttpMessageConverter2 canRead:" + (type instanceof Class ? canRead((Class<?>) type, mediaType) : canRead(mediaType))
                + "type:" + type + " " + "contextClass:" + contextClass + " " + "mediaType:" + " " + mediaType);

        System.out.println("MyHttpMessageConverter2 can read:"+((type instanceof Class ? canRead((Class<?>) type, mediaType) : canRead(mediaType))));
        return (type instanceof Class ? canRead((Class<?>) type, mediaType) : canRead(mediaType));
    }

    @Override
    public boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType) {
        System.out.println("MyHttpMessageConverter2 canWrite:" + (type instanceof Class ? canRead((Class<?>) type, mediaType) : canRead(mediaType))
                + "  type:" + type + " " + "clazz:" + clazz + " " + "mediaType:" + mediaType);

        System.out.println("MyHttpMessageConverter2 can write:"+canWrite(clazz, mediaType));
        return canWrite(clazz, mediaType);
    }

    /**
     * 重写writeInternal ，处理如何输出数据到response
     *
     * @param user
     * @param type
     * @param outputMessage
     * @throws IOException
     * @throws HttpMessageNotWritableException
     */
    @Override
    protected void writeInternal(User user, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        System.out.println("MyHttpMessageConverter2 =>writeInternal,user:" + user + "\n" + "type:" + type + "\n HttpOutputMessage:" + outputMessage);
        // 获取请求头
        HttpHeaders headers = outputMessage.getHeaders();
        System.out.println("MyHttpMessageConverter2 headers: " + headers);
        // 获取 content-type
        MediaType contentType = headers.getContentType();
        System.out.println("MyHttpMessageConverter2 contentType: " + contentType);
        outputMessage.getHeaders().set(HttpHeaders.CONTENT_TYPE,"application/myContentType");
        Integer age = user.getAge();
        String name = user.getName();
        String response =  "fuck:"+"name:"+name+"age is:"+age;
        outputMessage.getBody().write(response.getBytes());
    }

    /**
     * 处理请求的数据
     *
     * @param clazz
     * @param inputMessage
     * @return
     * @throws IOException
     * @throws HttpMessageNotReadableException
     */
    @Override
    protected User readInternal(Class<? extends User> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        System.out.println("MyHttpMessageConverter2 =>readInternal,clazz:" + clazz + "\n" + "inputMessage:" + inputMessage);

        // 获取请求头
        HttpHeaders headers = inputMessage.getHeaders();
        System.out.println("MyHttpMessageConverter2 readInternal headers: " + headers);
        // 获取 content-type
        MediaType contentType = headers.getContentType();
        // 获取编码
        Charset charset = null;
        if (contentType != null) {
            charset = contentType.getCharset();
        }
        System.out.println("MyHttpMessageConverter2 readInternal contentType: " + contentType);
        charset = charset == null ? Charset.forName("UTF-8") : charset;
        String temp = StreamUtils.copyToString(inputMessage.getBody(), charset);
        String[] array = temp.split("-");
        User user = new User();
        user.setAge(Integer.parseInt(array[0]));
        user.setName(array[1]);
        return user;
    }

    @Override
    public User read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return readInternal(null, inputMessage);
    }
}
