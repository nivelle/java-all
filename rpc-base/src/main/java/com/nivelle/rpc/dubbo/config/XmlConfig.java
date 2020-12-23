package com.nivelle.rpc.dubbo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
/**
 * 导入xml配置文件
 *
 * @author nivelle
 * @date 2019/08/21
 */
@Configuration
@ImportResource(locations={"classpath:beans.xml"})
public class XmlConfig {
}
