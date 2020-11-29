package com.nivelle.spring.utils;

import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;

import java.io.*;
import java.util.Properties;

/**
 * spring解析配置
 *
 * @author fuxinzhong
 * @date 2020/11/29
 */
public class PropertyPlaceHolderHelperDemo {

    private static Properties properties = new Properties();
    private static PropertyPlaceholderHelper holder = new PropertyPlaceholderHelper("@{", "}");
    private static PlaceholderResolver resolver = (placeholderName) -> {
        //return properties.getProperty(placeholderName);
        return "ok";
    };

    static {
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(new File("/Users/nivellefu/IdeaProjects/java-guides/spring-all/src/main/resources/propertiestest.properties")));
            properties.load(in);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        String kv = "@{keyname}:@{keyvalue}";

        System.out.println("替换前:========" + kv);

        String aa = holder.replacePlaceholders(kv, resolver);

        System.out.println("替换后:========" + aa);
    }
}
