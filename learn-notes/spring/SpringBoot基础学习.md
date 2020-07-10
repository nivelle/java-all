## springboot 启动

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/bin/java 

-XX:TieredStopAtLevel=1 
-noverify -Dspring.output.ansi.enabled=always
-Dcom.sun.management.jmxremote 
-Dcom.sun.management.jmxremote.port=56107 
-Dcom.sun.management.jmxremote.authenticate=false 
-Dcom.sun.management.jmxremote.ssl=false 
-Djava.rmi.server.hostname=127.0.0.1 
-Dspring.liveBeansView.mbeanDomain 
-Dspring.application.admin.enabled=true 
"-javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=56108:/Applications/IntelliJ IDEA.app/Contents/bin" 
-Dfile.encoding=UTF-8 
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
第五步: 启动容器engin:2020-07-08 18:05:58.187  INFO 40430 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.34]
第六步: 初始化WebApplicationContext: 2020-07-08 18:05:58.265  INFO 40430 --- [           main] o.a.c.c.C.[.[localhost].[/container]     : Initializing Spring embedded WebApplicationContext
第七步: WebApplicationContext 初始化完毕: 2020-07-08 18:05:58.266  INFO 40430 --- [           main] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 1263 ms
第七步: 初始化 applicationTaskExecutor: 2020-07-08 18:05:58.499  INFO 40430 --- [           main] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
第八步: 初始化 tomcat启动完毕: 2020-07-08 18:05:58.697  INFO 40430 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path '/container'
第九步: BootstrapApplication启动完毕: 2020-07-08 18:05:58.699  INFO 40430 --- [           main] c.n.container.BootstrapApplication       : Started BootstrapApplication in 2.034 seconds (JVM running for 2.61)


