### springBoot 启动

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/bin/java 

-XX:TieredStopAtLevel=1  //使用C1进行编译
-noverify -Dspring.output.ansi.enabled=always //彩色日志
-Dcom.sun.management.jmxremote // 使用 jmx 对java程序进行监控
-Dcom.sun.management.jmxremote.port=56107 
-Dcom.sun.management.jmxremote.authenticate=false 
-Dcom.sun.management.jmxremote.ssl=false 
-Djava.rmi.server.hostname=127.0.0.1 
-Dspring.liveBeansView.mbeanDomain 
-Dspring.application.admin.enabled=true //
"-javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=56108:/Applications/IntelliJ IDEA.app/Contents/bin" //id代理
-Dfile.encoding=UTF-8 //指定源文件使用的字符编码
-classpath 
/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/charsets.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/deploy.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/cldrdata.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/dnsns.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/jaccess.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/jfxrt.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/localedata.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/nashorn.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/sunec.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/zipfs.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/javaws.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/jce.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/jfr.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/jfxswt.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/jsse.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/management-agent.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/plugin.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/resources.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/rt.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/ant-javafx.jar
:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/dt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/javafx-mx.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/jconsole.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/packager.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/sa-jdi.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/tools.jar:/Users/nivellefu/IdeaProjects/programdayandnight/container-all/target/classes:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-starter-web/2.1.14.RELEASE/spring-boot-starter-web-2.1.14.RELEASE.jar:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-starter/2.1.14.RELEASE/spring-boot-starter-2.1.14.RELEASE.jar:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot/2.1.14.RELEASE/spring-boot-2.1.14.RELEASE.jar:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/2.1.14.RELEASE/spring-boot-autoconfigure-2.1.14.RELEASE.jar:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-starter-logging/2.1.14.RELEASE/spring-boot-starter-logging-2.1.14.RELEASE.jar:/Users/nivellefu/.m2/repository/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar:/Users/nivellefu/.m2/repository/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar:/Users/nivellefu/.m2/repository/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar:/Users/nivellefu/.m2/repository/org/apache/logging/log4j/log4j-to-slf4j/2.11.2/log4j-to-slf4j-2.11.2.jar:/Users/nivellefu/.m2/repository/org/apache/logging/log4j/log4j-api/2.11.2/log4j-api-2.11.2.jar:/Users/nivellefu/.m2/repository/org/slf4j/jul-to-slf4j/1.7.30/jul-to-slf4j-1.7.30.jar:/Users/nivellefu/.m2/repository/org/springframework/spring-core/5.1.15.RELEASE/spring-core-5.1.15.RELEASE.jar:/Users/nivellefu/.m2/repository/org/springframework/spring-jcl/5.1.15.RELEASE/spring-jcl-5.1.15.RELEASE.jar:/Users/nivellefu/.m2/repository/org/yaml/snakeyaml/1.23/snakeyaml-1.23.jar:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-starter-json/2.1.14.RELEASE/spring-boot-starter-json-2.1.14.RELEASE.jar:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.9.10.4/jackson-databind-2.9.10.4.jar:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.9.10/jackson-annotations-2.9.10.jar:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.9.10/jackson-core-2.9.10.jar:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jdk8/2.9.10/jackson-datatype-jdk8-2.9.10.jar:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.9.10/jackson-datatype-jsr310-2.9.10.jar:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/module/jackson-module-parameter-names/2.9.10/jackson-module-parameter-names-2.9.10.jar:/Users/nivellefu/.m2/repository/org/hibernate/validator/hibernate-validator/6.0.19.Final/hibernate-validator-6.0.19.Final.jar:/Users/nivellefu/.m2/repository/javax/validation/validation-api/2.0.1.Final/validation-api-2.0.1.Final.jar:/Users/nivellefu/.m2/repository/org/jboss/logging/jboss-logging/3.3.3.Final/jboss-logging-3.3.3.Final.jar:/Users/nivellefu/.m2/repository/com/fasterxml/classmate/1.4.0/classmate-1.4.0.jar:/Users/nivellefu/.m2/repository/org/springframework/spring-web/5.1.15.RELEASE/spring-web-5.1.15.RELEASE.jar:/Users/nivellefu/.m2/repository/org/springframework/spring-beans/5.1.15.RELEASE/spring-beans-5.1.15.RELEASE.jar:/Users/nivellefu/.m2/repository/org/springframework/spring-webmvc/5.1.15.RELEASE/spring-webmvc-5.1.15.RELEASE.jar:/Users/nivellefu/.m2/repository/org/springframework/spring-aop/5.1.15.RELEASE/spring-aop-5.1.15.RELEASE.jar:/Users/nivellefu/.m2/repository/org/springframework/spring-context/5.1.15.RELEASE/spring-context-5.1.15.RELEASE.jar:/Users/nivellefu/.m2/repository/org/springframework/spring-expression/5.1.15.RELEASE/spring-expression-5.1.15.RELEASE.jar:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-starter-tomcat/2.1.14.RELEASE/spring-boot-starter-tomcat-2.1.14.RELEASE.jar:/Users/nivellefu/.m2/repository/javax/annotation/javax.annotation-api/1.3.2/javax.annotation-api-1.3.2.jar:/Users/nivellefu/.m2/repository/org/apache/tomcat/embed/tomcat-embed-core/9.0.34/tomcat-embed-core-9.0.34.jar:/Users/nivellefu/.m2/repository/org/apache/tomcat/embed/tomcat-embed-el/9.0.34/tomcat-embed-el-9.0.34.jar:/Users/nivellefu/.m2/repository/org/apache/tomcat/embed/tomcat-embed-websocket/9.0.34/tomcat-embed-websocket-9.0.34.jar:/Users/nivellefu/.m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar com.nivelle.container.BootstrapApplication

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::       (v2.1.14.RELEASE)
```

第一步：BootstrapApplication开始启动(启动类,进程号):2020-07-08 18:05:56.965  INFO 40430 --- [           main] c.n.container.BootstrapApplication       : Starting BootstrapApplication on nivelleMac with PID 40430 (/Users/nivellefu/IdeaProjects/programdayandnight/container-all/target/classes started by nivellefu in /Users/nivellefu/IdeaProjects/programdayandnight/parent)
第二步: 配置文件激活:2020-07-08 18:05:56.968  INFO 40430 --- [           main] c.n.container.BootstrapApplication       : No active profile set, falling back to default profiles: default
第三步: tomcat初始化:2020-07-08 18:05:58.170  INFO 40430 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
第四步: tomcat启动开始:2020-07-08 18:05:58.187  INFO 40430 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
第五步: 启动容器 engin:2020-07-08 18:05:58.187  INFO 40430 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.34]
第六步: 初始化 WebApplicationContext: 2020-07-08 18:05:58.265  INFO 40430 --- [           main] o.a.c.c.C.[.[localhost].[/container]     : Initializing Spring embedded WebApplicationContext
第七步: WebApplicationContext 初始化完毕: 2020-07-08 18:05:58.266  INFO 40430 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 1263 ms
第七步: 初始化 applicationTaskExecutor: 2020-07-08 18:05:58.499  INFO 40430 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
第八步: 初始化 tomcat启动完毕: 2020-07-08 18:05:58.697  INFO 40430 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path '/container'
第九步: BootstrapApplication启动完毕: 2020-07-08 18:05:58.699  INFO 40430 --- [           main] c.n.container.BootstrapApplication       : Started BootstrapApplication in 2.034 seconds (JVM running for 2.61)

### springBoot 详细启动

```
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/Users/nivellefu/.m2/repository/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/Users/nivellefu/.m2/repository/org/slf4j/slf4j-log4j12/1.7.30/slf4j-log4j12-1.7.30.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [ch.qos.logback.classic.util.ContextSelectorStaticBinder]
20:35:17.201 [background-preinit] DEBUG o.h.v.m.ResourceBundleMessageInterpolator - Loaded expression factory via original TCCL
20:35:17.214 [background-preinit] DEBUG o.h.v.i.engine.ValidatorFactoryImpl - HV000234: Using org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator as ValidatorFactory-scoped message interpolator.
20:35:17.215 [background-preinit] DEBUG o.h.v.i.engine.ValidatorFactoryImpl - HV000234: Using org.hibernate.validator.internal.engine.resolver.TraverseAllTraversableResolver as ValidatorFactory-scoped traversable resolver.
20:35:17.215 [main] ERROR o.s.b.c.l.LoggingApplicationListener - Cannot set level 'com.nivelle.container : trace' for 'null'
20:35:17.215 [background-preinit] DEBUG o.h.v.i.engine.ValidatorFactoryImpl - HV000234: Using org.hibernate.validator.internal.util.ExecutableParameterNameProvider as ValidatorFactory-scoped parameter name provider.
20:35:17.215 [background-preinit] DEBUG o.h.v.i.engine.ValidatorFactoryImpl - HV000234: Using org.hibernate.validator.internal.engine.DefaultClockProvider as ValidatorFactory-scoped clock provider.
20:35:17.215 [background-preinit] DEBUG o.h.v.i.engine.ValidatorFactoryImpl - HV000234: Using org.hibernate.validator.internal.engine.scripting.DefaultScriptEvaluatorFactory as ValidatorFactory-scoped script evaluator factory.
20:35:17.216 [main] DEBUG o.s.b.c.l.ClasspathLoggingApplicationListener - Application started with classpath: [file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/charsets.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/deploy.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/cldrdata.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/dnsns.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/jaccess.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/jfxrt.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/localedata.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/nashorn.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/sunec.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/sunjce_provider.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/sunpkcs11.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/ext/zipfs.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/javaws.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/jce.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/jfr.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/jfxswt.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/jsse.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/management-agent.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/plugin.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/resources.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/rt.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/ant-javafx.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/dt.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/javafx-mx.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/jconsole.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/packager.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/sa-jdi.jar, file:/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/lib/tools.jar, file:/Users/nivellefu/IdeaProjects/programdayandnight/container-all/target/classes/, file:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-starter-web/2.1.14.RELEASE/spring-boot-starter-web-2.1.14.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-starter/2.1.14.RELEASE/spring-boot-starter-2.1.14.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot/2.1.14.RELEASE/spring-boot-2.1.14.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/2.1.14.RELEASE/spring-boot-autoconfigure-2.1.14.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-starter-logging/2.1.14.RELEASE/spring-boot-starter-logging-2.1.14.RELEASE.jar, file:/Users/nivellefu/.m2/repository/ch/qos/logback/logback-classic/1.2.3/logback-classic-1.2.3.jar, file:/Users/nivellefu/.m2/repository/ch/qos/logback/logback-core/1.2.3/logback-core-1.2.3.jar, file:/Users/nivellefu/.m2/repository/org/apache/logging/log4j/log4j-to-slf4j/2.11.2/log4j-to-slf4j-2.11.2.jar, file:/Users/nivellefu/.m2/repository/org/apache/logging/log4j/log4j-api/2.11.2/log4j-api-2.11.2.jar, file:/Users/nivellefu/.m2/repository/org/slf4j/jul-to-slf4j/1.7.30/jul-to-slf4j-1.7.30.jar, file:/Users/nivellefu/.m2/repository/org/springframework/spring-core/5.1.15.RELEASE/spring-core-5.1.15.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/springframework/spring-jcl/5.1.15.RELEASE/spring-jcl-5.1.15.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/yaml/snakeyaml/1.23/snakeyaml-1.23.jar, file:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-starter-json/2.1.14.RELEASE/spring-boot-starter-json-2.1.14.RELEASE.jar, file:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.9.10.4/jackson-databind-2.9.10.4.jar, file:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.9.10/jackson-annotations-2.9.10.jar, file:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.9.10/jackson-core-2.9.10.jar, file:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jdk8/2.9.10/jackson-datatype-jdk8-2.9.10.jar, file:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.9.10/jackson-datatype-jsr310-2.9.10.jar, file:/Users/nivellefu/.m2/repository/com/fasterxml/jackson/module/jackson-module-parameter-names/2.9.10/jackson-module-parameter-names-2.9.10.jar, file:/Users/nivellefu/.m2/repository/org/hibernate/validator/hibernate-validator/6.0.19.Final/hibernate-validator-6.0.19.Final.jar, file:/Users/nivellefu/.m2/repository/javax/validation/validation-api/2.0.1.Final/validation-api-2.0.1.Final.jar, file:/Users/nivellefu/.m2/repository/org/jboss/logging/jboss-logging/3.3.3.Final/jboss-logging-3.3.3.Final.jar, file:/Users/nivellefu/.m2/repository/com/fasterxml/classmate/1.4.0/classmate-1.4.0.jar, file:/Users/nivellefu/.m2/repository/org/springframework/spring-web/5.1.15.RELEASE/spring-web-5.1.15.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/springframework/spring-beans/5.1.15.RELEASE/spring-beans-5.1.15.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/springframework/spring-webmvc/5.1.15.RELEASE/spring-webmvc-5.1.15.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/springframework/spring-aop/5.1.15.RELEASE/spring-aop-5.1.15.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/springframework/spring-context/5.1.15.RELEASE/spring-context-5.1.15.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/springframework/spring-expression/5.1.15.RELEASE/spring-expression-5.1.15.RELEASE.jar, file:/Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot-starter-tomcat/2.1.14.RELEASE/spring-boot-starter-tomcat-2.1.14.RELEASE.jar, file:/Users/nivellefu/.m2/repository/javax/annotation/javax.annotation-api/1.3.2/javax.annotation-api-1.3.2.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/embed/tomcat-embed-core/9.0.34/tomcat-embed-core-9.0.34.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/embed/tomcat-embed-el/9.0.34/tomcat-embed-el-9.0.34.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/embed/tomcat-embed-websocket/9.0.34/tomcat-embed-websocket-9.0.34.jar, file:/Users/nivellefu/.m2/repository/com/google/code/gson/gson/2.8.6/gson-2.8.6.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-catalina/9.0.35/tomcat-catalina-9.0.35.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-servlet-api/9.0.35/tomcat-servlet-api-9.0.35.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-jsp-api/9.0.34/tomcat-jsp-api-9.0.34.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-el-api/9.0.34/tomcat-el-api-9.0.34.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-juli/9.0.35/tomcat-juli-9.0.35.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-annotations-api/9.0.34/tomcat-annotations-api-9.0.34.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-api/9.0.35/tomcat-api-9.0.35.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-jni/9.0.35/tomcat-jni-9.0.35.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-coyote/9.0.35/tomcat-coyote-9.0.35.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-util/9.0.35/tomcat-util-9.0.35.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-util-scan/9.0.35/tomcat-util-scan-9.0.35.jar, file:/Users/nivellefu/.m2/repository/org/apache/tomcat/tomcat-jaspic-api/9.0.35/tomcat-jaspic-api-9.0.35.jar, file:/Users/nivellefu/.m2/repository/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar, file:/Users/nivellefu/.m2/repository/org/slf4j/slf4j-log4j12/1.7.30/slf4j-log4j12-1.7.30.jar, file:/Users/nivellefu/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar, file:/Applications/IntelliJ%20IDEA.app/Contents/lib/idea_rt.jar]

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::       (v2.1.14.RELEASE)

