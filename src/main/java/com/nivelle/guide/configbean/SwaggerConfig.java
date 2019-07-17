package com.nivelle.guide.configbean;


import io.swagger.annotations.Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author fuxinzhong
 * @date 2019/07/17
 */
@Configuration
@EnableSwagger2
@Api(value = "swagger测试", description = "swagger测试 API", position = 100, protocols = "http")
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 自行修改为自己的包路径
                .apis(RequestHandlerSelectors.basePackage("com.nivelle.guide.springboot.controllor"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("接口管理")
                .description("客户管理中心 API 1.0 操作文档")
                //服务条款网址
                .termsOfServiceUrl("https://www.jianshu.com/u/2836e364afc5")
                .version("1.0")
                .contact(new Contact("Nivelle", "https://www.jianshu.com/u/2836e364afc5", "572358423@qq.com"))
                .build();
    }
}