20:35:17.359 [main] DEBUG o.s.boot.SpringApplication - Loading source class com.nivelle.container.ContainerBootstrapApplication
20:35:17.427 [main] DEBUG o.s.b.c.c.ConfigFileApplicationListener - Loaded config file 'file:/Users/nivellefu/IdeaProjects/programdayandnight/container-all/target/classes/config/application.properties' (classpath:/config/application.properties)
20:35:17.428 [main] DEBUG o.s.b.w.s.c.AnnotationConfigServletWebServerApplicationContext - Refreshing org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@6e0dec4a
20:35:17.479 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.context.annotation.internalConfigurationAnnotationProcessor'
20:35:17.589 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory'
20:35:17.789 [main] DEBUG o.s.c.a.ClassPathBeanDefinitionScanner - Identified candidate component class: file [/Users/nivellefu/IdeaProjects/programdayandnight/container-all/target/classes/com/nivelle/container/tomcat/TomcatConfig.class]
20:35:17.802 [main] DEBUG o.s.c.a.ClassPathBeanDefinitionScanner - Identified candidate component class: file [/Users/nivellefu/IdeaProjects/programdayandnight/container-all/target/classes/com/nivelle/container/tomcat/TomcatController.class]
20:35:18.073 [main] DEBUG o.s.c.e.PropertySourcesPropertyResolver - Found key 'spring.application.admin.enabled' in PropertySource 'configurationProperties' with value of type String
20:35:18.132 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.condition.BeanTypeRegistry'
20:35:18.254 [main] DEBUG o.s.c.e.PropertySourcesPropertyResolver - Found key 'spring.application.admin.enabled' in PropertySource 'configurationProperties' with value of type String
20:35:18.697 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'propertySourcesPlaceholderConfigurer'
20:35:18.713 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.context.event.internalEventListenerProcessor'
20:35:18.722 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata'
20:35:18.722 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'preserveErrorControllerTargetClassPostProcessor'
20:35:18.735 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.context.event.internalEventListenerFactory'
20:35:18.742 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.context.annotation.internalAutowiredAnnotationProcessor'
20:35:18.743 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.context.annotation.internalCommonAnnotationProcessor'
20:35:18.758 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor'
20:35:18.765 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'methodValidationPostProcessor'
20:35:18.788 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'methodValidationPostProcessor' via factory method to bean named 'environment'
20:35:18.798 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'webServerFactoryCustomizerBeanPostProcessor'
20:35:18.799 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'errorPageRegistrarBeanPostProcessor'
20:35:18.803 [main] DEBUG o.s.u.c.s.UiApplicationContextUtils - Unable to locate ThemeSource with name 'themeSource': using default [org.springframework.ui.context.support.ResourceBundleThemeSource@6b98a075]
20:35:18.806 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'webServerFactory'
20:35:18.806 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'tomcatConfig'
20:35:18.813 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'gracefulShutdown'
20:35:18.826 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'webServerFactory' via factory method to bean named 'gracefulShutdown'
20:35:18.888 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'websocketServletWebServerCustomizer'
20:35:18.888 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration$TomcatWebSocketConfiguration'
20:35:18.891 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'servletWebServerFactoryCustomizer'
20:35:18.891 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration'
20:35:18.894 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'server-org.springframework.boot.autoconfigure.web.ServerProperties'
20:35:18.920 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'servletWebServerFactoryCustomizer' via factory method to bean named 'server-org.springframework.boot.autoconfigure.web.ServerProperties'
20:35:18.924 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'tomcatServletWebServerFactoryCustomizer'
20:35:18.925 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'tomcatServletWebServerFactoryCustomizer' via factory method to bean named 'server-org.springframework.boot.autoconfigure.web.ServerProperties'
20:35:18.926 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'tomcatWebServerFactoryCustomizer'
20:35:18.929 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration$TomcatWebServerFactoryCustomizerConfiguration'
20:35:18.930 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'tomcatWebServerFactoryCustomizer' via factory method to bean named 'environment'
20:35:18.931 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'tomcatWebServerFactoryCustomizer' via factory method to bean named 'server-org.springframework.boot.autoconfigure.web.ServerProperties'
20:35:18.934 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'localeCharsetMappingsCustomizer'
20:35:18.934 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration'
20:35:18.935 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration$$EnhancerBySpringCGLIB$$a453d77d] - unable to determine constructor/method parameter names
20:35:18.936 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'spring.http-org.springframework.boot.autoconfigure.http.HttpProperties'
20:35:18.937 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration' via constructor to bean named 'spring.http-org.springframework.boot.autoconfigure.http.HttpProperties'
20:35:18.982 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'errorPageCustomizer'
20:35:18.982 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration'
20:35:18.982 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration$$EnhancerBySpringCGLIB$$fcab2660] - unable to determine constructor/method parameter names
20:35:18.983 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'dispatcherServletRegistration'
20:35:18.983 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration'
20:35:18.984 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration$$EnhancerBySpringCGLIB$$8306454b] - unable to determine constructor/method parameter names
20:35:18.984 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'spring.mvc-org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties'
20:35:18.986 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration' via constructor to bean named 'spring.mvc-org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties'
20:35:18.988 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'multipartConfigElement'
20:35:18.988 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration'
20:35:18.990 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration$$EnhancerBySpringCGLIB$$13445f8a] - unable to determine constructor/method parameter names
20:35:18.991 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'spring.servlet.multipart-org.springframework.boot.autoconfigure.web.servlet.MultipartProperties'
20:35:19.006 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration' via constructor to bean named 'spring.servlet.multipart-org.springframework.boot.autoconfigure.web.servlet.MultipartProperties'
20:35:19.014 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'dispatcherServlet'
20:35:19.014 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletConfiguration'
20:35:19.015 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletConfiguration$$EnhancerBySpringCGLIB$$20feb0a4] - unable to determine constructor/method parameter names
20:35:19.016 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletConfiguration' via constructor to bean named 'spring.http-org.springframework.boot.autoconfigure.http.HttpProperties'
20:35:19.016 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletConfiguration' via constructor to bean named 'spring.mvc-org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties'
20:35:19.030 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'dispatcherServletRegistration' via factory method to bean named 'dispatcherServlet'
20:35:19.035 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration' via constructor to bean named 'server-org.springframework.boot.autoconfigure.web.ServerProperties'
20:35:19.035 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration' via constructor to bean named 'dispatcherServletRegistration'
20:35:19.036 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'conventionErrorViewResolver'
20:35:19.036 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration$DefaultErrorViewResolverConfiguration'
20:35:19.036 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration$DefaultErrorViewResolverConfiguration$$EnhancerBySpringCGLIB$$113ffb00] - unable to determine constructor/method parameter names
20:35:19.038 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'spring.resources-org.springframework.boot.autoconfigure.web.ResourceProperties'
20:35:19.040 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration$DefaultErrorViewResolverConfiguration' via constructor to bean named 'org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@6e0dec4a'
20:35:19.041 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration$DefaultErrorViewResolverConfiguration' via constructor to bean named 'spring.resources-org.springframework.boot.autoconfigure.web.ResourceProperties'
20:35:19.146 [main] DEBUG o.s.b.w.e.t.TomcatServletWebServerFactory - Code archive: /Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot/2.1.14.RELEASE/spring-boot-2.1.14.RELEASE.jar
20:35:19.146 [main] DEBUG o.s.b.w.e.t.TomcatServletWebServerFactory - Code archive: /Users/nivellefu/.m2/repository/org/springframework/boot/spring-boot/2.1.14.RELEASE/spring-boot-2.1.14.RELEASE.jar
20:35:19.146 [main] DEBUG o.s.b.w.e.t.TomcatServletWebServerFactory - None of the document roots [src/main/webapp, public, static] point to a directory and will be ignored.
20:35:19.184 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat initialized with port(s): 8080 (http) 8090 (http)
20:35:19.233 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Initializing ProtocolHandler ["http-nio-8080"]
20:35:19.259 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Initializing ProtocolHandler ["http-nio-8090"]
20:35:19.308 [main] INFO  o.a.catalina.core.StandardService - Starting service [Tomcat]
20:35:19.308 [main] INFO  o.a.catalina.core.StandardEngine - Starting Servlet engine: [Apache Tomcat/9.0.34]
20:35:19.644 [main] INFO  o.a.c.c.C.[.[.[/container-connector] - Initializing Spring embedded WebApplicationContext
20:35:19.644 [main] DEBUG o.s.web.context.ContextLoader - Published root WebApplicationContext as ServletContext attribute with name [org.springframework.web.context.WebApplicationContext.ROOT]
20:35:19.644 [main] INFO  o.s.web.context.ContextLoader - Root WebApplicationContext: initialization completed in 2216 ms
20:35:19.652 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'requestContextFilter'
20:35:19.658 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'hiddenHttpMethodFilter'
20:35:19.659 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration'
20:35:19.669 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'formContentFilter'
20:35:19.672 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'characterEncodingFilter'
20:35:19.682 [main] DEBUG o.s.b.w.s.ServletContextInitializerBeans - Mapping filters: characterEncodingFilter urls=[/*], hiddenHttpMethodFilter urls=[/*], formContentFilter urls=[/*], requestContextFilter urls=[/*]
20:35:19.682 [main] DEBUG o.s.b.w.s.ServletContextInitializerBeans - Mapping servlets: dispatcherServlet urls=[/]
20:35:19.728 [main] DEBUG o.s.b.w.s.f.OrderedRequestContextFilter - Filter 'requestContextFilter' configured for use
20:35:19.729 [main] DEBUG o.s.b.w.s.f.OrderedHiddenHttpMethodFilter - Filter 'hiddenHttpMethodFilter' configured for use
20:35:19.729 [main] DEBUG o.s.b.w.s.f.OrderedCharacterEncodingFilter - Filter 'characterEncodingFilter' configured for use
20:35:19.729 [main] DEBUG o.s.b.w.s.f.OrderedFormContentFilter - Filter 'formContentFilter' configured for use
20:35:19.740 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'containerBootstrapApplication'
20:35:19.742 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'tomcatController'
20:35:19.744 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.AutoConfigurationPackages'
20:35:19.747 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration'
20:35:19.748 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration'
20:35:19.749 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration'
20:35:19.749 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration'
20:35:19.749 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration$$EnhancerBySpringCGLIB$$5c9eb9b1] - unable to determine constructor/method parameter names
20:35:19.750 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'spring.task.execution-org.springframework.boot.autoconfigure.task.TaskExecutionProperties'
20:35:19.752 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration' via constructor to bean named 'spring.task.execution-org.springframework.boot.autoconfigure.task.TaskExecutionProperties'
20:35:19.753 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'taskExecutorBuilder'
20:35:19.760 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration'
20:35:19.761 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'defaultValidator'
20:35:19.762 [main] DEBUG o.h.v.i.e.r.TraversableResolvers - Cannot find javax.persistence.Persistence on classpath. Assuming non JPA 2 environment. All properties will per default be traversable.
20:35:19.763 [main] DEBUG o.h.v.m.ResourceBundleMessageInterpolator - Loaded expression factory via original TCCL
20:35:19.766 [main] DEBUG o.h.v.i.e.r.TraversableResolvers - Cannot find javax.persistence.Persistence on classpath. Assuming non JPA 2 environment. All properties will per default be traversable.
20:35:19.767 [main] DEBUG o.h.v.i.engine.ConfigurationImpl - Setting custom MessageInterpolator of type org.springframework.validation.beanvalidation.LocaleContextMessageInterpolator
20:35:19.767 [main] DEBUG o.h.v.i.engine.ConfigurationImpl - Setting custom ConstraintValidatorFactory of type org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory
20:35:19.767 [main] DEBUG o.h.v.i.engine.ConfigurationImpl - Setting custom ParameterNameProvider of type org.springframework.validation.beanvalidation.LocalValidatorFactoryBean$1
20:35:19.768 [main] DEBUG o.h.v.i.x.config.ValidationXmlParser - Trying to load META-INF/validation.xml for XML based Validator configuration.
20:35:19.769 [main] DEBUG o.h.v.i.x.c.ResourceLoaderHelper - Trying to load META-INF/validation.xml via user class loader
20:35:19.769 [main] DEBUG o.h.v.i.x.c.ResourceLoaderHelper - Trying to load META-INF/validation.xml via TCCL
20:35:19.769 [main] DEBUG o.h.v.i.x.c.ResourceLoaderHelper - Trying to load META-INF/validation.xml via Hibernate Validator's class loader
20:35:19.769 [main] DEBUG o.h.v.i.x.config.ValidationXmlParser - No META-INF/validation.xml found. Using annotation based configuration only.
20:35:19.781 [main] DEBUG o.h.v.i.engine.ValidatorFactoryImpl - HV000234: Using org.springframework.validation.beanvalidation.LocaleContextMessageInterpolator as ValidatorFactory-scoped message interpolator.
20:35:19.781 [main] DEBUG o.h.v.i.engine.ValidatorFactoryImpl - HV000234: Using org.hibernate.validator.internal.engine.resolver.TraverseAllTraversableResolver as ValidatorFactory-scoped traversable resolver.
20:35:19.781 [main] DEBUG o.h.v.i.engine.ValidatorFactoryImpl - HV000234: Using org.hibernate.validator.internal.util.ExecutableParameterNameProvider as ValidatorFactory-scoped parameter name provider.
20:35:19.782 [main] DEBUG o.h.v.i.engine.ValidatorFactoryImpl - HV000234: Using org.hibernate.validator.internal.engine.DefaultClockProvider as ValidatorFactory-scoped clock provider.
20:35:19.782 [main] DEBUG o.h.v.i.engine.ValidatorFactoryImpl - HV000234: Using org.hibernate.validator.internal.engine.scripting.DefaultScriptEvaluatorFactory as ValidatorFactory-scoped script evaluator factory.
20:35:19.783 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration$WhitelabelErrorViewConfiguration'
20:35:19.808 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'error'
20:35:19.829 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'beanNameViewResolver'
20:35:19.834 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'errorAttributes'
20:35:19.835 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'basicErrorController'
20:35:19.836 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'basicErrorController' via factory method to bean named 'errorAttributes'
20:35:19.840 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter$FaviconConfiguration'
20:35:19.841 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter$FaviconConfiguration$$EnhancerBySpringCGLIB$$9a2d4486] - unable to determine constructor/method parameter names
20:35:19.842 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter$FaviconConfiguration' via constructor to bean named 'spring.resources-org.springframework.boot.autoconfigure.web.ResourceProperties'
20:35:19.843 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'faviconHandlerMapping'
20:35:19.858 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'faviconRequestHandler'
20:35:19.907 [main] DEBUG o.s.w.s.h.SimpleUrlHandlerMapping - Patterns [/**/favicon.ico] in 'faviconHandlerMapping'
20:35:19.907 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$EnableWebMvcConfiguration'
20:35:19.908 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$EnableWebMvcConfiguration$$EnhancerBySpringCGLIB$$3bec715b] - unable to determine constructor/method parameter names
20:35:19.915 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$EnableWebMvcConfiguration' via constructor to bean named 'spring.resources-org.springframework.boot.autoconfigure.web.ResourceProperties'
20:35:19.915 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$EnableWebMvcConfiguration' via constructor to bean named 'org.springframework.beans.factory.support.DefaultListableBeanFactory@5b94b04d'
20:35:19.938 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter'
20:35:19.941 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter$$EnhancerBySpringCGLIB$$ddf03c40] - unable to determine constructor/method parameter names
20:35:19.944 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter' via constructor to bean named 'spring.resources-org.springframework.boot.autoconfigure.web.ResourceProperties'
20:35:19.944 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter' via constructor to bean named 'spring.mvc-org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties'
20:35:19.944 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$WebMvcAutoConfigurationAdapter' via constructor to bean named 'org.springframework.beans.factory.support.DefaultListableBeanFactory@5b94b04d'
20:35:19.973 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'requestMappingHandlerAdapter'
20:35:20.053 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'mvcContentNegotiationManager'
20:35:20.058 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'messageConverters'
20:35:20.058 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration'
20:35:20.059 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration$$EnhancerBySpringCGLIB$$eb32ec6f] - unable to determine constructor/method parameter names
20:35:20.062 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'stringHttpMessageConverter'
20:35:20.062 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration$StringHttpMessageConverterConfiguration'
20:35:20.070 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration$StringHttpMessageConverterConfiguration$$EnhancerBySpringCGLIB$$1e40b30f] - unable to determine constructor/method parameter names
20:35:20.071 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration$StringHttpMessageConverterConfiguration' via constructor to bean named 'spring.http-org.springframework.boot.autoconfigure.http.HttpProperties'
20:35:20.111 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'mappingJackson2HttpMessageConverter'
20:35:20.114 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.http.JacksonHttpMessageConvertersConfiguration$MappingJackson2HttpMessageConverterConfiguration'
20:35:20.125 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'jacksonObjectMapper'
20:35:20.126 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$JacksonObjectMapperConfiguration'
20:35:20.127 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'jacksonObjectMapperBuilder'
20:35:20.127 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$JacksonObjectMapperBuilderConfiguration'
20:35:20.127 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$JacksonObjectMapperBuilderConfiguration$$EnhancerBySpringCGLIB$$dedf6285] - unable to determine constructor/method parameter names
20:35:20.128 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$JacksonObjectMapperBuilderConfiguration' via constructor to bean named 'org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@6e0dec4a'
20:35:20.129 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'standardJacksonObjectMapperBuilderCustomizer'
20:35:20.129 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$Jackson2ObjectMapperBuilderCustomizerConfiguration'
20:35:20.130 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties'
20:35:20.131 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'standardJacksonObjectMapperBuilderCustomizer' via factory method to bean named 'org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@6e0dec4a'
20:35:20.131 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'standardJacksonObjectMapperBuilderCustomizer' via factory method to bean named 'spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties'
20:35:20.136 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'jacksonObjectMapperBuilder' via factory method to bean named 'standardJacksonObjectMapperBuilderCustomizer'
20:35:20.146 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'parameterNamesModule'
20:35:20.146 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$ParameterNamesModuleConfiguration'
20:35:20.155 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'jsonComponentModule'
20:35:20.155 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration'
20:35:20.187 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'jacksonObjectMapper' via factory method to bean named 'jacksonObjectMapperBuilder'
20:35:20.291 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'mappingJackson2HttpMessageConverter' via factory method to bean named 'jacksonObjectMapper'
20:35:20.307 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'mvcConversionService'
20:35:20.328 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'mvcValidator'
20:35:20.337 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'applicationTaskExecutor'
20:35:20.339 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'applicationTaskExecutor' via factory method to bean named 'taskExecutorBuilder'
20:35:20.360 [main] INFO  o.s.s.c.ThreadPoolTaskExecutor - Initializing ExecutorService 'applicationTaskExecutor'
20:35:20.380 [main] DEBUG o.s.w.s.m.m.a.RequestMappingHandlerAdapter - ControllerAdvice beans: 0 @ModelAttribute, 0 @InitBinder, 1 RequestBodyAdvice, 1 ResponseBodyAdvice
20:35:20.432 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'requestMappingHandlerMapping'
20:35:20.456 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'mvcResourceUrlProvider'
20:35:20.499 [main] DEBUG o.s.w.s.m.m.a.RequestMappingHandlerMapping - 5 mappings in 'requestMappingHandlerMapping'
20:35:20.499 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'welcomePageHandlerMapping'
20:35:20.500 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'welcomePageHandlerMapping' via factory method to bean named 'org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@6e0dec4a'
20:35:20.511 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'mvcPathMatcher'
20:35:20.513 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'mvcUrlPathHelper'
20:35:20.513 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'viewControllerHandlerMapping'
20:35:20.514 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'beanNameHandlerMapping'
20:35:20.517 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'resourceHandlerMapping'
20:35:20.543 [main] DEBUG o.s.w.s.h.SimpleUrlHandlerMapping - Patterns [/webjars/**, /**] in 'resourceHandlerMapping'
20:35:20.544 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'defaultServletHandlerMapping'
20:35:20.552 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'mvcUriComponentsContributor'
20:35:20.557 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'httpRequestHandlerAdapter'
20:35:20.558 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'simpleControllerHandlerAdapter'
20:35:20.560 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'handlerExceptionResolver'
20:35:20.577 [main] DEBUG o.s.w.s.m.m.a.ExceptionHandlerExceptionResolver - ControllerAdvice beans: 0 @ExceptionHandler, 1 ResponseBodyAdvice
20:35:20.583 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'mvcViewResolver'
20:35:20.585 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'defaultViewResolver'
20:35:20.595 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'viewResolver'
20:35:20.596 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'viewResolver' via factory method to bean named 'org.springframework.beans.factory.support.DefaultListableBeanFactory@5b94b04d'
20:35:20.619 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration'
20:35:20.622 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'mbeanExporter'
20:35:20.623 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'objectNamingStrategy'
20:35:20.632 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'mbeanExporter' via factory method to bean named 'objectNamingStrategy'
20:35:20.654 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'mbeanServer'
20:35:20.662 [main] DEBUG o.s.jmx.support.JmxUtils - Found MBeanServer: com.sun.jmx.mbeanserver.JmxMBeanServer@4fca772d
20:35:20.672 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration'
20:35:20.673 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration$$EnhancerBySpringCGLIB$$63abc1a9] - unable to determine constructor/method parameter names
20:35:20.674 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration' via constructor to bean named 'environment'
20:35:20.675 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'springApplicationAdminRegistrar'
20:35:20.686 [main] DEBUG o.s.b.a.SpringApplicationAdminMXBeanRegistrar$SpringApplicationAdmin - Application Admin MBean registered with name 'org.springframework.boot:type=Admin,name=SpringApplication'
20:35:20.686 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration'
20:35:20.687 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration'
20:35:20.688 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'gsonBuilder'
20:35:20.689 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'standardGsonBuilderCustomizer'
20:35:20.691 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'spring.gson-org.springframework.boot.autoconfigure.gson.GsonProperties'
20:35:20.692 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'standardGsonBuilderCustomizer' via factory method to bean named 'spring.gson-org.springframework.boot.autoconfigure.gson.GsonProperties'
20:35:20.696 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'gsonBuilder' via factory method to bean named 'standardGsonBuilderCustomizer'
20:35:20.710 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'gson'
20:35:20.710 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'gson' via factory method to bean named 'gsonBuilder'
20:35:20.758 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.http.JacksonHttpMessageConvertersConfiguration'
20:35:20.759 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.http.GsonHttpMessageConvertersConfiguration'
20:35:20.759 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration$LoggingCodecConfiguration'
20:35:20.760 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'loggingCodecCustomizer'
20:35:20.760 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'loggingCodecCustomizer' via factory method to bean named 'spring.http-org.springframework.boot.autoconfigure.http.HttpProperties'
20:35:20.764 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration$JacksonCodecConfiguration'
20:35:20.765 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'jacksonCodecCustomizer'
20:35:20.765 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'jacksonCodecCustomizer' via factory method to bean named 'jacksonObjectMapper'
20:35:20.770 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration'
20:35:20.772 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration'
20:35:20.773 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration$$EnhancerBySpringCGLIB$$cf92df4] - unable to determine constructor/method parameter names
20:35:20.774 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'spring.info-org.springframework.boot.autoconfigure.info.ProjectInfoProperties'
20:35:20.775 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration' via constructor to bean named 'spring.info-org.springframework.boot.autoconfigure.info.ProjectInfoProperties'
20:35:20.775 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration'
20:35:20.776 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'taskSchedulerBuilder'
20:35:20.777 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'spring.task.scheduling-org.springframework.boot.autoconfigure.task.TaskSchedulingProperties'
20:35:20.778 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Autowiring by type from bean name 'taskSchedulerBuilder' via factory method to bean named 'spring.task.scheduling-org.springframework.boot.autoconfigure.task.TaskSchedulingProperties'
20:35:20.781 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration'
20:35:20.782 [main] DEBUG o.s.c.LocalVariableTableParameterNameDiscoverer - Cannot find '.class' file for class [class org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration$$EnhancerBySpringCGLIB$$6c151a5c] - unable to determine constructor/method parameter names
20:35:20.783 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'restTemplateBuilder'
20:35:20.789 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration'
20:35:20.790 [main] DEBUG o.s.b.f.s.DefaultListableBeanFactory - Creating shared instance of singleton bean 'multipartResolver'
20:35:20.837 [main] DEBUG o.s.j.e.a.AnnotationMBeanExporter - Registering beans for JMX exposure on startup
20:35:20.838 [main] DEBUG o.s.j.e.a.AnnotationMBeanExporter - Autodetecting user-defined JMX MBeans
20:35:20.947 [main] DEBUG o.s.b.a.l.ConditionEvaluationReportLoggingListener - 


============================
CONDITIONS EVALUATION REPORT
============================


Positive matches:
-----------------

   CodecsAutoConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.http.codec.CodecConfigurer' (OnClassCondition)

   CodecsAutoConfiguration.JacksonCodecConfiguration matched:
      - @ConditionalOnClass found required class 'com.fasterxml.jackson.databind.ObjectMapper' (OnClassCondition)

   CodecsAutoConfiguration.JacksonCodecConfiguration#jacksonCodecCustomizer matched:
      - @ConditionalOnBean (types: com.fasterxml.jackson.databind.ObjectMapper; SearchStrategy: all) found bean 'jacksonObjectMapper' (OnBeanCondition)

   DispatcherServletAutoConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.web.servlet.DispatcherServlet' (OnClassCondition)
      - found 'session' scope (OnWebApplicationCondition)

   DispatcherServletAutoConfiguration.DispatcherServletConfiguration matched:
      - @ConditionalOnClass found required class 'javax.servlet.ServletRegistration' (OnClassCondition)
      - Default DispatcherServlet did not find dispatcher servlet beans (DispatcherServletAutoConfiguration.DefaultDispatcherServletCondition)

   DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration matched:
      - @ConditionalOnClass found required class 'javax.servlet.ServletRegistration' (OnClassCondition)
      - DispatcherServlet Registration did not find servlet registration bean (DispatcherServletAutoConfiguration.DispatcherServletRegistrationCondition)

   DispatcherServletAutoConfiguration.DispatcherServletRegistrationConfiguration#dispatcherServletRegistration matched:
      - @ConditionalOnBean (names: dispatcherServlet; types: org.springframework.web.servlet.DispatcherServlet; SearchStrategy: all) found bean 'dispatcherServlet' (OnBeanCondition)

   EmbeddedWebServerFactoryCustomizerAutoConfiguration matched:
      - @ConditionalOnWebApplication (required) found 'session' scope (OnWebApplicationCondition)

   EmbeddedWebServerFactoryCustomizerAutoConfiguration.TomcatWebServerFactoryCustomizerConfiguration matched:
      - @ConditionalOnClass found required classes 'org.apache.catalina.startup.Tomcat', 'org.apache.coyote.UpgradeProtocol' (OnClassCondition)

   ErrorMvcAutoConfiguration matched:
      - @ConditionalOnClass found required classes 'javax.servlet.Servlet', 'org.springframework.web.servlet.DispatcherServlet' (OnClassCondition)
      - found 'session' scope (OnWebApplicationCondition)

   ErrorMvcAutoConfiguration#basicErrorController matched:
      - @ConditionalOnMissingBean (types: org.springframework.boot.web.servlet.error.ErrorController; SearchStrategy: current) did not find any beans (OnBeanCondition)

   ErrorMvcAutoConfiguration#errorAttributes matched:
      - @ConditionalOnMissingBean (types: org.springframework.boot.web.servlet.error.ErrorAttributes; SearchStrategy: current) did not find any beans (OnBeanCondition)

   ErrorMvcAutoConfiguration.DefaultErrorViewResolverConfiguration#conventionErrorViewResolver matched:
      - @ConditionalOnBean (types: org.springframework.web.servlet.DispatcherServlet; SearchStrategy: all) found bean 'dispatcherServlet'; @ConditionalOnMissingBean (types: org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver; SearchStrategy: all) did not find any beans (OnBeanCondition)

   ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration matched:
      - @ConditionalOnProperty (server.error.whitelabel.enabled) matched (OnPropertyCondition)
      - ErrorTemplate Missing did not find error template view (ErrorMvcAutoConfiguration.ErrorTemplateMissingCondition)

   ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration#beanNameViewResolver matched:
      - @ConditionalOnMissingBean (types: org.springframework.web.servlet.view.BeanNameViewResolver; SearchStrategy: all) did not find any beans (OnBeanCondition)

   ErrorMvcAutoConfiguration.WhitelabelErrorViewConfiguration#defaultErrorView matched:
      - @ConditionalOnMissingBean (names: error; SearchStrategy: all) did not find any beans (OnBeanCondition)

   GenericCacheConfiguration matched:
      - Cache org.springframework.boot.autoconfigure.cache.GenericCacheConfiguration automatic cache type (CacheCondition)

   GsonAutoConfiguration matched:
      - @ConditionalOnClass found required class 'com.google.gson.Gson' (OnClassCondition)

   GsonAutoConfiguration#gson matched:
      - @ConditionalOnMissingBean (types: com.google.gson.Gson; SearchStrategy: all) did not find any beans (OnBeanCondition)

   GsonAutoConfiguration#gsonBuilder matched:
      - @ConditionalOnMissingBean (types: com.google.gson.GsonBuilder; SearchStrategy: all) did not find any beans (OnBeanCondition)

   GsonHttpMessageConvertersConfiguration matched:
      - @ConditionalOnClass found required class 'com.google.gson.Gson' (OnClassCondition)

   HttpEncodingAutoConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.web.filter.CharacterEncodingFilter' (OnClassCondition)
      - found 'session' scope (OnWebApplicationCondition)
      - @ConditionalOnProperty (spring.http.encoding.enabled) matched (OnPropertyCondition)

   HttpEncodingAutoConfiguration#characterEncodingFilter matched:
      - @ConditionalOnMissingBean (types: org.springframework.web.filter.CharacterEncodingFilter; SearchStrategy: all) did not find any beans (OnBeanCondition)

   HttpMessageConvertersAutoConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.http.converter.HttpMessageConverter' (OnClassCondition)

   HttpMessageConvertersAutoConfiguration#messageConverters matched:
      - @ConditionalOnMissingBean (types: org.springframework.boot.autoconfigure.http.HttpMessageConverters; SearchStrategy: all) did not find any beans (OnBeanCondition)

   HttpMessageConvertersAutoConfiguration.StringHttpMessageConverterConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.http.converter.StringHttpMessageConverter' (OnClassCondition)

   HttpMessageConvertersAutoConfiguration.StringHttpMessageConverterConfiguration#stringHttpMessageConverter matched:
      - @ConditionalOnMissingBean (types: org.springframework.http.converter.StringHttpMessageConverter; SearchStrategy: all) did not find any beans (OnBeanCondition)

   JacksonAutoConfiguration matched:
      - @ConditionalOnClass found required class 'com.fasterxml.jackson.databind.ObjectMapper' (OnClassCondition)

   JacksonAutoConfiguration.Jackson2ObjectMapperBuilderCustomizerConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.http.converter.json.Jackson2ObjectMapperBuilder' (OnClassCondition)

   JacksonAutoConfiguration.JacksonObjectMapperBuilderConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.http.converter.json.Jackson2ObjectMapperBuilder' (OnClassCondition)

   JacksonAutoConfiguration.JacksonObjectMapperBuilderConfiguration#jacksonObjectMapperBuilder matched:
      - @ConditionalOnMissingBean (types: org.springframework.http.converter.json.Jackson2ObjectMapperBuilder; SearchStrategy: all) did not find any beans (OnBeanCondition)

   JacksonAutoConfiguration.JacksonObjectMapperConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.http.converter.json.Jackson2ObjectMapperBuilder' (OnClassCondition)

   JacksonAutoConfiguration.JacksonObjectMapperConfiguration#jacksonObjectMapper matched:
      - @ConditionalOnMissingBean (types: com.fasterxml.jackson.databind.ObjectMapper; SearchStrategy: all) did not find any beans (OnBeanCondition)

   JacksonAutoConfiguration.ParameterNamesModuleConfiguration matched:
      - @ConditionalOnClass found required class 'com.fasterxml.jackson.module.paramnames.ParameterNamesModule' (OnClassCondition)

   JacksonAutoConfiguration.ParameterNamesModuleConfiguration#parameterNamesModule matched:
      - @ConditionalOnMissingBean (types: com.fasterxml.jackson.module.paramnames.ParameterNamesModule; SearchStrategy: all) did not find any beans (OnBeanCondition)

   JacksonHttpMessageConvertersConfiguration.MappingJackson2HttpMessageConverterConfiguration matched:
      - @ConditionalOnClass found required class 'com.fasterxml.jackson.databind.ObjectMapper' (OnClassCondition)
      - @ConditionalOnProperty (spring.http.converters.preferred-json-mapper=jackson) matched (OnPropertyCondition)
      - @ConditionalOnBean (types: com.fasterxml.jackson.databind.ObjectMapper; SearchStrategy: all) found bean 'jacksonObjectMapper' (OnBeanCondition)

   JacksonHttpMessageConvertersConfiguration.MappingJackson2HttpMessageConverterConfiguration#mappingJackson2HttpMessageConverter matched:
      - @ConditionalOnMissingBean (types: org.springframework.http.converter.json.MappingJackson2HttpMessageConverter; SearchStrategy: all) did not find any beans (OnBeanCondition)

   JmxAutoConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.jmx.export.MBeanExporter' (OnClassCondition)
      - @ConditionalOnProperty (spring.jmx.enabled=true) matched (OnPropertyCondition)

   JmxAutoConfiguration#mbeanExporter matched:
      - @ConditionalOnMissingBean (types: org.springframework.jmx.export.MBeanExporter; SearchStrategy: current) did not find any beans (OnBeanCondition)

   JmxAutoConfiguration#mbeanServer matched:
      - @ConditionalOnMissingBean (types: javax.management.MBeanServer; SearchStrategy: all) did not find any beans (OnBeanCondition)

   JmxAutoConfiguration#objectNamingStrategy matched:
      - @ConditionalOnMissingBean (types: org.springframework.jmx.export.naming.ObjectNamingStrategy; SearchStrategy: current) did not find any beans (OnBeanCondition)

   MultipartAutoConfiguration matched:
      - @ConditionalOnClass found required classes 'javax.servlet.Servlet', 'org.springframework.web.multipart.support.StandardServletMultipartResolver', 'javax.servlet.MultipartConfigElement' (OnClassCondition)
      - found 'session' scope (OnWebApplicationCondition)
      - @ConditionalOnProperty (spring.servlet.multipart.enabled) matched (OnPropertyCondition)

   MultipartAutoConfiguration#multipartConfigElement matched:
      - @ConditionalOnMissingBean (types: javax.servlet.MultipartConfigElement,org.springframework.web.multipart.commons.CommonsMultipartResolver; SearchStrategy: all) did not find any beans (OnBeanCondition)

   MultipartAutoConfiguration#multipartResolver matched:
      - @ConditionalOnMissingBean (types: org.springframework.web.multipart.MultipartResolver; SearchStrategy: all) did not find any beans (OnBeanCondition)

   NoOpCacheConfiguration matched:
      - Cache org.springframework.boot.autoconfigure.cache.NoOpCacheConfiguration automatic cache type (CacheCondition)

   PropertyPlaceholderAutoConfiguration#propertySourcesPlaceholderConfigurer matched:
      - @ConditionalOnMissingBean (types: org.springframework.context.support.PropertySourcesPlaceholderConfigurer; SearchStrategy: current) did not find any beans (OnBeanCondition)

   RestTemplateAutoConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.web.client.RestTemplate' (OnClassCondition)

   RestTemplateAutoConfiguration#restTemplateBuilder matched:
      - @ConditionalOnMissingBean (types: org.springframework.boot.web.client.RestTemplateBuilder; SearchStrategy: all) did not find any beans (OnBeanCondition)

   ServletWebServerFactoryAutoConfiguration matched:
      - @ConditionalOnClass found required class 'javax.servlet.ServletRequest' (OnClassCondition)
      - found 'session' scope (OnWebApplicationCondition)

   ServletWebServerFactoryAutoConfiguration#tomcatServletWebServerFactoryCustomizer matched:
      - @ConditionalOnClass found required class 'org.apache.catalina.startup.Tomcat' (OnClassCondition)

   SimpleCacheConfiguration matched:
      - Cache org.springframework.boot.autoconfigure.cache.SimpleCacheConfiguration automatic cache type (CacheCondition)

   SpringApplicationAdminJmxAutoConfiguration matched:
      - @ConditionalOnProperty (spring.application.admin.enabled=true) matched (OnPropertyCondition)

   SpringApplicationAdminJmxAutoConfiguration#springApplicationAdminRegistrar matched:
      - @ConditionalOnMissingBean (types: org.springframework.boot.admin.SpringApplicationAdminMXBeanRegistrar; SearchStrategy: all) did not find any beans (OnBeanCondition)

   TaskExecutionAutoConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor' (OnClassCondition)

   TaskExecutionAutoConfiguration#applicationTaskExecutor matched:
      - @ConditionalOnMissingBean (types: java.util.concurrent.Executor; SearchStrategy: all) did not find any beans (OnBeanCondition)

   TaskExecutionAutoConfiguration#taskExecutorBuilder matched:
      - @ConditionalOnMissingBean (types: org.springframework.boot.task.TaskExecutorBuilder; SearchStrategy: all) did not find any beans (OnBeanCondition)

   TaskSchedulingAutoConfiguration matched:
      - @ConditionalOnClass found required class 'org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler' (OnClassCondition)

   TaskSchedulingAutoConfiguration#taskSchedulerBuilder matched:
      - @ConditionalOnMissingBean (types: org.springframework.boot.task.TaskSchedulerBuilder; SearchStrategy: all) did not find any beans (OnBeanCondition)

   ValidationAutoConfiguration matched:
      - @ConditionalOnClass found required class 'javax.validation.executable.ExecutableValidator' (OnClassCondition)
      - @ConditionalOnResource found location classpath:META-INF/services/javax.validation.spi.ValidationProvider (OnResourceCondition)

   ValidationAutoConfiguration#defaultValidator matched:
      - @ConditionalOnMissingBean (types: javax.validation.Validator; SearchStrategy: all) did not find any beans (OnBeanCondition)

   ValidationAutoConfiguration#methodValidationPostProcessor matched:
      - @ConditionalOnMissingBean (types: org.springframework.validation.beanvalidation.MethodValidationPostProcessor; SearchStrategy: all) did not find any beans (OnBeanCondition)

   WebMvcAutoConfiguration matched:
      - @ConditionalOnClass found required classes 'javax.servlet.Servlet', 'org.springframework.web.servlet.DispatcherServlet', 'org.springframework.web.servlet.config.annotation.WebMvcConfigurer' (OnClassCondition)
      - found 'session' scope (OnWebApplicationCondition)
      - @ConditionalOnMissingBean (types: org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport; SearchStrategy: all) did not find any beans (OnBeanCondition)

   WebMvcAutoConfiguration#formContentFilter matched:
      - @ConditionalOnProperty (spring.mvc.formcontent.filter.enabled) matched (OnPropertyCondition)
      - @ConditionalOnMissingBean (types: org.springframework.web.filter.FormContentFilter; SearchStrategy: all) did not find any beans (OnBeanCondition)

   WebMvcAutoConfiguration#hiddenHttpMethodFilter matched:
      - @ConditionalOnProperty (spring.mvc.hiddenmethod.filter.enabled) matched (OnPropertyCondition)
      - @ConditionalOnMissingBean (types: org.springframework.web.filter.HiddenHttpMethodFilter; SearchStrategy: all) did not find any beans (OnBeanCondition)

   WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#defaultViewResolver matched:
      - @ConditionalOnMissingBean (types: org.springframework.web.servlet.view.InternalResourceViewResolver; SearchStrategy: all) did not find any beans (OnBeanCondition)

   WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#requestContextFilter matched:
      - @ConditionalOnMissingBean (types: org.springframework.web.context.request.RequestContextListener,org.springframework.web.filter.RequestContextFilter; SearchStrategy: all) did not find any beans (OnBeanCondition)

   WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#viewResolver matched:
      - @ConditionalOnBean (types: org.springframework.web.servlet.ViewResolver; SearchStrategy: all) found beans 'defaultViewResolver', 'beanNameViewResolver', 'mvcViewResolver'; @ConditionalOnMissingBean (names: viewResolver; types: org.springframework.web.servlet.view.ContentNegotiatingViewResolver; SearchStrategy: all) did not find any beans (OnBeanCondition)

   WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter.FaviconConfiguration matched:
      - @ConditionalOnProperty (spring.mvc.favicon.enabled) matched (OnPropertyCondition)

   WebSocketServletAutoConfiguration matched:
      - @ConditionalOnClass found required classes 'javax.servlet.Servlet', 'javax.websocket.server.ServerContainer' (OnClassCondition)
      - found 'session' scope (OnWebApplicationCondition)

   WebSocketServletAutoConfiguration.TomcatWebSocketConfiguration matched:
      - @ConditionalOnClass found required classes 'org.apache.catalina.startup.Tomcat', 'org.apache.tomcat.websocket.server.WsSci' (OnClassCondition)

   WebSocketServletAutoConfiguration.TomcatWebSocketConfiguration#websocketServletWebServerCustomizer matched:
      - @ConditionalOnMissingBean (names: websocketServletWebServerCustomizer; SearchStrategy: all) did not find any beans (OnBeanCondition)


Negative matches:
-----------------

   ActiveMQAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'javax.jms.ConnectionFactory' (OnClassCondition)

   AopAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.aspectj.lang.annotation.Aspect' (OnClassCondition)

   ArtemisAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'javax.jms.ConnectionFactory' (OnClassCondition)

   BatchAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.batch.core.launch.JobLauncher' (OnClassCondition)

   CacheAutoConfiguration:
      Did not match:
         - @ConditionalOnBean (types: org.springframework.cache.interceptor.CacheAspectSupport; SearchStrategy: all) did not find any beans of type org.springframework.cache.interceptor.CacheAspectSupport (OnBeanCondition)
      Matched:
         - @ConditionalOnClass found required class 'org.springframework.cache.CacheManager' (OnClassCondition)

   CacheAutoConfiguration.CacheManagerJpaDependencyConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean' (OnClassCondition)
         - Ancestor org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration did not match (ConditionEvaluationReport.AncestorsMatchedCondition)

   CaffeineCacheConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required classes 'com.github.benmanes.caffeine.cache.Caffeine', 'org.springframework.cache.caffeine.CaffeineCacheManager' (OnClassCondition)

   CassandraAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.datastax.driver.core.Cluster' (OnClassCondition)

   CassandraDataAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.datastax.driver.core.Cluster' (OnClassCondition)

   CassandraReactiveDataAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.datastax.driver.core.Cluster' (OnClassCondition)

   CassandraReactiveRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.data.cassandra.ReactiveSession' (OnClassCondition)

   CassandraRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.datastax.driver.core.Session' (OnClassCondition)

   ClientHttpConnectorAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.web.reactive.function.client.WebClient' (OnClassCondition)

   CloudServiceConnectorsAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.cloud.config.java.CloudScanConfiguration' (OnClassCondition)

   CouchbaseAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.couchbase.client.java.Cluster' (OnClassCondition)

   CouchbaseCacheConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required classes 'com.couchbase.client.java.Bucket', 'com.couchbase.client.spring.cache.CouchbaseCacheManager' (OnClassCondition)

   CouchbaseDataAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.couchbase.client.java.Bucket' (OnClassCondition)

   CouchbaseReactiveDataAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.couchbase.client.java.Bucket' (OnClassCondition)

   CouchbaseReactiveRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.couchbase.client.java.Bucket' (OnClassCondition)

   CouchbaseRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.couchbase.client.java.Bucket' (OnClassCondition)

   DataSourceAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType' (OnClassCondition)

   DataSourceTransactionManagerAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.jdbc.core.JdbcTemplate' (OnClassCondition)

   DispatcherServletAutoConfiguration.DispatcherServletConfiguration#multipartResolver:
      Did not match:
         - @ConditionalOnBean (types: org.springframework.web.multipart.MultipartResolver; SearchStrategy: all) did not find any beans of type org.springframework.web.multipart.MultipartResolver (OnBeanCondition)

   EhCacheCacheConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required classes 'net.sf.ehcache.Cache', 'org.springframework.cache.ehcache.EhCacheCacheManager' (OnClassCondition)

   ElasticsearchAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.elasticsearch.client.Client' (OnClassCondition)

   ElasticsearchDataAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.elasticsearch.client.Client' (OnClassCondition)

   ElasticsearchRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.elasticsearch.client.Client' (OnClassCondition)

   EmbeddedLdapAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.unboundid.ldap.listener.InMemoryDirectoryServer' (OnClassCondition)

   EmbeddedMongoAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.mongodb.MongoClient' (OnClassCondition)

   EmbeddedWebServerFactoryCustomizerAutoConfiguration.JettyWebServerFactoryCustomizerConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required classes 'org.eclipse.jetty.server.Server', 'org.eclipse.jetty.util.Loader', 'org.eclipse.jetty.webapp.WebAppContext' (OnClassCondition)

   EmbeddedWebServerFactoryCustomizerAutoConfiguration.NettyWebServerFactoryCustomizerConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'reactor.netty.http.server.HttpServer' (OnClassCondition)

   EmbeddedWebServerFactoryCustomizerAutoConfiguration.UndertowWebServerFactoryCustomizerConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required classes 'io.undertow.Undertow', 'org.xnio.SslClientAuthMode' (OnClassCondition)

   ErrorWebFluxAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.web.reactive.config.WebFluxConfigurer' (OnClassCondition)

   FlywayAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.flywaydb.core.Flyway' (OnClassCondition)

   FreeMarkerAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'freemarker.template.Configuration' (OnClassCondition)

   GroovyTemplateAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'groovy.text.markup.MarkupTemplateEngine' (OnClassCondition)

   GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration:
      Did not match:
         - AnyNestedCondition 0 matched 2 did not; NestedCondition on GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition.JacksonJsonbUnavailable NoneNestedConditions 1 matched 1 did not; NestedCondition on GsonHttpMessageConvertersConfiguration.JacksonAndJsonbUnavailableCondition.JsonbPreferred @ConditionalOnProperty (spring.http.converters.preferred-json-mapper=jsonb) did not find property 'spring.http.converters.preferred-json-mapper'; NestedCondition on GsonHttpMessageConvertersConfiguration.JacksonAndJsonbUnavailableCondition.JacksonAvailable @ConditionalOnBean (types: org.springframework.http.converter.json.MappingJackson2HttpMessageConverter; SearchStrategy: all) found bean 'mappingJackson2HttpMessageConverter'; NestedCondition on GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition.GsonPreferred @ConditionalOnProperty (spring.http.converters.preferred-json-mapper=gson) did not find property 'spring.http.converters.preferred-json-mapper' (GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition)

   H2ConsoleAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.h2.server.web.WebServlet' (OnClassCondition)

   HazelcastAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.hazelcast.core.HazelcastInstance' (OnClassCondition)

   HazelcastCacheConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required classes 'com.hazelcast.core.HazelcastInstance', 'com.hazelcast.spring.cache.HazelcastCacheManager' (OnClassCondition)

   HazelcastJpaDependencyAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.hazelcast.core.HazelcastInstance' (OnClassCondition)

   HibernateJpaAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'javax.persistence.EntityManager' (OnClassCondition)

   HttpHandlerAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.web.reactive.DispatcherHandler' (OnClassCondition)

   HypermediaAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.hateoas.Resource' (OnClassCondition)

   InfinispanCacheConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.infinispan.spring.provider.SpringEmbeddedCacheManager' (OnClassCondition)

   InfluxDbAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.influxdb.InfluxDB' (OnClassCondition)

   IntegrationAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.integration.config.EnableIntegration' (OnClassCondition)

   JCacheCacheConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required classes 'javax.cache.Caching', 'org.springframework.cache.jcache.JCacheCacheManager' (OnClassCondition)

   JacksonAutoConfiguration.JodaDateTimeJacksonConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required classes 'org.joda.time.DateTime', 'com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer', 'com.fasterxml.jackson.datatype.joda.cfg.JacksonJodaDateFormat' (OnClassCondition)

   JacksonHttpMessageConvertersConfiguration.MappingJackson2XmlHttpMessageConverterConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.fasterxml.jackson.dataformat.xml.XmlMapper' (OnClassCondition)

   JdbcRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.data.jdbc.repository.config.JdbcConfiguration' (OnClassCondition)

   JdbcTemplateAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.jdbc.core.JdbcTemplate' (OnClassCondition)

   JerseyAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.glassfish.jersey.server.spring.SpringComponentProvider' (OnClassCondition)

   JestAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'io.searchbox.client.JestClient' (OnClassCondition)

   JmsAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'javax.jms.Message' (OnClassCondition)

   JndiConnectionFactoryAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.jms.core.JmsTemplate' (OnClassCondition)

   JndiDataSourceAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType' (OnClassCondition)

   JooqAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.jooq.DSLContext' (OnClassCondition)

   JpaRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.data.jpa.repository.JpaRepository' (OnClassCondition)

   JsonbAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'javax.json.bind.Jsonb' (OnClassCondition)

   JsonbHttpMessageConvertersConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'javax.json.bind.Jsonb' (OnClassCondition)

   JtaAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'javax.transaction.Transaction' (OnClassCondition)

   KafkaAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.kafka.core.KafkaTemplate' (OnClassCondition)

   LdapAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.ldap.core.ContextSource' (OnClassCondition)

   LdapRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.data.ldap.repository.LdapRepository' (OnClassCondition)

   LiquibaseAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'liquibase.change.DatabaseChange' (OnClassCondition)

   MailSenderAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'javax.mail.internet.MimeMessage' (OnClassCondition)

   MailSenderValidatorAutoConfiguration:
      Did not match:
         - @ConditionalOnSingleCandidate did not find required type 'org.springframework.mail.javamail.JavaMailSenderImpl' (OnBeanCondition)

   MessageSourceAutoConfiguration:
      Did not match:
         - ResourceBundle did not find bundle with basename messages (MessageSourceAutoConfiguration.ResourceBundleCondition)

   MongoAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.mongodb.MongoClient' (OnClassCondition)

   MongoDataAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.mongodb.client.MongoClient' (OnClassCondition)

   MongoReactiveAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.mongodb.reactivestreams.client.MongoClient' (OnClassCondition)

   MongoReactiveDataAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.mongodb.reactivestreams.client.MongoClient' (OnClassCondition)

   MongoReactiveRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.mongodb.reactivestreams.client.MongoClient' (OnClassCondition)

   MongoRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.mongodb.MongoClient' (OnClassCondition)

   MustacheAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.samskivert.mustache.Mustache' (OnClassCondition)

   Neo4jDataAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.neo4j.ogm.session.SessionFactory' (OnClassCondition)

   Neo4jRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.neo4j.ogm.session.Neo4jSession' (OnClassCondition)

   OAuth2ClientAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.security.config.annotation.web.configuration.EnableWebSecurity' (OnClassCondition)

   OAuth2ResourceServerAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.security.oauth2.jwt.JwtDecoder' (OnClassCondition)

   PersistenceExceptionTranslationAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor' (OnClassCondition)

   ProjectInfoAutoConfiguration#buildProperties:
      Did not match:
         - @ConditionalOnResource did not find resource '${spring.info.build.location:classpath:META-INF/build-info.properties}' (OnResourceCondition)

   ProjectInfoAutoConfiguration#gitProperties:
      Did not match:
         - GitResource did not find git info at classpath:git.properties (ProjectInfoAutoConfiguration.GitResourceAvailableCondition)

   QuartzAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.quartz.Scheduler' (OnClassCondition)

   RabbitAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.rabbitmq.client.Channel' (OnClassCondition)

   ReactiveOAuth2ClientAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'reactor.core.publisher.Flux' (OnClassCondition)

   ReactiveOAuth2ResourceServerAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity' (OnClassCondition)

   ReactiveSecurityAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'reactor.core.publisher.Flux' (OnClassCondition)

   ReactiveUserDetailsServiceAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.security.authentication.ReactiveAuthenticationManager' (OnClassCondition)

   ReactiveWebServerFactoryAutoConfiguration:
      Did not match:
         - @ConditionalOnWebApplication did not find reactive web application classes (OnWebApplicationCondition)

   ReactorCoreAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'reactor.core.publisher.Flux' (OnClassCondition)

   RedisAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.data.redis.core.RedisOperations' (OnClassCondition)

   RedisCacheConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.data.redis.connection.RedisConnectionFactory' (OnClassCondition)

   RedisReactiveAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'reactor.core.publisher.Flux' (OnClassCondition)

   RedisRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.data.redis.repository.configuration.EnableRedisRepositories' (OnClassCondition)

   RepositoryRestMvcAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration' (OnClassCondition)

   RestClientAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.elasticsearch.client.RestClient' (OnClassCondition)

   SecurityAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.security.authentication.DefaultAuthenticationEventPublisher' (OnClassCondition)

   SecurityFilterAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.security.config.http.SessionCreationPolicy' (OnClassCondition)

   SendGridAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.sendgrid.SendGrid' (OnClassCondition)

   ServletWebServerFactoryConfiguration.EmbeddedJetty:
      Did not match:
         - @ConditionalOnClass did not find required classes 'org.eclipse.jetty.server.Server', 'org.eclipse.jetty.util.Loader', 'org.eclipse.jetty.webapp.WebAppContext' (OnClassCondition)

   ServletWebServerFactoryConfiguration.EmbeddedTomcat:
      Did not match:
         - @ConditionalOnMissingBean (types: org.springframework.boot.web.servlet.server.ServletWebServerFactory; SearchStrategy: current) found beans of type 'org.springframework.boot.web.servlet.server.ServletWebServerFactory' webServerFactory (OnBeanCondition)
      Matched:
         - @ConditionalOnClass found required classes 'javax.servlet.Servlet', 'org.apache.catalina.startup.Tomcat', 'org.apache.coyote.UpgradeProtocol' (OnClassCondition)

   ServletWebServerFactoryConfiguration.EmbeddedUndertow:
      Did not match:
         - @ConditionalOnClass did not find required classes 'io.undertow.Undertow', 'org.xnio.SslClientAuthMode' (OnClassCondition)

   SessionAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.session.Session' (OnClassCondition)

   SolrAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.apache.solr.client.solrj.impl.CloudSolrClient' (OnClassCondition)

   SolrRepositoriesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.apache.solr.client.solrj.SolrClient' (OnClassCondition)

   SpringDataWebAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.data.web.PageableHandlerMethodArgumentResolver' (OnClassCondition)

   TaskSchedulingAutoConfiguration#taskScheduler:
      Did not match:
         - @ConditionalOnBean (names: org.springframework.context.annotation.internalScheduledAnnotationProcessor; SearchStrategy: all) did not find any beans named org.springframework.context.annotation.internalScheduledAnnotationProcessor (OnBeanCondition)

   ThymeleafAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.thymeleaf.spring5.SpringTemplateEngine' (OnClassCondition)

   TransactionAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.transaction.PlatformTransactionManager' (OnClassCondition)

   UserDetailsServiceAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.security.authentication.AuthenticationManager' (OnClassCondition)

   WebClientAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.web.reactive.function.client.WebClient' (OnClassCondition)

   WebFluxAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.web.reactive.config.WebFluxConfigurer' (OnClassCondition)

   WebMvcAutoConfiguration.ResourceChainCustomizerConfiguration:
      Did not match:
         - @ConditionalOnEnabledResourceChain did not find class org.webjars.WebJarAssetLocator (OnEnabledResourceChainCondition)

   WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#beanNameViewResolver:
      Did not match:
         - @ConditionalOnMissingBean (types: org.springframework.web.servlet.view.BeanNameViewResolver; SearchStrategy: all) found beans of type 'org.springframework.web.servlet.view.BeanNameViewResolver' beanNameViewResolver (OnBeanCondition)

   WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter#localeResolver:
      Did not match:
         - @ConditionalOnProperty (spring.mvc.locale) did not find property 'locale' (OnPropertyCondition)

   WebServiceTemplateAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.oxm.Marshaller' (OnClassCondition)

   WebServicesAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.ws.transport.http.MessageDispatcherServlet' (OnClassCondition)

   WebSocketMessagingAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer' (OnClassCondition)

   WebSocketReactiveAutoConfiguration:
      Did not match:
         - @ConditionalOnWebApplication did not find reactive web application classes (OnWebApplicationCondition)

   WebSocketServletAutoConfiguration.JettyWebSocketConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer' (OnClassCondition)

   WebSocketServletAutoConfiguration.UndertowWebSocketConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'io.undertow.websockets.jsr.Bootstrap' (OnClassCondition)

   XADataSourceAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'javax.transaction.TransactionManager' (OnClassCondition)


Exclusions:
-----------

    None


Unconditional classes:
----------------------

    org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration

    org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration

    org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration



20:35:20.952 [main] DEBUG o.s.c.e.PropertySourcesPropertyResolver - Found key 'spring.liveBeansView.mbeanDomain' in PropertySource 'systemProperties' with value of type String
20:35:20.974 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Starting ProtocolHandler ["http-nio-8080"]
20:35:20.988 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Starting ProtocolHandler ["http-nio-8090"]
20:35:21.005 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port(s): 8080 (http) 8090 (http) with context path '/container-connector'
20:35:21.282 [RMI TCP Connection(4)-127.0.0.1] DEBUG o.s.c.e.PropertySourcesPropertyResolver - Found key 'local.server.port' in PropertySource 'server.ports' with value of type Integer
20:35:21.400 [RMI TCP Connection(4)-127.0.0.1] DEBUG o.s.c.e.PropertySourcesPropertyResolver - Found key 'server.servlet.context-path' in PropertySource 'configurationProperties' with value of type String


```
